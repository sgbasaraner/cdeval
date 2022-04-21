package io.github.sgbasaraner;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;

public class Evaluator {

    public static class Evaluation {
        final double winRate;
        final double drawRate;
        final long gameCount;

        public Evaluation(double winRate, double drawRate, long gameCount) {
            this.winRate = winRate;
            this.drawRate = drawRate;
            this.gameCount = gameCount;
        }
    }

    private static class Performance {
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
            return new Evaluation((double)winCount / total, (double)drawCount / total, total);
        }
    }

    // TODO: make separate maps for w vs b
    private final LongObjectHashMap<Performance> performanceMap = new LongObjectHashMap<>();

    private enum GameResult {
        WON, LOST, DRAW
    }

    public Evaluation evaluate(Board board) {
        return performanceMap.getIfAbsent(board.getZobristKey(), Performance::new).toEvaluation();
    }

    public Evaluator(String pgnFilePath, String playerName) throws Exception {
        PgnHolder pgn = new PgnHolder(pgnFilePath);
        pgn.loadPgn();
        for (Game game: pgn.getGames()) {
            final var variant = game.getProperty().get("Variant");
            if (variant != null && variant.equals("From Position")) {
                continue;
            }
            final boolean playerIsBlack;
            if (game.getWhitePlayer().getName().equals(playerName)) {
                playerIsBlack = false;
            } else if (game.getBlackPlayer().getName().equals(playerName)) {
                playerIsBlack = true;
            } else {
                continue;
            }

            final GameResult result;
            switch (game.getResult().value()) {
                case "WHITE_WON":
                    result = !playerIsBlack ? GameResult.WON : GameResult.LOST;
                    break;
                case "BLACK_WON":
                    result = playerIsBlack ? GameResult.WON : GameResult.LOST;
                    break;
                case "DRAW":
                    result = GameResult.DRAW;
                    break;
                default:
                    continue;
            }

            final MoveList moves = game.getHalfMoves();
            final Board board = new Board();
            for (Move move: moves) {
                try {
                    board.doMove(move);
                    final var currentPerformance = performanceMap.getIfAbsentPut(board.getIncrementalHashKey(), Performance::new);
                    switch (result) {
                        case WON:
                            currentPerformance.winCount++;
                            break;
                        case LOST:
                            currentPerformance.lossCount++;
                            break;
                        case DRAW:
                            currentPerformance.drawCount++;
                            break;
                    }
                } catch (Exception e) {
                    System.out.println("NPE in move " + move.toString());
                    break;
                }
            }
        }
    }
}
