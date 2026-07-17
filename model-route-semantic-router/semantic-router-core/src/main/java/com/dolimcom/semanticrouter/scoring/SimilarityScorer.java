package com.dolimcom.semanticrouter.scoring;

public interface SimilarityScorer {

    double score(double[] left, double[] right);
}
