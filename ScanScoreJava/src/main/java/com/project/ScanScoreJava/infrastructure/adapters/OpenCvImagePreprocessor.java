package com.project.ScanScoreJava.infrastructure.adapters;

import com.project.ScanScoreJava.core.ports.ImagePreProcessor;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OpenCvImagePreprocessor implements ImagePreProcessor {

    @Override
    public Mat process(Mat sourceImage) {
        log.info("Starting image preprocessing with Adaptive Threshold...");

        Mat gray = new Mat();
        Mat blurred = new Mat();
        Mat binarized = new Mat();

        try {
            if (sourceImage == null || sourceImage.empty()) {
                throw new IllegalArgumentException("Source image cannot be null or empty.");
            }
            Imgproc.cvtColor(sourceImage, gray, Imgproc.COLOR_BGR2GRAY);

            Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);

            Imgproc.adaptiveThreshold(
                    blurred,
                    binarized,
                    255,
                    Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                    Imgproc.THRESH_BINARY_INV,
                    15,
                    10
            );

            log.info("Image preprocessing completed successfully.");
            return binarized;

        } catch (Exception e) {
            log.error("Error during image preprocessing: {}", e.getMessage(), e);

            binarized.release();
            throw new RuntimeException("Failed to preprocess the image using OpenCV", e);

        } finally {
            gray.release();
            blurred.release();
        }
    }
}

