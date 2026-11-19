package com.example.survey.repository;

import com.example.survey.domain.Questionnaire;

import java.util.List;
import java.util.Optional;

/** 问卷仓储：接口与实现分离，后续可无痛替换成 JDBC / 文件存储。 */
public interface QuestionnaireRepository {

    Questionnaire save(Questionnaire questionnaire);

    Optional<Questionnaire> findById(long id);

    List<Questionnaire> findAll();
}
