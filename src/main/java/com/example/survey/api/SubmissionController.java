package com.example.survey.api;

import com.example.survey.common.Json;
import com.example.survey.common.TimeFormat;
import com.example.survey.domain.Answer;
import com.example.survey.domain.Submission;
import com.example.survey.service.SubmissionService;
import com.example.survey.transport.HttpResult;
import com.example.survey.transport.Router;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 答卷接口层：只做字段拆包，校验逻辑全在 SubmissionService。 */
public final class SubmissionController {

    private final SubmissionService service;

    public SubmissionController(SubmissionService service) {
        this.service = service;
    }

    public void register(Router router) {
        router.post("/api/questionnaires/{id}/responses", request -> {
            long questionnaireId = request.pathParamLong("id");
            Map<String, Object> body = request.jsonObject();
            String submitter = Json.str(body.get("submitter"));

            List<Answer> answers = new ArrayList<>();
            for (Object raw : Json.asArrayOrEmpty(body.get("answers"))) {
                Map<String, Object> node = Json.asObject(raw);
                long questionId = Json.asLong(node.get("questionId"), -1L);
                List<String> values = new ArrayList<>();
                for (Object value : Json.asArrayOrEmpty(node.get("values"))) {
                    if (value != null) {
                        values.add(String.valueOf(value));
                    }
                }
                answers.add(new Answer(questionId, values));
            }

            Submission submission = service.submit(questionnaireId, submitter, answers);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("submissionId", submission.id());
            data.put("questionnaireId", submission.questionnaireId());
            data.put("submitter", submission.submitter());
            data.put("submittedAt", TimeFormat.display(submission.submittedAt()));
            data.put("message", "提交成功，感谢参与！");
            return HttpResult.created(data);
        });
    }
}
