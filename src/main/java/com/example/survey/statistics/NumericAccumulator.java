package com.example.survey.statistics;

import com.example.survey.domain.Answer;
import com.example.survey.domain.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/** 打分题累加器：一次遍历同时算出样本数、合计、平均分、最高分、最低分与分值分布。 */
public final class NumericAccumulator implements Accumulator {

    private int count;
    private double sum;
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;
    private final TreeMap<Double, Integer> distribution = new TreeMap<>();

    @Override
    public void accept(Answer answer) {
        for (String raw : answer.values()) {
            double value;
            try {
                value = Double.parseDouble(raw.trim());
            } catch (NumberFormatException e) {
                continue; // 脏数据直接跳过，统计不因一条坏数据整体失败
            }
            count++;
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
            distribution.merge(value, 1, Integer::sum);
        }
    }

    @Override
    public QuestionStat result(Question question, int responseCount) {
        if (count == 0) {
            // 空数据分支：不计算、不除零，直接给一组 0
            return new QuestionStat(question.id(), question.title(), question.type(),
                    responseCount, ZeroGuard.emptyNumeric(), List.of(), null);
        }
        NumericStat numeric = new NumericStat(count, sum, ZeroGuard.average(sum, count), max, min);
        List<Bucket> buckets = new ArrayList<>(distribution.size());
        for (var entry : distribution.entrySet()) {
            String label = ZeroGuard.scoreLabel(entry.getKey());
            buckets.add(new Bucket(label, label + " 分", entry.getValue(),
                    ZeroGuard.ratio(entry.getValue(), count)));
        }
        return new QuestionStat(question.id(), question.title(), question.type(),
                responseCount, numeric, buckets, null);
    }
}
