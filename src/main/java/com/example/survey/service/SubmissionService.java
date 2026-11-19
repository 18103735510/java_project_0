package com.example.survey.service;

import com.example.survey.common.ApiException;
import com.example.survey.common.Ids;
import com.example.survey.domain.Answer;
import com.example.survey.domain.Question;
import com.example.survey.domain.Questionnaire;
import com.example.survey.domain.Status;
import com.example.survey.domain.Submission;
import com.example.survey.repository.QuestionnaireRepository;
import com.example.survey.repository.SubmissionRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** 答卷应用服务：状态校验、重复提交拦截、按题型规范化答案。 */
public final class SubmissionService {

    private static final int MAX_TEXT_LENGTH = 500;
    private static final int MAX_SUBMITTER_LENGTH = 32;

    private final QuestionnaireRepository questionnaireRepository;
    private final SubmissionRepository submissionRepository;

    public SubmissionService(QuestionnaireRepository questionnaireRepository,
                             SubmissionRepository submissionRepository) {
        this.questionnaireRepository = questionnaireRepository;
        this.submissionRepository = submissionRepository;
    }

    public Submission submit(long questionnaireId, String submitter, List<Answer> answers) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> ApiException.notFound("问卷不存在：" + questionnaireId));
        if (questionnaire.getStatus() != Status.PUBLISHED) {
            throw ApiException.conflict("问卷当前状态为「" + questionnaire.getStatus().label() + "」，暂不接受填写");
        }

        String safeSubmitter = submitter == null ? "" : submitter.trim();
        if (safeSubmitter.isEmpty()) {
            throw ApiException.badRequest("请填写昵称");
        }
        if (safeSubmitter.length() > MAX_SUBMITTER_LENGTH) {
            throw ApiException.badRequest("昵称不能超过 " + MAX_SUBMITTER_LENGTH + " 个字符");
        }
        if (submissionRepository.existsBySubmitter(questionnaireId, safeSubmitter)) {
            throw ApiException.conflict("昵称「" + safeSubmitter + "」已经提交过本问卷了");
        }

        Map<Long, Answer> byQuestion = new HashMap<>();
        if (answers != null) {
            for (Answer answer : answers) {
                byQuestion.put(answer.questionId(), answer);
            }
        }

        List<Answer> normalized = new ArrayList<>(questionnaire.getQuestions().size());
        for (Question question : questionnaire.getQuestions()) {
            Answer answer = byQuestion.get(question.id());
            if (answer == null) {
                throw ApiException.badRequest("题目未作答：" + question.title());
            }
            normalized.add(new Answer(question.id(), normalize(question, answer.values())));
        }

        Submission submission = new Submission(Ids.nextSubmissionId(), questionnaireId,
                safeSubmitter, Instant.now(), normalized);
        return submissionRepository.save(submission);
    }

    private List<String> normalize(Question question, List<String> rawValues) {
        List<String> values = new ArrayList<>();
        if (rawValues != null) {
            for (String raw : rawValues) {
                if (raw == null) {
                    continue;
                }
                String trimmed = raw.trim();
                if (!trimmed.isEmpty()) {
                    values.add(trimmed);
                }
            }
        }

        String title = question.title();
        switch (question.type()) {
            case SINGLE -> {
                if (values.size() != 1) {
                    throw ApiException.badRequest("题目「" + title + "」只能选择一个选项");
                }
                if (!question.hasOption(values.get(0))) {
                    throw ApiException.badRequest("题目「" + title + "」的选项无效：" + values.get(0));
                }
            }
            case MULTI -> {
                if (values.isEmpty()) {
                    throw ApiException.badRequest("题目「" + title + "」至少选择一个选项");
                }
                LinkedHashSet<String> unique = new LinkedHashSet<>();
                for (String value : values) {
                    if (!question.hasOption(value)) {
                        throw ApiException.badRequest("题目「" + title + "」的选项无效：" + value);
                    }
                    unique.add(value);
                }
                values = new ArrayList<>(unique);
            }
            case SCALE -> {
                if (values.size() != 1) {
                    throw ApiException.badRequest("题目「" + title + "」需要填写一个分值");
                }
                int score;
                try {
                    score = Integer.parseInt(values.get(0));
                } catch (NumberFormatException e) {
                    throw ApiException.badRequest("题目「" + title + "」的分值必须是整数");
                }
                int min = question.min() == null ? 0 : question.min();
                int max = question.max() == null ? 10 : question.max();
                if (score < min || score > max) {
                    throw ApiException.badRequest("题目「" + title + "」的分值需在 " + min + " ~ " + max + " 之间");
                }
                values = List.of(String.valueOf(score));
            }
            case TEXT -> {
                if (values.isEmpty()) {
                    throw ApiException.badRequest("题目「" + title + "」不能为空");
                }
                String text = String.join(" ", values);
                if (text.length() > MAX_TEXT_LENGTH) {
                    throw ApiException.badRequest("题目「" + title + "」的内容不能超过 " + MAX_TEXT_LENGTH + " 字");
                }
                values = List.of(text);
            }
        }
        return values;
    }
}
