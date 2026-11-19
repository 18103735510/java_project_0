package com.example.survey.domain;

import java.time.Instant;
import java.util.List;

/** 一份完整的答卷（一个提交人针对一份问卷）。 */
public record Submission(long id, long questionnaireId, String submitter, Instant submittedAt, List<Answer> answers) {

    public Submission {
        answers = answers == null ? List.of() : List.copyOf(answers);
    }
}
