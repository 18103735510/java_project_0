package com.example.survey.service;

import com.example.survey.common.ApiException;
import com.example.survey.common.Ids;
import com.example.survey.domain.Option;
import com.example.survey.domain.Question;
import com.example.survey.domain.Questionnaire;
import com.example.survey.domain.Status;
import com.example.survey.repository.QuestionnaireRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 问卷应用服务：创建、查询、发布、结束，以及题目定义的合法性校验。 */
public final class QuestionnaireService {

    private final QuestionnaireRepository repository;

    public QuestionnaireService(QuestionnaireRepository repository) {
        this.repository = repository;
    }

    public List<Questionnaire> list() {
        return repository.findAll();
    }

    public Questionnaire get(long id) {
        return repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("问卷不存在：" + id));
    }

    public Questionnaire create(String title, List<Question> questions) {
        String safeTitle = title == null ? "" : title.trim();
        if (safeTitle.isEmpty()) {
            throw ApiException.badRequest("问卷标题不能为空");
        }
        if (safeTitle.length() > 100) {
            throw ApiException.badRequest("问卷标题不能超过 100 个字符");
        }
        if (questions == null || questions.isEmpty()) {
            throw ApiException.badRequest("问卷至少需要一道题目");
        }
        validateQuestions(questions);

        Questionnaire questionnaire = new Questionnaire(
                Ids.nextQuestionnaireId(), safeTitle, Instant.now(), Status.DRAFT, questions);
        return repository.save(questionnaire);
    }

    public Questionnaire publish(long id) {
        Questionnaire questionnaire = get(id);
        if (questionnaire.getStatus() == Status.CLOSED) {
            throw ApiException.conflict("问卷已结束，不能重新发布");
        }
        questionnaire.markPublished();
        return repository.save(questionnaire);
    }

    public Questionnaire close(long id) {
        Questionnaire questionnaire = get(id);
        if (questionnaire.getStatus() == Status.CLOSED) {
            return questionnaire;
        }
        questionnaire.markClosed();
        return repository.save(questionnaire);
    }

    private static void validateQuestions(List<Question> questions) {
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            String prefix = "第 " + (i + 1) + " 题：";
            if (question.title() == null || question.title().isBlank()) {
                throw ApiException.badRequest(prefix + "题目内容不能为空");
            }
            switch (question.type()) {
                case SINGLE, MULTI -> {
                    if (question.options().size() < 2) {
                        throw ApiException.badRequest(prefix + "选择题至少需要 2 个选项");
                    }
                    Set<String> keys = new HashSet<>();
                    for (Option option : question.options()) {
                        if (option.key().isEmpty()) {
                            throw ApiException.badRequest(prefix + "选项 key 不能为空");
                        }
                        if (!keys.add(option.key())) {
                            throw ApiException.badRequest(prefix + "选项 key 重复：" + option.key());
                        }
                        if (option.label().isEmpty()) {
                            throw ApiException.badRequest(prefix + "选项内容不能为空");
                        }
                    }
                }
                case SCALE -> {
                    if (question.min() == null || question.max() == null) {
                        throw ApiException.badRequest(prefix + "打分题必须设置 min / max");
                    }
                    if (question.min() >= question.max()) {
                        throw ApiException.badRequest(prefix + "打分题的 max 必须大于 min");
                    }
                    if (question.max() - question.min() > 100) {
                        throw ApiException.badRequest(prefix + "打分题的区间不宜超过 100");
                    }
                }
                case TEXT -> {
                    // 填空题不需要额外配置
                }
            }
        }
    }
}
