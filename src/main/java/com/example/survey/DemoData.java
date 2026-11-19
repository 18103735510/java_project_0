package com.example.survey;

import com.example.survey.common.Ids;
import com.example.survey.domain.Answer;
import com.example.survey.domain.Option;
import com.example.survey.domain.Question;
import com.example.survey.domain.QuestionType;
import com.example.survey.domain.Questionnaire;
import com.example.survey.service.QuestionnaireService;
import com.example.survey.service.SubmissionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 启动时灌入的演示数据：一份 10 道题的问卷 + 6 份模拟答卷。
 * 覆盖打分题、单选题、多选题、填空题，方便立刻看到统计效果。
 */
public final class DemoData {

    private DemoData() {
    }

    public static void seed(QuestionnaireService questionnaires, SubmissionService submissions) {
        if (!questionnaires.list().isEmpty()) {
            return;
        }
        Questionnaire questionnaire = questionnaires.create("产品体验调查（示例，10 题）", buildQuestions());
        questionnaires.publish(questionnaire.getId());
        Questionnaire published = questionnaires.get(questionnaire.getId());
        seedSubmissions(published, submissions);
        System.out.printf("[demo] 已生成示例问卷 #%d（%d 题）和 %d 份模拟答卷%n",
                published.getId(), published.getQuestions().size(), 6);
    }

    /* ------------------------------------------------------------------ 题目 */

    private static List<Question> buildQuestions() {
        List<Question> list = new ArrayList<>();

        list.add(new Question(Ids.nextQuestionId(), QuestionType.SCALE,
                "我对本系统的整体表现感到满意", List.of(), 1, 5));

        list.add(new Question(Ids.nextQuestionId(), QuestionType.SINGLE,
                "我最常使用系统的时段", options(
                "A", "上午 09:00 - 12:00",
                "B", "下午 12:00 - 18:00",
                "C", "晚上 18:00 - 23:00",
                "D", "时间不固定"), null, null));

        list.add(new Question(Ids.nextQuestionId(), QuestionType.MULTI,
                "我使用过下面哪些功能", options(
                "A", "问卷创建",
                "B", "题目管理",
                "C", "在线填写",
                "D", "统计汇总",
                "E", "结果导出"), null, null));

        list.add(new Question(Ids.nextQuestionId(), QuestionType.SINGLE,
                "我愿意把它推荐给同事", options(
                "A", "非常愿意",
                "B", "比较愿意",
                "C", "一般",
                "D", "不太愿意"), null, null));

        list.add(new Question(Ids.nextQuestionId(), QuestionType.TEXT,
                "我认为最需要改进的地方是", List.of(), null, null));

        list.add(new Question(Ids.nextQuestionId(), QuestionType.SCALE,
                "系统易用性评分（1 分最低，10 分最高）", List.of(), 1, 10));

        list.add(new Question(Ids.nextQuestionId(), QuestionType.SINGLE,
                "我的岗位角色", options(
                "A", "产品 / 设计",
                "B", "研发 / 测试",
                "C", "运营 / 市场",
                "D", "其他"), null, null));

        list.add(new Question(Ids.nextQuestionId(), QuestionType.MULTI,
                "希望优先增加的功能", options(
                "A", "图表可视化",
                "B", "问卷模板库",
                "C", "权限与协作",
                "D", "导出 Word / PDF",
                "E", "移动端适配"), null, null));

        list.add(new Question(Ids.nextQuestionId(), QuestionType.SCALE,
                "系统响应速度令我满意", List.of(), 1, 5));

        list.add(new Question(Ids.nextQuestionId(), QuestionType.TEXT,
                "其他建议", List.of(), null, null));

        return list;
    }

    private static List<Option> options(String... keyLabelPairs) {
        List<Option> options = new ArrayList<>(keyLabelPairs.length / 2);
        for (int i = 0; i + 1 < keyLabelPairs.length; i += 2) {
            options.add(new Option(keyLabelPairs[i], keyLabelPairs[i + 1]));
        }
        return options;
    }

    /* ------------------------------------------------------------------ 模拟答卷 */

    private static void seedSubmissions(Questionnaire questionnaire, SubmissionService submissions) {
        List<Question> questions = questionnaire.getQuestions();
        Question q1 = questions.get(0);
        Question q2 = questions.get(1);
        Question q3 = questions.get(2);
        Question q4 = questions.get(3);
        Question q5 = questions.get(4);
        Question q6 = questions.get(5);
        Question q7 = questions.get(6);
        Question q8 = questions.get(7);
        Question q9 = questions.get(8);
        Question q10 = questions.get(9);

        String[] names = {"张三", "李四", "王五", "赵六", "钱七", "孙八"};
        String[] improvements = {
                "统计报告如果能再直观一点就好了",
                "题目编辑的交互可以再顺一点",
                "希望可以按部门筛选答卷",
                "导出的格式还可以再丰富一些",
                "手机上填问卷有点挤",
                "整体不错，暂时没有别的建议"
        };
        String[] extras = {
                "希望增加图表看板",
                "暂时没有了",
                "希望支持匿名填写",
                "统计速度可以再快一点",
                "继续加油",
                "希望能支持多语言"
        };

        Random random = new Random(20261119L);
        for (int i = 0; i < names.length; i++) {
            List<Answer> answers = new ArrayList<>();
            answers.add(new Answer(q1.id(), List.of(String.valueOf(3 + random.nextInt(3)))));      // 3 ~ 5
            answers.add(new Answer(q2.id(), List.of(pick(random, "A", "B", "C", "D"))));
            answers.add(new Answer(q3.id(), pickMany(random, 1 + random.nextInt(3), "A", "B", "C", "D", "E")));
            answers.add(new Answer(q4.id(), List.of(pick(random, "A", "A", "B", "B", "C", "D"))));
            answers.add(new Answer(q5.id(), List.of(improvements[i])));
            answers.add(new Answer(q6.id(), List.of(String.valueOf(5 + random.nextInt(4)))));      // 5 ~ 8
            answers.add(new Answer(q7.id(), List.of(pick(random, "A", "B", "B", "C", "D"))));
            answers.add(new Answer(q8.id(), pickMany(random, 1 + random.nextInt(2), "A", "B", "C", "D", "E")));
            answers.add(new Answer(q9.id(), List.of(String.valueOf(2 + random.nextInt(4)))));      // 2 ~ 5
            answers.add(new Answer(q10.id(), List.of(extras[i])));

            submissions.submit(questionnaire.getId(), names[i], answers);
        }
    }

    private static String pick(Random random, String... keys) {
        return keys[random.nextInt(keys.length)];
    }

    private static List<String> pickMany(Random random, int count, String... keys) {
        List<String> pool = new ArrayList<>(List.of(keys));
        Collections.shuffle(pool, random);
        return new ArrayList<>(pool.subList(0, Math.min(count, pool.size())));
    }
}
