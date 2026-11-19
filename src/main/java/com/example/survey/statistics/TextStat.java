package com.example.survey.statistics;

import java.util.List;

/** 文本统计结果：有效条数、平均字数、样例（最多 20 条，避免报告无限膨胀）。 */
public record TextStat(int count, double avgLength, List<String> samples) {

    public TextStat {
        samples = samples == null ? List.of() : List.copyOf(samples);
    }
}
