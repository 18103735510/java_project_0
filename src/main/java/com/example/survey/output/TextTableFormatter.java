package com.example.survey.output;

import com.example.survey.common.Numbers;
import com.example.survey.common.TimeFormat;
import com.example.survey.statistics.Bucket;
import com.example.survey.statistics.NumericStat;
import com.example.survey.statistics.QuestionStat;
import com.example.survey.statistics.StatisticsReport;
import com.example.survey.statistics.TextStat;

import java.util.Locale;

/** 文本报表格式化器：终端 / 页面都能直接读的纯文本，不依赖任何样式。 */
public final class TextTableFormatter implements ReportFormatter {

    @Override
    public String contentType() {
        return "text/plain; charset=utf-8";
    }

    @Override
    public String format(StatisticsReport report) {
        StringBuilder sb = new StringBuilder(2048);
        sb.append("================ 问卷调查统计系统 · 统计报告 ================\n\n");
        sb.append("问卷：#").append(report.questionnaireId()).append('　').append(report.title()).append('\n');
        sb.append("提交总数：").append(report.totalResponses()).append(" 份\n");
        sb.append("生成时间：").append(TimeFormat.display(report.generatedAt())).append('\n');
        if (report.totalResponses() == 0) {
            sb.append("\n提示：当前还没有任何答卷，以下为空数据结果（平均分按 0 处理，不执行除零运算）。\n");
        }

        sb.append('\n').append("---------------- 题目概览 ----------------\n");
        int index = 1;
        for (QuestionStat stat : report.questions()) {
            sb.append(" ").append(String.format(Locale.ROOT, "%2d", index++)).append(". [")
                    .append(pad(stat.type().label(), 6)).append("] ")
                    .append(stat.title())
                    .append("（答题 ").append(stat.responseCount()).append(" 人）\n");
        }

        sb.append('\n').append("---------------- 题目明细 ----------------\n");
        for (int i = 0; i < report.questions().size(); i++) {
            appendQuestionDetail(sb, i + 1, report.questions().get(i));
        }

        sb.append("==========================================================\n");
        sb.append("报告结束：共 ").append(report.questions().size()).append(" 道题，")
                .append(report.totalResponses()).append(" 份答卷。\n");
        return sb.toString();
    }

    private static void appendQuestionDetail(StringBuilder sb, int index, QuestionStat stat) {
        sb.append('\n').append('[').append(index).append("] ").append(stat.title())
                .append("（").append(stat.type().label()).append("）\n");
        sb.append("    答题人数：").append(stat.responseCount()).append(" 人\n");

        if (stat.numeric() != null) {
            NumericStat numeric = stat.numeric();
            sb.append("    平均分：").append(Numbers.fixed(numeric.avg(), 4))
                    .append("　最高分：").append(Numbers.score(numeric.max()))
                    .append("　最低分：").append(Numbers.score(numeric.min()))
                    .append("　合计：").append(Numbers.score(numeric.sum()))
                    .append("　样本数：").append(numeric.count()).append('\n');
            if (numeric.count() == 0) {
                sb.append("    （空数据：没有可计算的分数，平均分按 0 返回，未执行除法）\n");
            }
        }

        if (stat.distribution() != null) {
            if (stat.distribution().isEmpty()) {
                sb.append("    分布：暂无数据\n");
            } else {
                sb.append("    分布：\n");
                for (Bucket bucket : stat.distribution()) {
                    sb.append("      ").append(pad(bucket.label(), 18)).append(' ')
                            .append(pad(bar(bucket.ratio()), 20))
                            .append(String.format(Locale.ROOT, "%3d", bucket.count())).append(" 人")
                            .append('　').append(Numbers.percent(bucket.ratio())).append('\n');
                }
            }
        }

        if (stat.text() != null) {
            TextStat text = stat.text();
            sb.append("    有效作答：").append(text.count()).append(" 条");
            if (text.count() > 0) {
                sb.append("　平均字数：").append(Numbers.fixed(text.avgLength(), 2));
            }
            sb.append('\n');
            if (text.samples().isEmpty()) {
                sb.append("    样例：暂无\n");
            } else {
                sb.append("    样例：\n");
                for (String sample : text.samples()) {
                    sb.append("      · ").append(sample).append('\n');
                }
            }
        }
    }

    private static String bar(double ratio) {
        int length = (int) Math.round(Math.max(0.0, Math.min(1.0, ratio)) * 20);
        return "#".repeat(length);
    }

    /** 按显示宽度补空格，中文算两列（不追求完美，够看就行）。 */
    private static String pad(String text, int width) {
        int actual = displayWidth(text);
        if (actual >= width) {
            return text;
        }
        return text + " ".repeat(width - actual);
    }

    private static int displayWidth(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += isWide(text.charAt(i)) ? 2 : 1;
        }
        return width;
    }

    private static boolean isWide(char c) {
        return (c >= 0x1100 && c <= 0x115F)
                || (c >= 0x2E80 && c <= 0xA4CF)
                || (c >= 0xAC00 && c <= 0xD7A3)
                || (c >= 0xF900 && c <= 0xFAFF)
                || (c >= 0xFE30 && c <= 0xFE6F)
                || (c >= 0xFF00 && c <= 0xFF60)
                || (c >= 0xFFE0 && c <= 0xFFE6);
    }
}
