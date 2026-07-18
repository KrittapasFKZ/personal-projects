package net.otsutsukimiho.nozomiaddon.utils;

public class NumberSetting extends Settings {
    private int value;
    private final int min;
    private final int max;
    private final int increment;

    public NumberSetting(String name, int defaultValue, int min, int max, int increment) {
        super(name);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        value = Math.max(min, Math.min(max, value));

        if (increment != 0) {
            double steps = (double) (value - min) / increment;
            int snappedSteps = (int) Math.round(steps);
            value = min + (snappedSteps * increment);
        }

        this.value = value;
    }

    public void setValue(double value) {
        setValue((int) value);
    }

    public int getMin() { return min; }
    public int getMax() { return max; }
}