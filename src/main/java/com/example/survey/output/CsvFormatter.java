package com.example.survey.output;

import com.example.survey.common.Numbers;
import com.example.survey.common.TimeFormat;
import com.example.survey.statistics.Bucket;
import com.example.survey.statistics.NumericStat;
import com.example.survey.statistics.QuestionStat;
import com.example.survey.statistics.StatisticsReport;

/**
 * CSV 格式化器：长表结构（一行一个指标），方便直接丢进 Excel 做二次分析。
 * 文件头带 UTF-8 BOM，避免 Windows Excel 打开中文乱码。
 */
public final class CsvFormatter implements ReportFormatter {

    private static final String HEADER = "questionId,title,type,responseCount,metric,key,label,value,ratio";

    @Override
    public String contentType() {
        return "text/csv; charset=utf-8";
    }

    @Override
    public String format(StatisticsReport report) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append('\uFEFF').append(HEADER).append("\r\n");

        // 报告级汇总
        row(sb, "", "问卷统计报告", "", "", "summary", "", "totalResponses",
                String.valueOf(report.totalResponses()), "");
        row(sb, "", "问卷统计报告", "", "", "summary", "", "generatedAt",
                TimeFormat.display(report.generatedAt()), "");
        row(sb, "", "问卷统计报告", "", "", "summary", "", "questionnaireId",
                String.valueOf(report.questionnaireId()), "");
        row(sb, "", "问卷统计报告", "", "", "summary", "", "questionnaireTitle",
                report.title(), "");

        for (QuestionStat stat : report.questions()) {
            String id = String.valueOf(stat.questionId());
            String title = stat.title();
            String type = stat.type().name();
            String count = String.valueOf(stat.responseCount());

            if (stat.numeric() != null) {
                NumericStat numeric = stat.numeric();
                row(sb, id, title, type, count, "count", "", "样本数", String.valueOf(numeric.count()), "");
                row(sb, id, title, type, count, "sum", "", "合计", Numbers.score(numeric.sum()), "");
                row(sb, id, title, type, count, "avg", "", "平均分", Numbers.fixed(numeric.avg(), 4), "");
                row(sb, id, title, type, count, "max", "", "最高分", Numbers.score(numeric.max()), "");
                row(sb, id, title, type, count, "min", "", "最低分", Numbers.score(numeric.min()), "");
            }

            if (stat.distribution() != null) {
                if (stat.distribution().isEmpty()) {
                    row(sb, id, title, type, count, "distribution", "", "暂无数据", "0", "0");
                } else {
                    for (Bucket bucket : stat.distribution()) {
                        row(sb, id, title, type, count, "distribution", bucket.key(), bucket.label(),
                                String.valueOf(bucket.count()), Numbers.fixed(bucket.ratio(), 4));
                    }
                }
            }

            if (stat.text() != null) {
                row(sb, id, title, type, count, "textCount", "", "有效条数",
                        String.valueOf(stat.text().count()), "");
                row(sb, id, title, type, count, "textAvgLength", "", "平均字数",
                        Numbers.fixed(stat.text().avgLength(), 2), "");
                for (String sample : stat.text().samples()) {
                    row(sb, id, title, type, count, "sample", "", "样例", sample, "");
                }
            }
        }
        return sb.toString();
    }

    private static void row(StringBuilder sb, String questionId, String title, String type, String responseCount,
                            String metric, String key, String label, String value, String ratio) {
        sb.append(csv(questionId)).append(',')
                .append(csv(title)).append(',')
                .append(csv(type)).append(',')
                .append(csv(responseCount)).append(',')
                .append(csv(metric)).append(',')
                .append(csv(key)).append(',')
                .append(csv(label)).append(',')
                .append(csv(value)).append(',')
                .append(csv(ratio)).append("\r\n");
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        boolean needQuote = value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
        String escaped = value.replace("\"", "\"\"");
        return needQuote ? "\"" + escaped + "\"" : escaped;
    }
}
