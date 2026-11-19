package com.example.survey.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 数字格式化助手：报告里统一 4 位小数、百分比 2 位小数，且绝不让 NaN / Infinity 流到输出。 */
public final class Numbers {

    private Numbers() {
    }

    /** 保留至多 scale 位小数并去掉多余的 0；NaN / Infinity 一律按 0 处理。 */
    public static String fixed(double value, int scale) {
        double safe = (Double.isNaN(value) || Double.isInfinite(value)) ? 0.0 : value;
        return BigDecimal.valueOf(safe)
                .setScale(scale, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    public static String percent(double ratio) {
        return fixed(ratio * 100.0, 2) + "%";
    }

    /** 打分分值：整数不带小数点。 */
    public static String score(double value) {
        return fixed(value, 2);
    }
}
