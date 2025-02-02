package core.SynthLogic.Effects;

import javax.swing.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface ParameterizedEffect {
    enum ControlType {
        KNOB, BUTTON, LIST
    }
    interface Parameter {
        String getDisplayName();
        ControlType getControlType();
        double getMin();
        double getMax();
        double getDefault();
        default Enum<?>[] getOptions() {
            return new Enum<?>[0];
        }
    }
    default Map<Enum<?>, Double> getAllParameters() {
        Map<Enum<?>, Double> params = new HashMap<>();
        for (Parameter param : getParameters()) {
            if (param instanceof Enum) {
                params.put((Enum<?>) param, getParameter(param));
            }
        }
        return params;
    }

    default void setAllParameters(Map<Enum<?>, Double> parameters) {
        parameters.forEach((param, value) -> {
            if (param instanceof Parameter) {
                setParameter((Parameter) param, value);
            }
        });
    }

    List<ParameterizedEffect.Parameter> getParameters();
    void setParameter(Parameter param, double value);
    double getParameter(Parameter param);
}
