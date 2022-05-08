package io.github.sgbasaraner;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;

import java.time.Instant;
import java.util.Arrays;

public class App {
    public static void main( String[] args ) {
        final var pgnFilePath = "data/sarp.pgn";
        PgnHolder pgn = new PgnHolder(pgnFilePath);
        try {
            final var startInstant = Instant.now();
            var evaluator = new Evaluator(pgnFilePath, "sarpbasaraner");
            final var loadFinished = Instant.now();
            pgn.loadPgn();
            final var evalStart = Instant.now();
            for (Game game: pgn.getGames()) {
                final var variant = game.getProperty().get("Variant");
                if (variant != null && variant.equals("From Position")) {
                    continue;
                }
                final Side playerSide = game.getWhitePlayer().getName().equals("sarpbasaraner") ? Side.WHITE : Side.BLACK;
                final MoveList moves = game.getHalfMoves();
                if (moves.size() >= 2) {
                    final Board board = new Board();
                    final var m1 = moves.getFirst();
                    board.doMove(m1);
                    final var m2 = moves.get(1);
                    board.doMove(m2);
                    final var eval = evaluator.evaluate(board, playerSide);
                    System.out.println("m1: " + m1.toString() + " m2: " + m2.toString() + "; wr: " + eval.winRate + "; w - " + game.getWhitePlayer().getName());
                }

            }
            final var evalFinished = Instant.now();
            System.out.println("Load took " + (loadFinished.toEpochMilli() - startInstant.toEpochMilli()) + " milliseconds.");
            System.out.println("Eval took " + (evalFinished.toEpochMilli() - evalStart.toEpochMilli()) + " milliseconds.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
