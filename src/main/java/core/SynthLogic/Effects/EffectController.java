package core.SynthLogic.Effects;

import core.SynthLogic.Effects.EffectPicker;

public class EffectController {
    private final EffectPicker effectPicker;

    public EffectController(EffectPicker effectPicker) {
        this.effectPicker = effectPicker;
    }

    public void changeEffect(EffectPicker.EffectEnums effectEnum) {
        effectPicker.setEffect(effectEnum);
    }
}
