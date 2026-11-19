package com.example.survey.statistics;

import com.example.survey.domain.Answer;
import com.example.survey.domain.Option;
import com.example.survey.domain.Question;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 选择型累加器（单选 / 多选）。
 *
 * <p>先用 Map 记频次，出结果时再按题目里选项的定义顺序展开，
 * 这样输出顺序稳定，同一份数据永远得到同一份报告。</p>
 */
public final class ChoiceAccumulator implements Accumulator {

    private final Map<String, Integer> frequency = new LinkedHashMap<>();

    @Override
    public void accept(Answer answer) {
        for (String value : answer.values()) {
            frequency.merge(value, 1, Integer::sum);
        }
    }

    @Override
    public QuestionStat result(Question question, int responseCount) {
        if (responseCount <= 0) {
            // 空数据：不列一堆 0，直接给空分布
            return new QuestionStat(question.id(), question.title(), question.type(),
                    responseCount, null, List.of(), null);
        }
        List<Bucket> buckets = new ArrayList<>(question.options().size());
        for (Option option : question.options()) {
            int hits = frequency.getOrDefault(option.key(), 0);
            buckets.add(new Bucket(option.key(), option.label(), hits, ZeroGuard.ratio(hits, responseCount)));
        }
        return new QuestionStat(question.id(), question.title(), question.type(),
                responseCount, null, buckets, null);
    }
}
