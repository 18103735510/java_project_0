package com.example.survey.domain;

/** 选项：key 是稳定标识（A/B/C…），label 是给人看的文案。 */
public record Option(String key, String label) {

    public Option {
        key = key == null ? "" : key.trim();
        label = label == null ? "" : label.trim();
    }
}
