package io.github.sgbasaraner.cdevaldemo;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import io.github.sgbasaraner.Evaluation;
import io.github.sgbasaraner.Side;
import org.eclipse.collections.api.tuple.Pair;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class EvalController {
    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    @GetMapping("/index")
    public String showUserList(Model model) {
        return "index";
    }

    private final EvalService service;

    public EvalController(EvalService service) {
        this.service = service;
    }

    @GetMapping("/evaluateMoves")
    public List<EvalResponse> getMoves(@RequestParam String fen, @RequestParam String side) {
        final var board = new Board();
        board.loadFromFen(fen);
        try {
            return service.eval(board, side.equalsIgnoreCase("white") ? Side.WHITE : Side.BLACK)
                    .parallelStream()
                    .map(this::mapToEvalResponse)
                    .collect(Collectors.toUnmodifiableList());
        } catch (SQLException e) {
            throw new RuntimeException("db error");
        }
    }

    private EvalResponse mapToEvalResponse(Pair<Move, Evaluation> pair) {
        return new EvalResponse(pair.getOne().toString(), pair.getTwo().getWinRate(), pair.getTwo().getDrawRate(), pair.getTwo().getGameCount());
    }
}
