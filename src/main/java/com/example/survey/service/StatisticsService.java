package com.example.survey.service;

import com.example.survey.common.ApiException;
import com.example.survey.domain.Answer;
import com.example.survey.domain.Question;
import com.example.survey.domain.Questionnaire;
import com.example.survey.domain.Submission;
import com.example.survey.repository.QuestionnaireRepository;
import com.example.survey.repository.SubmissionRepository;
import com.example.survey.statistics.Accumulator;
import com.example.survey.statistics.AccumulatorFactory;
import com.example.survey.statistics.Grouper;
import com.example.survey.statistics.QuestionStat;
import com.example.survey.statistics.StatisticsReport;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统计服务：编排「取样 -> Map 分组 -> 单趟遍历汇总 -> 装配报告」。
 * 没有数据时同样能出报告（除零保护在 Accumulator / ZeroGuard 里）。
 */
public final class StatisticsService {

    private final QuestionnaireRepository questionnaireRepository;
    private final SubmissionRepository submissionRepository;

    public StatisticsService(QuestionnaireRepository questionnaireRepository,
                             SubmissionRepository submissionRepository) {
        this.questionnaireRepository = questionnaireRepository;
        this.submissionRepository = submissionRepository;
    }

    public StatisticsReport buildReport(long questionnaireId) {
        Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> ApiException.notFound("问卷不存在：" + questionnaireId));

        // 1. 取样：仓储返回的是快照，统计期间不会被新答卷干扰
        List<Submission> submissions = submissionRepository.findByQuestionnaireId(questionnaireId);

        // 2. Map 分组：题目 ID -> 该题全部作答
        Map<Long, List<Answer>> grouped = Grouper.byQuestion(submissions);

        // 3. 单趟遍历汇总：每道题只扫一遍自己的答案
        List<QuestionStat> stats = new ArrayList<>(questionnaire.getQuestions().size());
        for (Question question : questionnaire.getQuestions()) {
            List<Answer> answers = grouped.getOrDefault(question.id(), List.of());
            Accumulator accumulator = AccumulatorFactory.of(question.type());
            for (Answer answer : answers) {
                accumulator.accept(answer);
            }
            stats.add(accumulator.result(question, answers.size()));
        }

        // 4. 装配报告
        return new StatisticsReport(questionnaire.getId(), questionnaire.getTitle(),
                submissions.size(), Instant.now(), stats);
    }
}
