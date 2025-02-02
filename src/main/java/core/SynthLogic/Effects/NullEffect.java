package core.SynthLogic.Effects;

import java.util.List;

public class NullEffect implements EffectRack, ParameterizedEffect {
    @Override
    public double[] applyEffect(double[] mixBuffer) {
        return mixBuffer;
    }


    @Override
    public List<Parameter> getParameters() {
        return List.of();
    }

    @Override
    public void setParameter(Parameter paramName, double value) {

    }

    @Override
    public double getParameter(Parameter paramName) {
        return 0;
    }
}
