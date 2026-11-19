package com.example.survey.statistics;

import java.time.Instant;
import java.util.List;

/** 整份问卷的统计报告：输出层（JSON / 文本 / CSV）的唯一数据源。 */
public record StatisticsReport(
        long questionnaireId,
        String title,
        int totalResponses,
        Instant generatedAt,
        List<QuestionStat> questions) {

    public StatisticsReport {
        questions = questions == null ? List.of() : List.copyOf(questions);
    }
}
