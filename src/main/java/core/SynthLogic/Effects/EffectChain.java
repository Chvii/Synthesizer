package core.SynthLogic.Effects;

import java.util.ArrayList;
import java.util.List;

public class EffectChain implements EffectRack {

    private ArrayList<EffectRack> effects = new ArrayList<>();

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

    public ArrayList<EffectRack> getEffects() { // its just a getter
        return effects;
    }
    public void addEffects(EffectRack effect){
        effects.add(effect);
    }

    public void reorderEffect(int fromIndex, int toIndex){
        // I dont even care if this could also be achieved with a simple Array.add() Array.remove(), this algorithm is my masterpiece
        if(fromIndex <= effects.size()+1 || toIndex <= effects.size()) {
            ArrayList<EffectRack> bufferList = (ArrayList)effects.clone();
            EffectRack effectToReorder = effects.get(fromIndex);
            int elementsToMove = Math.abs(fromIndex - toIndex);
            int indexSwitch = toIndex;
            EffectRack effectSwapper;
            for(int i = 0; Math.abs(i) <= elementsToMove; i++) {   //conditional operator in the for-loop lets go, this is madness
                int j = (fromIndex > toIndex) ? i :-i;
                effectSwapper = bufferList.get(indexSwitch);
                indexSwitch = toIndex + j;
                effects.set(indexSwitch, effectSwapper);
            }
            effects.set(toIndex, effectToReorder);
        }
    }
    @Override
    public EffectRack getEffect() {
        return this;
    }
}