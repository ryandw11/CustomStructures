package com.ryandw11.structure.io.sql;

import org.sqlite.Function;

import java.sql.SQLException;

/**
 * A custom distance function for the SQLite engine.
 *
 * <p>Usage: DIST(x1, y1, z1, x2, y2, z2); where the parameters are doubles.</p>
 */
public class DistanceFunction extends Function {
    @Override
    protected void xFunc() throws SQLException {
        if (args() != 6) {
            throw new SQLException("DIST(x1, y1, z1, x2, y2, z2): Invalid argument count.");
        }

        double x1 = value_double(0);
        double y1 = value_double(1);
        double z1 = value_double(2);
        double x2 = value_double(3);
        double y2 = value_double(4);
        double z2 = value_double(5);

        result(Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2)));
    }
}
