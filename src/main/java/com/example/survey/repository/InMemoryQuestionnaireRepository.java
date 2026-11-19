package com.example.survey.repository;

import com.example.survey.domain.Questionnaire;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 内存版问卷仓储（演示用）。 */
public final class InMemoryQuestionnaireRepository implements QuestionnaireRepository {

    private final Map<Long, Questionnaire> store = new ConcurrentHashMap<>();

    @Override
    public Questionnaire save(Questionnaire questionnaire) {
        store.put(questionnaire.getId(), questionnaire);
        return questionnaire;
    }

    @Override
    public Optional<Questionnaire> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Questionnaire> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparingLong(Questionnaire::getId))
                .toList();
    }
}
