package com.project.ScanScoreJava.infrastructure.adapters;

import com.project.ScanScoreJava.core.ports.ImageExtractor;
import com.project.ScanScoreJava.infrastructure.web.dto.DetectedBubbleDTO;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class OpenCvBubbleExtractor implements ImageExtractor {

    @Override
    public List<DetectedBubbleDTO> extractBubble(Mat sourceImage) {
               log.info("Starting bubble extraction from the image...");

               List<DetectedBubbleDTO> detectedBubbles = new ArrayList<>();
               List<MatOfPoint> contours = new ArrayList<>();
               Mat hierarchy = new Mat();

               Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
               Mat morphedImage = new Mat();

               try{
               Imgproc.morphologyEx(sourceImage, morphedImage, Imgproc.MORPH_CLOSE, kernel);

               Imgproc.findContours(
                       morphedImage,
                       contours,
                       hierarchy,
                       Imgproc.RETR_EXTERNAL,
                       Imgproc.CHAIN_APPROX_SIMPLE
               );

               log.info("Total raw contours found: {}", contours.size());

               for (MatOfPoint contour : contours) {

                   Rect boundingRect = Imgproc.boundingRect(contour);
                   float aspectRatio = (float) boundingRect.width / (float) boundingRect.height;

                   double area = Imgproc.contourArea(contour);

                   double boundingBoxArea = boundingRect.width * boundingRect.height;
                   double extent = area / boundingBoxArea;

                   boolean isProportional = aspectRatio >= 0.8f && aspectRatio <= 1.2f;
                   boolean isLargeEnough = boundingRect.width >= 15 && boundingRect.height >= 15;
                   boolean isNotTooLarge = boundingRect.width <= 80 && boundingRect.height <= 80;
                   boolean isCircleShape = extent >= 0.60 && extent <= 0.88;

                   if (isProportional && isLargeEnough && isNotTooLarge && isCircleShape) {
                       detectedBubbles.add(new DetectedBubbleDTO(
                               boundingRect.x,
                               boundingRect.y,
                               boundingRect.width,
                               boundingRect.height
                       ));
                   }
               }

               log.info("Total bubbles detected after filtering: {}", detectedBubbles.size());
               return detectedBubbles;

           }catch (Exception e) {
               log.error("Error during bubble extraction: {}", e.getMessage(), e);
               throw new RuntimeException("Failed to extract bubbles using OpenCV", e);
           }
           finally {
               morphedImage.release();
               kernel.release();
               hierarchy.release();

               for (MatOfPoint contour : contours) {
                   if (contour != null) {
                           contour.release();
                   }
               }
               contours.clear();
           }
    }

}


