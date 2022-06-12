package io.github.sgbasaraner.cdevaldemo;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import io.github.sgbasaraner.Evaluation;
import io.github.sgbasaraner.Evaluator;
import io.github.sgbasaraner.Side;
import org.eclipse.collections.api.tuple.Pair;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class EvalService {
    private final Evaluator evaluator = new Evaluator(new ClassPathResource("data/sarp.pgn").getURL().getPath(), "sarpbasaraner");

    public EvalService() throws Exception {}

    public List<Pair<Move, Evaluation>> eval(Board board, Side side) throws SQLException {
        return evaluator.evaluateMoves(board, side);
    }
}

