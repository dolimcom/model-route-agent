package com.dolimcom.semanticrouter.policy;

public class HeuristicConfidenceCalibrator implements ConfidenceCalibrator {

    @Override
    public double calibrate(double rawScore, double margin) {
        double normalizedScore = Math.max(0.0d, Math.min(1.0d, (rawScore + 1.0d) / 2.0d));
        double normalizedMargin = Math.max(0.0d, Math.min(1.0d, margin + 0.5d));
        return Math.max(0.0d, Math.min(1.0d, (normalizedScore * 0.7d) + (normalizedMargin * 0.3d)));
    }
}
