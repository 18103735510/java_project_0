package com.example.survey.statistics;

/**
 * 除零保护：所有除法都必须从这里走。
 * 空数据（分母为 0）返回 0，而不是 NaN / Infinity / 抛 ArithmeticException。
 */
public final class ZeroGuard {

    private ZeroGuard() {
    }

    public static double average(double sum, int count) {
        return count <= 0 ? 0.0 : sum / count;
    }

    public static double ratio(int part, int total) {
        return total <= 0 ? 0.0 : (double) part / (double) total;
    }

    public static NumericStat emptyNumeric() {
        return new NumericStat(0, 0.0, 0.0, 0.0, 0.0);
    }

    /** 二分值转展示文案：整数不带小数点。 */
    public static String scoreLabel(double value) {
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }
}
