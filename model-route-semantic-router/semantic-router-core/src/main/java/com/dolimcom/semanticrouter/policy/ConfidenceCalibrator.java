package com.dolimcom.semanticrouter.policy;

public interface ConfidenceCalibrator {

    double calibrate(double rawScore, double margin);
}
