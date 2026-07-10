package com.project.ScanScoreJava.core.ports;

import com.project.ScanScoreJava.infrastructure.web.dto.DetectedBubbleDTO;
import org.opencv.core.Mat;
import java.util.List;

public interface ImageExtractor {

    List<DetectedBubbleDTO> extractBubble(Mat sourceImage);
}
