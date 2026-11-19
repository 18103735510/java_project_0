package com.example.survey.domain;

import java.util.List;

/**
 * 题目：不可变对象。
 *
 * @param min 打分题最小分值（其它题型为 null）
 * @param max 打分题最大分值（其它题型为 null）
 */
public record Question(long id, QuestionType type, String title, List<Option> options, Integer min, Integer max) {

    public Question {
        options = options == null ? List.of() : List.copyOf(options);
    }

    public Option option(String key) {
        for (Option option : options) {
            if (option.key().equals(key)) {
                return option;
            }
        }
        return null;
    }

    public boolean hasOption(String key) {
        return option(key) != null;
    }
}
