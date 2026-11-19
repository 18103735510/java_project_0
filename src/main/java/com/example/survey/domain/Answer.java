package com.example.survey.domain;

import java.util.List;

/**
 * 一份答卷里某道题的作答结果。
 * values 的语义：单选题/打分题只有 1 个值（选项 key 或分值），多选题有多个选项 key，填空题是文本。
 */
public record Answer(long questionId, List<String> values) {

    public Answer {
        values = values == null ? List.of() : List.copyOf(values);
    }
}
