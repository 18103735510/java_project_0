package com.example.survey.output;

import com.example.survey.common.Json;
import com.example.survey.common.TimeFormat;
import com.example.survey.statistics.Bucket;
import com.example.survey.statistics.NumericStat;
import com.example.survey.statistics.QuestionStat;
import com.example.survey.statistics.StatisticsReport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON 格式化器：键序固定、比例保留 4 位小数、空数据输出 0 而不是 null。
 * 这是「结果输出格式规范」的实际执行者。
 */
public final class JsonFormatter implements ReportFormatter {

    @Override
    public String contentType() {
        return "application/json; charset=utf-8";
    }

    @Override
    public String format(StatisticsReport report) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("questionnaireId", report.questionnaireId());
        root.put("questionnaireTitle", report.title());
        root.put("totalResponses", report.totalResponses());
        root.put("generatedAt", TimeFormat.iso(report.generatedAt()));

        List<Object> questions = new ArrayList<>(report.questions().size());
        for (QuestionStat stat : report.questions()) {
            questions.add(questionNode(stat));
        }
        root.put("questions", questions);
        return Json.stringify(root, true);
    }

    private static Map<String, Object> questionNode(QuestionStat stat) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("questionId", stat.questionId());
        node.put("title", stat.title());
        node.put("type", stat.type().name());
        node.put("responseCount", stat.responseCount());

        if (stat.numeric() != null) {
            NumericStat numeric = stat.numeric();
            Map<String, Object> numbers = new LinkedHashMap<>();
            numbers.put("count", numeric.count());
            numbers.put("sum", round(numeric.sum()));
            numbers.put("avg", round(numeric.avg()));
            numbers.put("max", round(numeric.max()));
            numbers.put("min", round(numeric.min()));
            node.put("numeric", numbers);
        }

        if (stat.distribution() != null) {
            List<Object> buckets = new ArrayList<>(stat.distribution().size());
            for (Bucket bucket : stat.distribution()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("key", bucket.key());
                item.put("label", bucket.label());
                item.put("count", bucket.count());
                item.put("ratio", round(bucket.ratio()));
                buckets.add(item);
            }
            node.put("distribution", buckets);
        }

        if (stat.text() != null) {
            Map<String, Object> text = new LinkedHashMap<>();
            text.put("count", stat.text().count());
            text.put("avgLength", round(stat.text().avgLength()));
            text.put("samples", stat.text().samples());
            node.put("text", text);
        }
        return node;
    }

    private static double round(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.round(value * 10000.0) / 10000.0;
    }
}
