package io.github.sgbasaraner;

public class Evaluation {
    final double winRate;
    final double drawRate;
    final long gameCount;

    public Evaluation(double winRate, double drawRate, long gameCount) {
        this.winRate = winRate;
        this.drawRate = drawRate;
        this.gameCount = gameCount;
    }
}
