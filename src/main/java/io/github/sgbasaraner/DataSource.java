package io.github.sgbasaraner;

import java.sql.SQLException;

public interface DataSource {
    void saveWin(long position, int ratingDiff, Side side) throws SQLException;
    void saveLoss(long position, int ratingDiff, Side side) throws SQLException;
    void saveDraw(long position, int ratingDiff, Side side) throws SQLException;
    Performance query(long position, Side side, int ratingDiffGt, int ratingDiffLt) throws SQLException;
    Performance query(long position, Side side) throws SQLException;
    void load(String pgnFilePath, String playerName) throws Exception;
}
