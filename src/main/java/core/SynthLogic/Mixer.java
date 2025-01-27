package core.SynthLogic;

import core.Constants.ConstantValues;
import core.SynthLogic.Effects.EffectRack;
import core.Visuals.WaveformPanel;
import core.Visuals.WaveformUpdateListener;

import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Mixer{
    private final SourceDataLine line;
    private final CopyOnWriteArrayList<Voice> activeVoices;
    private final ExecutorService executor;
    private final List<WaveformUpdateListener> listeners = new ArrayList<>();
    private EffectRack effectRack;
    private final ThreadLocal<float[]> threadLocalMixBuffer = ThreadLocal.withInitial(() -> new float[ConstantValues.BUFFER_SIZE]);
    private final ThreadLocal<byte[]> threadLocalByteBuffer = ThreadLocal.withInitial(() -> new byte[ConstantValues.BUFFER_SIZE*2]);



    public Mixer(SourceDataLine line, EffectRack effectRack) {
        this.effectRack = effectRack;
        this.line = line;
        this.activeVoices = new CopyOnWriteArrayList<>();
        this.executor = Executors.newCachedThreadPool(); // Thread pool for voice processing
    }
    public void addWaveformUpdateListener(WaveformUpdateListener listener) {
        listeners.add(listener);
    }
    public void removeWaveformUpdateListener(WaveformUpdateListener listener) {
        listeners.remove(listener);
    }
    private void notifyWaveformUpdate(float[] mixBuffer) {
        for (WaveformUpdateListener listener : listeners) {
            listener.updateWaveform(mixBuffer);
        }
    }

    public void addVoice(Voice voice) {
        activeVoices.add(voice);
    }

    public void removeVoice(Note note) {
        activeVoices.stream()
                .filter(v -> v.getNote() == note)
                .forEach(Voice::stopVoice);

    }
    public void overrideVoice(Note note){
        activeVoices.stream()
                .filter(v -> v.getNote() == note)
                .forEach(activeVoices::remove);
    }

    public CopyOnWriteArrayList<Voice> getActiveVoices() {
        return activeVoices;
    }

    public synchronized void start() {
        new Thread(() -> {
            float[] mixBuffer = threadLocalMixBuffer.get();
            byte[] byteBuffer = threadLocalByteBuffer.get();

            while (true) {
                Arrays.fill(mixBuffer, 0);

                // Process voices only if active
                if (!activeVoices.isEmpty()) {
                    List<Future<float[]>> futures = activeVoices.stream()
                            .map(voice -> executor.submit(voice::generateAudio))
                            .toList();

                    for (int i = 0; i < mixBuffer.length; i++) {
                        for (Future<float[]> future : futures) {
                            try {
                                float[] voiceBuffer = future.get();
                                mixBuffer[i] += voiceBuffer[i]/7;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Ensure silence buffers still go through effects
                mixBuffer = effectRack.applyEffect(mixBuffer);


                // Clamp and output
                for (int i = 0; i < mixBuffer.length; i++) {
                    mixBuffer[i] = Math.max(-1.0f, Math.min(1.0f, mixBuffer[i]));
                }

                notifyWaveformUpdate(mixBuffer);

                for (int i = 0; i < mixBuffer.length; i++) {
                    int intSample = (int) (mixBuffer[i] * 32767);
                    byteBuffer[i * 2] = (byte) ((intSample >> 8) & 0xFF);
                    byteBuffer[i * 2 + 1] = (byte) (intSample & 0xFF);
                }
                line.write(byteBuffer, 0, byteBuffer.length);

                // Clean up stopped voices
                activeVoices.removeIf(Voice::isStopped);
            }
        }).start();
    }
}
