package com.example.survey.statistics;

import com.example.survey.domain.QuestionType;

/** 按题型分派累加器：新增题型时只需要在这里加一个分支和一个实现类。 */
public final class AccumulatorFactory {

    private AccumulatorFactory() {
    }

    public static Accumulator of(QuestionType type) {
        return switch (type) {
            case SCALE -> new NumericAccumulator();
            case SINGLE, MULTI -> new ChoiceAccumulator();
            case TEXT -> new TextAccumulator();
        };
    }
}
