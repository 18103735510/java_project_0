package com.example.survey.statistics;

/** 数值统计结果：样本数、总分、平均分、最高分、最低分。空数据时全部为 0。 */
public record NumericStat(int count, double sum, double avg, double max, double min) {
}
