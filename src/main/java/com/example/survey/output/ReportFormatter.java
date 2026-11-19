package com.example.survey.output;

import com.example.survey.statistics.StatisticsReport;

/** 报告输出器接口：新增一种格式只需实现它并注册到 ReportFormat。 */
public interface ReportFormatter {

    String contentType();

    String format(StatisticsReport report);
}
