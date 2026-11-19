package com.example.survey.domain;

import java.util.Locale;

/** 题型：打分题、单选题、多选题、填空题。 */
public enum QuestionType {

    SCALE("打分题"),
    SINGLE("单选题"),
    MULTI("多选题"),
    TEXT("填空题");

    private final String label;

    QuestionType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean numeric() {
        return this == SCALE;
    }

    public boolean choice() {
        return this == SINGLE || this == MULTI;
    }

    public static QuestionType parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("题型不能为空");
        }
        try {
            return QuestionType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知题型：" + raw + "（可选 SCALE / SINGLE / MULTI / TEXT）");
        }
    }
}
