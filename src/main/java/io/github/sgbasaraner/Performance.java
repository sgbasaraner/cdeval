package io.github.sgbasaraner;

public class Performance {
    long winCount = 0;
    long lossCount = 0;
    long drawCount = 0;

    long getTotal() {
        return winCount + lossCount + drawCount;
    }

    Evaluation toEvaluation() {
        final var total = getTotal();
        if (total <= 0) {
            return new Evaluation(0D, 0D, 0);
        }
        return new Evaluation((double) winCount / total, (double) drawCount / total, total);
    }
}
