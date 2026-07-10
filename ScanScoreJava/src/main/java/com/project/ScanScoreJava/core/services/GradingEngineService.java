package com.project.ScanScoreJava.core.services;

import com.project.ScanScoreJava.infrastructure.web.dto.ExamResultDto;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GradingEngineService {

    public ExamResultDto grade(Map<Integer, Character> studentAnswers, Map<Integer, Character> officialKey) {

        if (studentAnswers == null || studentAnswers.isEmpty()) {
            throw new IllegalArgumentException("Student answers cannot be null or empty.");
        }

        if (officialKey == null || officialKey.isEmpty()) {
            throw new IllegalArgumentException("Official answer key cannot be null or empty.");
        }

        int totalQuestions = officialKey.size();
        int correct = 0;
        int incorrectAnswers = 0;
        int blankAnswers = 0;
        int voidedAnswers = 0;

        for (Map.Entry<Integer, Character> entry : officialKey.entrySet()) {
            Integer questionNumber = entry.getKey();
            Character expectedAnswer = entry.getValue();

            Character actualAnswer = studentAnswers.getOrDefault(questionNumber, '-');

            switch (actualAnswer) {
                case '-' -> blankAnswers++;
                case '*' -> voidedAnswers++;
                default -> {
                    if (actualAnswer.equals(expectedAnswer)) {
                        correct++;
                    } else {
                        incorrectAnswers++;
                    }
                }

            }
        }

        double score = ((double) correct / totalQuestions) * 10.0;
        double finalScore = Math.round(score * 100.0) / 100.0;

        return ExamResultDto.builder()
                .studentAnswers(studentAnswers)
                .totalQuestion(totalQuestions)
                .correct(correct)
                .incorrectAnswers(incorrectAnswers)
                .blankAnswers(blankAnswers)
                .voidedAnswers(voidedAnswers)
                .score(finalScore)
                .build();

    }
}