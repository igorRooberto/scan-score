package com.project.ScanScoreJava.core.ports;

import org.opencv.core.Mat;

public interface ImagePreProcessor {

    Mat process(Mat sourceImage);
}
