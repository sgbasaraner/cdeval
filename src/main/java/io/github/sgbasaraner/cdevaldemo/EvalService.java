package io.github.sgbasaraner.cdevaldemo;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import io.github.sgbasaraner.Evaluation;
import io.github.sgbasaraner.Evaluator;
import io.github.sgbasaraner.Side;
import org.eclipse.collections.api.tuple.Pair;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class EvalService {
//    private final Evaluator evaluator = new Evaluator("data/sarp.pgn", "sarpbasaraner");

    public EvalService() throws Exception {}

    public List<Pair<Move, Evaluation>> eval(Board board, Side side) throws SQLException {
        return null;
//        return evaluator.evaluateMoves(board, side);
    }
}

