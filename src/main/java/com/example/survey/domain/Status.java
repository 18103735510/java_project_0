package com.example.survey.domain;

/** 问卷状态：草稿 -> 收集中 -> 已结束。 */
public enum Status {

    DRAFT("草稿"),
    PUBLISHED("收集中"),
    CLOSED("已结束");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
