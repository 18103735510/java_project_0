package com.example.survey.domain;

import java.time.Instant;
import java.util.List;

/**
 * 问卷聚合根：包含题目列表与当前状态。
 * 状态可变（发布 / 结束），题目列表不可变。
 */
public final class Questionnaire {

    private final long id;
    private final String title;
    private final Instant createdAt;
    private final List<Question> questions;
    private volatile Status status;

    public Questionnaire(long id, String title, Instant createdAt, Status status, List<Question> questions) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.status = status;
        this.questions = List.copyOf(questions);
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public Status getStatus() {
        return status;
    }

    public void markPublished() {
        this.status = Status.PUBLISHED;
    }

    public void markClosed() {
        this.status = Status.CLOSED;
    }

    public Question findQuestion(long questionId) {
        for (Question question : questions) {
            if (question.id() == questionId) {
                return question;
            }
        }
        return null;
    }
}
