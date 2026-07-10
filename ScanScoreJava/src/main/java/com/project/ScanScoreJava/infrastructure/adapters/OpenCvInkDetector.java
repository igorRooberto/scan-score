package com.project.ScanScoreJava.infrastructure.adapters;

import com.project.ScanScoreJava.core.ports.InkDetector;
import com.project.ScanScoreJava.infrastructure.web.dto.DetectedBubbleDTO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.springframework.stereotype.Component;

import java.util.List;

import java.util.Map;
import java.util.TreeMap;

@Component
public class OpenCvInkDetector implements InkDetector {

    private final double FILL_INK_PERCENTAGE = 0.70;

    @Override
    public Map<Integer, Character> extractAnswer(Map<Integer, List<DetectedBubbleDTO>> grid, Mat binarizedImage) {

        Map<Integer, Character> studentAnswers = new TreeMap<>();

        grid.forEach((questionNumber, alternatives) -> {
            char answer = determineAnswerForQuestion(alternatives, binarizedImage);
            studentAnswers.put(questionNumber, answer);
        });

        return studentAnswers;
    }

    private char determineAnswerForQuestion(List<DetectedBubbleDTO> alternatives, Mat binarizedImage){
        int markedCount = 0;
        char lastMarkedLetter = '-';

        for (int i = 0; i < alternatives.size(); i++) {
            if (isBubbleMarked(alternatives.get(i), binarizedImage)) {
                markedCount++;
                lastMarkedLetter = (char) ('A' + i);
            }
        }

        return evaluateMarkingRules(markedCount, lastMarkedLetter);
    }

    private boolean isBubbleMarked(DetectedBubbleDTO bubble, Mat binarizedImage){
        Rect bubbleRect = new Rect(bubble.x(), bubble.y(), bubble.width(), bubble.height());
        Mat croppedBubble = new Mat(binarizedImage, bubbleRect);

        try {
            int whitePixels = Core.countNonZero(croppedBubble);
            int totalPixels = bubbleRect.width * bubbleRect.height;
            double fillPercentage = (double) whitePixels / totalPixels;

            return fillPercentage >= FILL_INK_PERCENTAGE;
        } finally {
            croppedBubble.release();
        }
    }

    private char evaluateMarkingRules(int markedCount, char lastMarkedLetter) {
        if (markedCount == 0) return '-'; // Blank
        if (markedCount > 1) return '*'; // Voided (multiple marks on the same question)
        return lastMarkedLetter;     // Valid answer
    }
}
