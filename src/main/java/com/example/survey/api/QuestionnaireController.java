package com.example.survey.api;

import com.example.survey.common.ApiException;
import com.example.survey.common.Ids;
import com.example.survey.common.Json;
import com.example.survey.common.TimeFormat;
import com.example.survey.domain.Option;
import com.example.survey.domain.Question;
import com.example.survey.domain.QuestionType;
import com.example.survey.domain.Questionnaire;
import com.example.survey.service.QuestionnaireService;
import com.example.survey.transport.HttpResult;
import com.example.survey.transport.Router;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 问卷接口层：路由 + DTO 转换，业务规则全部下沉到 service。 */
public final class QuestionnaireController {

    private final QuestionnaireService service;

    public QuestionnaireController(QuestionnaireService service) {
        this.service = service;
    }

    public void register(Router router) {
        router.get("/api/questionnaires", request -> HttpResult.data(summaries(service.list())));

        router.post("/api/questionnaires", request -> {
            Map<String, Object> body = request.jsonObject();
            String title = Json.str(body.get("title"));
            List<Question> questions = parseQuestions(Json.asArrayOrEmpty(body.get("questions")));
            return HttpResult.created(detail(service.create(title, questions)));
        });

        router.get("/api/questionnaires/{id}",
                request -> HttpResult.data(detail(service.get(request.pathParamLong("id")))));

        router.post("/api/questionnaires/{id}/publish",
                request -> HttpResult.data(detail(service.publish(request.pathParamLong("id")))));

        router.post("/api/questionnaires/{id}/close",
                request -> HttpResult.data(detail(service.close(request.pathParamLong("id")))));
    }

    /* ---------------------------------------------------------------- DTO */

    private static List<Object> summaries(List<Questionnaire> questionnaires) {
        List<Object> items = new ArrayList<>(questionnaires.size());
        for (Questionnaire questionnaire : questionnaires) {
            items.add(summary(questionnaire));
        }
        return items;
    }

    private static Map<String, Object> summary(Questionnaire questionnaire) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", questionnaire.getId());
        item.put("title", questionnaire.getTitle());
        item.put("status", questionnaire.getStatus().name());
        item.put("statusLabel", questionnaire.getStatus().label());
        item.put("questionCount", questionnaire.getQuestions().size());
        item.put("createdAt", TimeFormat.display(questionnaire.getCreatedAt()));
        return item;
    }

    private static Map<String, Object> detail(Questionnaire questionnaire) {
        Map<String, Object> item = summary(questionnaire);
        List<Object> questions = new ArrayList<>(questionnaire.getQuestions().size());
        for (Question question : questionnaire.getQuestions()) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", question.id());
            node.put("type", question.type().name());
            node.put("typeLabel", question.type().label());
            node.put("title", question.title());
            if (question.min() != null) {
                node.put("min", question.min());
            }
            if (question.max() != null) {
                node.put("max", question.max());
            }
            List<Object> options = new ArrayList<>(question.options().size());
            for (Option option : question.options()) {
                Map<String, Object> optionNode = new LinkedHashMap<>();
                optionNode.put("key", option.key());
                optionNode.put("label", option.label());
                options.add(optionNode);
            }
            node.put("options", options);
            questions.add(node);
        }
        item.put("questions", questions);
        return item;
    }

    /* ---------------------------------------------------------------- 请求体解析 */

    private static List<Question> parseQuestions(List<Object> rawQuestions) {
        List<Question> questions = new ArrayList<>(rawQuestions.size());
        int index = 0;
        for (Object raw : rawQuestions) {
            index++;
            if (!(raw instanceof Map<?, ?>)) {
                throw ApiException.badRequest("第 " + index + " 题格式不正确");
            }
            Map<String, Object> spec = Json.asObject(raw);

            QuestionType type;
            try {
                type = QuestionType.parse(Json.str(spec.get("type")));
            } catch (IllegalArgumentException e) {
                throw ApiException.badRequest("第 " + index + " 题：" + e.getMessage());
            }

            List<Option> options = new ArrayList<>();
            for (Object rawOption : Json.asArrayOrEmpty(spec.get("options"))) {
                if (!(rawOption instanceof Map<?, ?>)) {
                    throw ApiException.badRequest("第 " + index + " 题的选项格式不正确");
                }
                Map<String, Object> optionSpec = Json.asObject(rawOption);
                options.add(new Option(Json.str(optionSpec.get("key")), Json.str(optionSpec.get("label"))));
            }

            Integer min;
            Integer max;
            try {
                min = Json.intOrNull(spec.get("min"));
                max = Json.intOrNull(spec.get("max"));
            } catch (IllegalArgumentException e) {
                throw ApiException.badRequest("第 " + index + " 题：" + e.getMessage());
            }

            questions.add(new Question(Ids.nextQuestionId(), type,
                    Json.str(spec.get("title")).trim(), options, min, max));
        }
        return questions;
    }
}
