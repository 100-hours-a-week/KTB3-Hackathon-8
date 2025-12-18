package com.ktb.restaurant.google.rating;

public final class BayesianRatingCalculator implements RatingCalculator {

    private final double priorMean; // C
    private final int priorWeight;  // m

    public BayesianRatingCalculator(double priorMean, int priorWeight) {
        if (priorMean < 0.0 || priorMean > 5.0) throw new IllegalArgumentException("priorMean must be 0..5");
        if (priorWeight <= 0) throw new IllegalArgumentException("priorWeight must be positive");
        this.priorMean = priorMean;
        this.priorWeight = priorWeight;
    }

    @Override
    public double calcRating(Double rating, Integer userRatingCount) {
        if (rating == null || userRatingCount == null) return priorMean;

        double r = clamp(rating, 0.0, 5.0);
        int v = Math.max(userRatingCount, 0);

        double w = (double) v / (v + priorWeight);
        return w * r + (1.0 - w) * priorMean;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
