package com.example.survey.statistics;

import com.example.survey.domain.Answer;
import com.example.survey.domain.Question;

import java.util.ArrayList;
import java.util.List;

/** 填空题累加器：统计有效作答条数、平均字数，并保留前若干条样例。 */
public final class TextAccumulator implements Accumulator {

    private static final int MAX_SAMPLES = 20;

    private int count;
    private long totalLength;
    private final List<String> samples = new ArrayList<>();

    @Override
    public void accept(Answer answer) {
        for (String raw : answer.values()) {
            if (raw == null) {
                continue;
            }
            String text = raw.trim();
            if (text.isEmpty()) {
                continue;
            }
            count++;
            totalLength += text.length();
            if (samples.size() < MAX_SAMPLES) {
                samples.add(text);
            }
        }
    }

    @Override
    public QuestionStat result(Question question, int responseCount) {
        TextStat textStat = new TextStat(count, ZeroGuard.average(totalLength, count), samples);
        return new QuestionStat(question.id(), question.title(), question.type(),
                responseCount, null, null, textStat);
    }
}
