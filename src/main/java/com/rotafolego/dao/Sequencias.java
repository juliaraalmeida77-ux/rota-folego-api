package com.rotafolego.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Busca o proximo valor de uma sequence do Oracle (o nome vem sempre de constante do DAO). */
final class Sequencias {

    private Sequencias() { }

    static long proximoValor(Connection c, String sequence) throws SQLException {
        String sql = "SELECT " + sequence + ".NEXTVAL FROM dual";
        try (PreparedStatement st = c.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
