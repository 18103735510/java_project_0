package com.example.survey.statistics;

import com.example.survey.domain.Answer;
import com.example.survey.domain.Submission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 分组器：把「按答卷展开的答案」重组成「按题目聚合的答案」。 */
public final class Grouper {

    private Grouper() {
    }

    /**
     * 一次遍历完成 Map 分组：key = 题目 ID，value = 这道题的全部作答。
     * 没人作答的题目不会出现在 Map 里，调用方用 getOrDefault 走空数据分支。
     */
    public static Map<Long, List<Answer>> byQuestion(List<Submission> submissions) {
        Map<Long, List<Answer>> grouped = new LinkedHashMap<>();
        if (submissions == null) {
            return grouped;
        }
        for (Submission submission : submissions) {
            for (Answer answer : submission.answers()) {
                grouped.computeIfAbsent(answer.questionId(), key -> new ArrayList<>()).add(answer);
            }
        }
        return grouped;
    }
}
