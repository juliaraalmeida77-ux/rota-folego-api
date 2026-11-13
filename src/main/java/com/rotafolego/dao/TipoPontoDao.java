package com.rotafolego.dao;

import com.rotafolego.model.TipoPonto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/** Acesso a tb_tipo_ponto com JDBC puro (PreparedStatement). */
public class TipoPontoDao {

    private final DataSource ds;

    public TipoPontoDao(DataSource ds) {
        this.ds = ds;
    }

    public void inserir(TipoPonto t) throws SQLException {
        String sql = "INSERT INTO tb_tipo_ponto (cd_tipo, nm_tipo) VALUES (?, ?)";
        try (Connection c = ds.getConnection()) {
            long id = Sequencias.proximoValor(c, "sq_tb_tipo_ponto");
            try (PreparedStatement st = c.prepareStatement(sql)) {
                st.setLong(1, id);
                st.setString(2, t.getNmTipo());
                st.executeUpdate();
            }
            t.setCdTipo(id);
        }
    }

    public List<TipoPonto> listar() throws SQLException {
        String sql = "SELECT cd_tipo, nm_tipo FROM tb_tipo_ponto ORDER BY nm_tipo";
        List<TipoPonto> lista = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<TipoPonto> buscarPorId(long id) throws SQLException {
        String sql = "SELECT cd_tipo, nm_tipo FROM tb_tipo_ponto WHERE cd_tipo = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /** @return quantidade de linhas atualizadas (0 = id nao existe) */
    public int atualizar(TipoPonto t) throws SQLException {
        String sql = "UPDATE tb_tipo_ponto SET nm_tipo = ? WHERE cd_tipo = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, t.getNmTipo());
            st.setLong(2, t.getCdTipo());
            return st.executeUpdate();
        }
    }

    /** @return quantidade de linhas removidas (0 = id nao existe) */
    public int remover(long id) throws SQLException {
        String sql = "DELETE FROM tb_tipo_ponto WHERE cd_tipo = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setLong(1, id);
            return st.executeUpdate();
        }
    }

    private TipoPonto mapear(ResultSet rs) throws SQLException {
        TipoPonto t = new TipoPonto();
        t.setCdTipo(rs.getLong("cd_tipo"));
        t.setNmTipo(rs.getString("nm_tipo"));
        return t;
    }
}
