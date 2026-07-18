package net.otsutsukimiho.nozomiaddon.utils;

public class RangeSetting extends Settings {
    private int min;
    private int max;
    private final int absoluteMin;
    private final int absoluteMax;
    private final int increment;

    public RangeSetting(String name, int defaultMin, int defaultMax, int absoluteMin, int absoluteMax, int increment) {
        super(name);
        this.absoluteMin = absoluteMin;
        this.absoluteMax = absoluteMax;
        this.increment = increment;

        this.min = Math.max(absoluteMin, Math.min(defaultMin, absoluteMax));
        this.max = Math.max(absoluteMin, Math.min(defaultMax, absoluteMax));

        if (this.min > this.max) {
            this.min = this.max;
        }
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        min = Math.round((float) min / increment) * increment;
        this.min = Math.max(this.absoluteMin, Math.min(min, this.max));
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        max = Math.round((float) max / increment) * increment;
        this.max = Math.max(this.min, Math.min(max, this.absoluteMax));
    }

    public int getAbsoluteMin() {
        return absoluteMin;
    }

    public int getAbsoluteMax() {
        return absoluteMax;
    }

    public int getIncrement() {
        return increment;
    }

    public double getRandomValue() {
        if (min == max) return min;
        return min + (Math.random() * (max - min));
    }
}