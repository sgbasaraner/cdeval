package io.github.sgbasaraner.cdevaldemo;

public class EvalResponse {
    private final String move;
    private final double winRate;
    private final double drawRate;
    private final long gameCount;

    public EvalResponse(String move, double winRate, double drawRate, long gameCount) {
        this.move = move;
        this.winRate = winRate;
        this.drawRate = drawRate;
        this.gameCount = gameCount;
    }

    public String getMove() {
        return move;
    }

    public double getWinRate() {
        return winRate;
    }

    public double getDrawRate() {
        return drawRate;
    }

    public long getGameCount() {
        return gameCount;
    }
}
