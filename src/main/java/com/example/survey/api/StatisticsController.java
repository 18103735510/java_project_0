package com.example.survey.api;

import com.example.survey.common.ApiException;
import com.example.survey.output.ReportFormat;
import com.example.survey.output.ReportFormatter;
import com.example.survey.service.StatisticsService;
import com.example.survey.statistics.StatisticsReport;
import com.example.survey.transport.HttpResult;
import com.example.survey.transport.Router;

/** 统计接口层：json / text / csv 三种输出共用同一份统计报告。 */
public final class StatisticsController {

    private final StatisticsService service;

    public StatisticsController(StatisticsService service) {
        this.service = service;
    }

    public void register(Router router) {
        router.get("/api/questionnaires/{id}/statistics", request -> {
            long questionnaireId = request.pathParamLong("id");
            StatisticsReport report = service.buildReport(questionnaireId);

            ReportFormat format;
            try {
                format = ReportFormat.parse(request.query("format", "json"));
            } catch (IllegalArgumentException e) {
                throw ApiException.badRequest(e.getMessage());
            }
            ReportFormatter formatter = format.formatter();
            return HttpResult.raw(formatter.contentType(), formatter.format(report));
        });
    }
}
