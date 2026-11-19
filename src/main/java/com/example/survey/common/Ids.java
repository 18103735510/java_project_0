package com.example.survey.common;

import java.util.concurrent.atomic.AtomicLong;

/** 自增主键生成器（演示用；生产环境应换成数据库序列或雪花算法）。 */
public final class Ids {

    private static final AtomicLong QUESTIONNAIRES = new AtomicLong(1000);
    private static final AtomicLong QUESTIONS = new AtomicLong(100);
    private static final AtomicLong SUBMISSIONS = new AtomicLong(5000);

    private Ids() {
    }

    public static long nextQuestionnaireId() {
        return QUESTIONNAIRES.incrementAndGet();
    }

    public static long nextQuestionId() {
        return QUESTIONS.incrementAndGet();
    }

    public static long nextSubmissionId() {
        return SUBMISSIONS.incrementAndGet();
    }
}
