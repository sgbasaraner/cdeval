package io.github.sgbasaraner;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;

import java.sql.SQLException;

public class Evaluator {

    private final DataSource dataSource;

    public Evaluation evaluate(Board board, Side side) throws SQLException {
        return dataSource.query(board.getZobristKey(), side).toEvaluation();
    }

    public Evaluator(String pgnFilePath, String playerName) throws Exception {
        final var dataSource = DataSource.createDefault();
        dataSource.load(pgnFilePath, playerName);
        this.dataSource = dataSource;
    }
}
