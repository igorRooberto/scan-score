package com.project.ScanScoreJava.core.ports;

import com.project.ScanScoreJava.infrastructure.web.dto.DetectedBubbleDTO;
import org.opencv.core.Mat;

import java.util.List;
import java.util.Map;

public interface InkDetector {

    Map<Integer, Character> extractAnswer(Map<Integer, List<DetectedBubbleDTO>> grid, Mat binarizedImage);
}
