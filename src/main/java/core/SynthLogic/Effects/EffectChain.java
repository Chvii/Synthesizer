package core.SynthLogic.Effects;

import java.util.ArrayList;

public class EffectChain implements EffectRack {
    private ArrayList<EffectRack> effects = new ArrayList<>();

    @Override
    public double[] applyEffect(double[] buffer) {
        for (EffectRack effect : effects) {
            buffer = effect.applyEffect(buffer);
        }
        return buffer;
    }
    public void addEffect(EffectRack effect){
        effects.add(effect);
    }

    public void removeEffect(EffectRack effect){
        effects.remove(effect);
    }

    public ArrayList<EffectRack> getEffects() {
        return effects;
    }

    public void reorderEffect(int fromIndex, int toIndex){
        // I dont even care if this could also be achieved with a simple Array.add() Array.remove(), this algorithm is my masterpiece
        if(fromIndex <= effects.size() || toIndex <= effects.size()) {
            ArrayList<EffectRack> bufferList = (ArrayList)effects.clone();
            EffectRack effectToReorder = effects.get(fromIndex);
            int elementsToMove = Math.abs(fromIndex - toIndex);
            int indexSwitch = toIndex;
            EffectRack effectSwapper;
            for(int i = 0; Math.abs(i) <= elementsToMove; i++) {   // this is madness
                int j = (fromIndex > toIndex) ? i :-i;
                effectSwapper = bufferList.get(indexSwitch);
                indexSwitch = toIndex + j;
                effects.set(indexSwitch, effectSwapper);
            }
            effects.set(toIndex, effectToReorder);
        }
    }

    public void clear() {
        effects.clear();
    }
}