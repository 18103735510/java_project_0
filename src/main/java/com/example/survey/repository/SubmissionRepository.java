package com.example.survey.repository;

import com.example.survey.domain.Submission;

import java.util.List;

/** 答卷仓储。 */
public interface SubmissionRepository {

    Submission save(Submission submission);

    /** 返回的是快照列表，统计时不会与并发写入互相干扰。 */
    List<Submission> findByQuestionnaireId(long questionnaireId);

    boolean existsBySubmitter(long questionnaireId, String submitter);
}
