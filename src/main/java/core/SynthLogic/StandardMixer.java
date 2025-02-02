package core.SynthLogic;

import core.Constants.ConstantValues;
import core.SynthLogic.Effects.SaturatorEffect;
import core.SynthLogic.Effects.EffectChain;
import core.SynthLogic.Effects.EffectRack;
import core.Visuals.WaveformUpdateListener;

import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StandardMixer implements Mixer {
    private final SourceDataLine line;
    private final CopyOnWriteArrayList<Voice> activeVoices;
    private final ExecutorService executor;
    private final List<WaveformUpdateListener> listeners = new ArrayList<>();
    private EffectChain effectChain;
    private final ThreadLocal<double[]> threadLocalMixBuffer = ThreadLocal.withInitial(() -> new double[ConstantValues.BUFFER_SIZE]);
    private final ThreadLocal<byte[]> threadLocalByteBuffer = ThreadLocal.withInitial(() -> new byte[ConstantValues.BUFFER_SIZE*2]);



    public StandardMixer(SourceDataLine line, EffectChain effectChain) {
        this.effectChain = effectChain;
        this.line = line;
        this.activeVoices = new CopyOnWriteArrayList<>();
        getActiveEffects();
        this.executor = Executors.newCachedThreadPool(); // Thread pool for voice processing
    }
    public ArrayList<EffectRack> getActiveEffects(){
        return effectChain.getEffects();
    }
    @Override
    public void addWaveformUpdateListener(WaveformUpdateListener listener) {
        listeners.add(listener);
    }
    @Override
    public void removeWaveformUpdateListener(WaveformUpdateListener listener) {
        listeners.remove(listener);
    }
    private void notifyWaveformUpdate(double[] mixBuffer) {
        for (WaveformUpdateListener listener : listeners) {
            listener.updateWaveform(mixBuffer);
        }
    }

    @Override
    public void addVoice(Voice voice) {
        activeVoices.add(voice);
    }

    @Override
    public void removeVoice(Note note) {
        activeVoices.stream()
                .filter(v -> v.getNote() == note)
                .forEach(Voice::stopVoice);

    }
    @Override
    public void overrideVoice(Note note){
        activeVoices.stream()
                .filter(v -> v.getNote() == note)
                .forEach(activeVoices::remove);
    }

    @Override
    public CopyOnWriteArrayList<Voice> getActiveVoices() {
        return activeVoices;
    }

    @Override
    public synchronized void startMixer() {
        new Thread(() -> {
            double[] mixBuffer = threadLocalMixBuffer.get();
            byte[] byteBuffer = threadLocalByteBuffer.get();

            while (true) {
                Arrays.fill(mixBuffer, 0);

                // Process voices only if active
                if (!activeVoices.isEmpty()) {
                    List<Future<double[]>> futures = activeVoices.stream()
                            .map(voice -> executor.submit(voice::generateAudio))
                            .toList();

                    for (int i = 0; i < mixBuffer.length; i++) {
                        for (Future<double[]> future : futures) {
                            try {
                                double[] voiceBuffer = future.get();
                                mixBuffer[i] += voiceBuffer[i] / 7;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                mixBuffer = effectChain.applyEffect(mixBuffer);

                /**
                 *  FOR TESTING NEW EFFECTS, OTHERWISE COMMENT OUT
                 **/
                //EffectRack testEffect = new SaturatorEffect();
                //mixBuffer = testEffect.applyEffect(mixBuffer);
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

