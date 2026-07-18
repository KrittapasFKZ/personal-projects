package net.otsutsukimiho.nozomiaddon.utils;

public class FloatSetting extends Settings {
    private float value;
    private final float min;
    private final float max;
    private final float increment;

    public FloatSetting(String name, float defaultValue, float min, float max, float increment) {
        super(name);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        value = Math.max(min, Math.min(max, value));

        if (increment != 0) {
            float steps = (value - min) / increment;
            int snappedSteps = Math.round(steps);
            value = min + (snappedSteps * increment);
        }

        this.value = Math.round(value * 1000.0f) / 1000.0f;
    }

    public float getMin() { return min; }
    public float getMax() { return max; }
}