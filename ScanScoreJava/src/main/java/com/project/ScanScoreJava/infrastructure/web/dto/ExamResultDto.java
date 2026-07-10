package com.project.ScanScoreJava.infrastructure.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ExamResultDto {

    private Map<Integer, Character> studentAnswers;

    private int totalQuestion;
    private int correct;
    private int incorrectAnswers;
    private int blankAnswers;
    private int voidedAnswers;

    private double score;

}
