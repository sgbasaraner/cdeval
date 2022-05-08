package io.github.sgbasaraner;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.eclipse.collections.impl.tuple.Tuples;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Evaluator {

    private final DataSource dataSource;

    public Evaluation evaluate(Board board, Side side) throws SQLException {
        return dataSource.query(board.getZobristKey(), side).toEvaluation();
    }

    public Evaluation evaluate(Board board, Side side, int ratingGt, int ratingLt) throws SQLException {
        return dataSource.query(board.getZobristKey(), side, ratingGt, ratingLt).toEvaluation();
    }

    public List<Pair<Move, Evaluation>> evaluateMoves(Board board, Side side) {
        assert board.getSideToMove().value().equals("WHITE") ? side.equals(Side.WHITE) : side.equals(Side.BLACK);

        final var possibleMoves = board.legalMoves();


        return possibleMoves
                .parallelStream()
                .map(move -> {
                    final var clonedBoard = board.clone();
                    clonedBoard.doMove(move);
                    try {
                        final var r = Tuples.pair(move, evaluate(clonedBoard, side));
                        if (r.getTwo().gameCount == 0) {
                            return null;
                        }
                        return r;
                    } catch (SQLException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(p -> ((Pair<Move, Evaluation>)p).getTwo().winRate)
                        .reversed()
                        .thenComparing(p -> ((Pair<Move, Evaluation>)p).getTwo().gameCount))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<Pair<Move, Evaluation>> evaluateMoves(Board board, Side side, int ratingGt, int ratingLt) {
        assert board.getSideToMove().value().equals("WHITE") ? side.equals(Side.WHITE) : side.equals(Side.BLACK);

        final var possibleMoves = board.legalMoves();

        return possibleMoves
                .parallelStream()
                .map(move -> {
                    final var clonedBoard = board.clone();
                    clonedBoard.doMove(move);
                    try {
                        final var r = Tuples.pair(move, evaluate(clonedBoard, side, ratingGt, ratingLt));
                        if (r.getTwo().gameCount == 0) {
                            return null;
                        }
                        return r;
                    } catch (SQLException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(p -> ((Pair<Move, Evaluation>)p).getTwo().winRate)
                        .reversed()
                        .thenComparing(p -> ((Pair<Move, Evaluation>)p).getTwo().gameCount))
                .collect(Collectors.toUnmodifiableList());
    }

    public Evaluator(String pgnFilePath, String playerName) throws Exception {
        final var dataSource = DataSource.createDefault();
        dataSource.load(pgnFilePath, playerName);
        this.dataSource = dataSource;
    }

    public Evaluator(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
