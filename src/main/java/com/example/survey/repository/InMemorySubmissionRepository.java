package com.example.survey.repository;

import com.example.survey.domain.Submission;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** 内存版答卷仓储：公用的 ConcurrentHashMap + 提交人索引（防止同一昵称重复提交）。 */
public final class InMemorySubmissionRepository implements SubmissionRepository {

    private final Map<Long, Submission> store = new ConcurrentHashMap<>();
    private final Set<String> submitterIndex = ConcurrentHashMap.newKeySet();

    @Override
    public Submission save(Submission submission) {
        store.put(submission.id(), submission);
        submitterIndex.add(indexKey(submission.questionnaireId(), submission.submitter()));
        return submission;
    }

    @Override
    public List<Submission> findByQuestionnaireId(long questionnaireId) {
        return store.values().stream()
                .filter(submission -> submission.questionnaireId() == questionnaireId)
                .sorted(Comparator.comparing(Submission::submittedAt))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Override
    public boolean existsBySubmitter(long questionnaireId, String submitter) {
        return submitterIndex.contains(indexKey(questionnaireId, submitter));
    }

    private static String indexKey(long questionnaireId, String submitter) {
        return questionnaireId + "|" + (submitter == null ? "" : submitter.trim().toLowerCase(Locale.ROOT));
    }
}
