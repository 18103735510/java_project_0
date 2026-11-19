package com.example.survey.statistics;

import com.example.survey.domain.Answer;
import com.example.survey.domain.Question;

/**
 * 单题累加器：接收一条条答案，最后产出该题的统计结果。
 * 不同题型对应不同实现，由 AccumulatorFactory 按题型分派。
 */
public interface Accumulator {

    /** 累积一份作答。 */
    void accept(Answer answer);

    /**
     * 产出统计结果。
     *
     * @param question      题目定义（用于取选项文案、分值区间）
     * @param responseCount 该题的答题人数（分布比例的分母）
     */
    QuestionStat result(Question question, int responseCount);
}
