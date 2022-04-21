package io.github.sgbasaraner;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;

import java.util.Arrays;

public class App {
    public static void main( String[] args ) {
        final var pgnFilePath = "data/sarp.pgn";
        PgnHolder pgn = new PgnHolder(pgnFilePath);
        try {
            var evaluator = new Evaluator(pgnFilePath, "sarpbasaraner");
            pgn.loadPgn();
            for (Game game: pgn.getGames()) {
                final var variant = game.getProperty().get("Variant");
                if (variant != null && variant.equals("From Position")) {
                    continue;
                }
                final MoveList moves = game.getHalfMoves();
                if (moves.size() >= 2) {
                    final Board board = new Board();
                    final var m1 = moves.getFirst();
                    board.doMove(m1);
                    final var m2 = moves.get(1);
                    board.doMove(m2);
                    final var eval = evaluator.evaluate(board);
                    System.out.println("m1: " + m1.toString() + " m2: " + m2.toString() + "; wr: " + eval.winRate + "; w - " + game.getWhitePlayer().getName());
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
