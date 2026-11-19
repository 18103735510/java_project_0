package com.example.survey.statistics;

import com.example.survey.domain.QuestionType;

import java.util.List;

/**
 * 单题统计结果。
 * numeric / distribution / text 按题型只有其一非空，其余为 null，
 * 保证输出层只渲染与题型相关的字段。
 */
public record QuestionStat(
        long questionId,
        String title,
        QuestionType type,
        int responseCount,
        NumericStat numeric,
        List<Bucket> distribution,
        TextStat text) {

    public QuestionStat {
        distribution = distribution == null ? null : List.copyOf(distribution);
    }
}
