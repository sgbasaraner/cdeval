package io.github.sgbasaraner;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class InMemoryDataSource implements DataSource {

    private final JdbcConnectionPool connectionPool;

    private static final String JDBC_URL = "jdbc:h2:mem:cdeval_inmemorydatasource";

    public InMemoryDataSource() throws SQLException {
        this.connectionPool = JdbcConnectionPool.create(JDBC_URL, null, null);
        this.connectionPool.setMaxConnections(Runtime.getRuntime().availableProcessors());

        final var sql = "" +
                "Create table performance (" +
                "position long not null, " +
                "rating_diff int not null, " +
                "is_black boolean not null, " +
                "outcome int not null); " +
                "CREATE INDEX position_idx on performance(position); " +
                "CREATE INDEX rating_diff_idx on performance(rating_diff); " +
                "CREATE INDEX is_black_idx on performance(is_black); " +
                "CREATE INDEX outcome_idx on performance(outcome); ";


        useStatement(statement -> {
            try {
                statement.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException();
            }
        });
    }

    private void executeInsert(long key, int ratingDiff, Side side, String result) throws SQLException {
        final var sql = String.format("Insert into performance (position, rating_diff, is_black, outcome) values (%s, %s, %s, %s)",
                key,
                ratingDiff,
                side.equals(Side.BLACK) ? "TRUE" : "FALSE",
                result
        );

        useStatement(statement -> {
            try {
                statement.executeUpdate(sql);
            } catch (SQLException e) {
                throw new RuntimeException();
            }
        });
    }

    private void useStatement(Consumer<Statement> fn) throws SQLException {
        final var conn = connectionPool.getConnection();

        final var stmt = conn.createStatement();

        fn.accept(stmt);

        conn.close();
    }

    private <R> R useStatement (Function<Statement, R> fn) throws SQLException {
        final var conn = connectionPool.getConnection();

        final var stmt = conn.createStatement();

        final var r = fn.apply(stmt);

        conn.close();

        return r;
    }


    @Override
    public void saveWin(long key, int ratingDiff, Side side) throws SQLException {
        executeInsert(key, ratingDiff, side, "1");
    }

    @Override
    public void saveLoss(long key, int ratingDiff, Side side) throws SQLException {
        executeInsert(key, ratingDiff, side, "-1");
    }

    @Override
    public void saveDraw(long position, int ratingDiff, Side side) throws SQLException {
        executeInsert(position, ratingDiff, side, "0");
    }

    private Performance mapToPerformance(ResultSet resultSet) throws SQLException {
        final var perf = new Performance();
        while (resultSet.next()) {
            int outcome = resultSet.getInt("outcome");
            if (outcome == 0) {
                perf.drawCount++;
            } else if (outcome == -1) {
                perf.lossCount++;
            } else {
                perf.winCount++;
            }
        }

        return perf;
    }

    @Override
    public Performance query(long key, Side side, int ratingDiffGt, int ratingDiffLt) throws SQLException {
        final var sql = String.format("SELECT outcome FROM performance where rating_diff > %s and position = %s and is_black = %s and rating_diff < %s",
                ratingDiffGt,
                key,
                side.equals(Side.BLACK) ? "TRUE" : "FALSE",
                ratingDiffLt
        );

        return useStatement(statement -> {
            try {
                return mapToPerformance(statement.executeQuery(sql));
            } catch (SQLException e) {
                return new Performance();
            }
        });
    }

    @Override
    public Performance query(long key, Side side) throws SQLException {
        final var sql = String.format("SELECT outcome FROM performance where position = %s and is_black = %s",
                key,
                side.equals(Side.BLACK) ? "TRUE" : "FALSE"
        );

        return useStatement(statement -> {
            try {
                return mapToPerformance(statement.executeQuery(sql));
            } catch (SQLException e) {
                return new Performance();
            }
        });
    }

    private enum GameResult {
        WON, LOST, DRAW
    }

    @Override
    public void load(String pgnFilePath, String playerName) throws Exception {
        PgnHolder pgn = new PgnHolder(pgnFilePath);
        pgn.loadPgn();
        pgn.getGames()
                .parallelStream()
                .forEach(game -> {
                    final var variant = game.getProperty().get("Variant");
                    if (variant != null && variant.equals("From Position")) {
                        return;
                    }
                    final boolean playerIsBlack;
                    final int ratingDiff;
                    if (game.getWhitePlayer().getName().equals(playerName)) {
                        playerIsBlack = false;
                        ratingDiff = game.getWhitePlayer().getElo() - game.getBlackPlayer().getElo();
                    } else if (game.getBlackPlayer().getName().equals(playerName)) {
                        playerIsBlack = true;
                        ratingDiff = game.getBlackPlayer().getElo() - game.getWhitePlayer().getElo();
                    } else {
                        return;
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
                            return;
                    }

                    final MoveList moves = game.getHalfMoves();
                    final Board board = new Board();
                    for (Move move: moves) {

                        board.doMove(move);

                        final Side side = playerIsBlack ? Side.BLACK : Side.WHITE;
                        try {
                            switch (result) {
                                case WON:
                                    saveWin(board.getIncrementalHashKey(), ratingDiff, side);
                                    break;
                                case LOST:
                                    saveLoss(board.getIncrementalHashKey(), ratingDiff, side);
                                    break;
                                case DRAW:
                                    saveDraw(board.getIncrementalHashKey(), ratingDiff, side);
                                    break;
                            }
                        } catch (SQLException e) {
                            return;
                        }


                    }

                });

    }
}
