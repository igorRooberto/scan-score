package com.project.ScanScoreJava.core.usecases;

import com.project.ScanScoreJava.core.ports.ImageExtractor;
import com.project.ScanScoreJava.core.ports.ImagePreProcessor;
import com.project.ScanScoreJava.core.ports.InkDetector;
import com.project.ScanScoreJava.core.services.BubbleGridOrganizer;
import com.project.ScanScoreJava.core.services.GradingEngineService;
import com.project.ScanScoreJava.infrastructure.web.dto.DetectedBubbleDTO;
import com.project.ScanScoreJava.infrastructure.web.dto.ExamResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradExamUseCase {

    private final ImagePreProcessor preProcessor;
    private final ImageExtractor bubbleExtractor;
    private final BubbleGridOrganizer gridOrganizer;
    private final InkDetector inkDetector;
    private final GradingEngineService gradingEngine;

    public ExamResultDto execute(byte[] imageBytes, Map<Integer, Character> officialKey) {
        log.info("Starting exam grading use case...");

        Mat sourceImage = new Mat();
        Mat binarizedImage = new Mat();

        try {
            sourceImage = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);
            if (sourceImage.empty()) {
                throw new IllegalArgumentException("The provided image is empty or corrupted.");
            }

            log.info("Applying image preprocessing...");
            binarizedImage = preProcessor.process(sourceImage);

            log.info("Extracting answer bubbles from image...");
            List<DetectedBubbleDTO> rawBubbles = bubbleExtractor.extractBubble(binarizedImage);

            if (rawBubbles.isEmpty()) {
                throw new IllegalStateException("No bubbles detected in the image. Please check the photo quality.");
            }

            log.info("Organizing detected bubbles into Y/X grid...");
            Map<Integer, List<DetectedBubbleDTO>> grid = gridOrganizer.organize(rawBubbles);

            log.info("Analyzing bubble fill levels...");
            Map<Integer, Character> studentAnswers = inkDetector.extractAnswer(grid, binarizedImage);

            log.info("Calculating final score using the official answer key...");
            return gradingEngine.grade(studentAnswers, officialKey);

        } finally {
            if (sourceImage != null && !sourceImage.empty()) {
                sourceImage.release();
            }
            if (binarizedImage != null && !binarizedImage.empty()) {
                binarizedImage.release();
            }
        }
    }
}
