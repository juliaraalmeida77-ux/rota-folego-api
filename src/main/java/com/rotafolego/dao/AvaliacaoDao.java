package com.rotafolego.dao;

import com.rotafolego.model.Avaliacao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/** Acesso a tb_avaliacao com JDBC puro (PreparedStatement). */
public class AvaliacaoDao {

    private static final String SELECT_BASE =
            "SELECT cd_avaliacao, nr_nota, ds_comentario, dt_avaliacao, cd_ponto, cd_cidadao "
          + "  FROM tb_avaliacao ";

    private final DataSource ds;

    public AvaliacaoDao(DataSource ds) {
        this.ds = ds;
    }

    public void inserir(Avaliacao a) throws SQLException {
        String sql = "INSERT INTO tb_avaliacao (cd_avaliacao, nr_nota, ds_comentario, cd_ponto, cd_cidadao) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ds.getConnection()) {
            long id = Sequencias.proximoValor(c, "sq_tb_avaliacao");
            try (PreparedStatement st = c.prepareStatement(sql)) {
                st.setLong(1, id);
                st.setInt(2, a.getNrNota());
                st.setString(3, a.getDsComentario());
                st.setLong(4, a.getCdPonto());
                st.setLong(5, a.getCdCidadao());
                st.executeUpdate();
            }
            a.setCdAvaliacao(id);
        }
    }

    public List<Avaliacao> listar() throws SQLException {
        return consultarLista(SELECT_BASE + "ORDER BY cd_avaliacao", null);
    }

    public List<Avaliacao> listarPorPonto(long cdPonto) throws SQLException {
        return consultarLista(SELECT_BASE + "WHERE cd_ponto = ? ORDER BY cd_avaliacao", cdPonto);
    }

    public Optional<Avaliacao> buscarPorId(long id) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(SELECT_BASE + "WHERE cd_avaliacao = ?")) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /** Atualiza so nota e comentario (ponto e cidadao da avaliacao nao mudam). */
    public int atualizar(Avaliacao a) throws SQLException {
        String sql = "UPDATE tb_avaliacao SET nr_nota = ?, ds_comentario = ? WHERE cd_avaliacao = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setInt(1, a.getNrNota());
            st.setString(2, a.getDsComentario());
            st.setLong(3, a.getCdAvaliacao());
            return st.executeUpdate();
        }
    }

    public int remover(long id) throws SQLException {
        String sql = "DELETE FROM tb_avaliacao WHERE cd_avaliacao = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setLong(1, id);
            return st.executeUpdate();
        }
    }

    /** Regra de negocio: o mesmo cidadao nao pode avaliar o mesmo ponto duas vezes. */
    public boolean existePorCidadaoEPonto(long cdCidadao, long cdPonto) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_avaliacao WHERE cd_cidadao = ? AND cd_ponto = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setLong(1, cdCidadao);
            st.setLong(2, cdPonto);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private List<Avaliacao> consultarLista(String sql, Long parametro) throws SQLException {
        List<Avaliacao> lista = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            if (parametro != null) {
                st.setLong(1, parametro);
            }
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Avaliacao mapear(ResultSet rs) throws SQLException {
        Avaliacao a = new Avaliacao();
        a.setCdAvaliacao(rs.getLong("cd_avaliacao"));
        a.setNrNota(rs.getInt("nr_nota"));
        a.setDsComentario(rs.getString("ds_comentario"));
        a.setDtAvaliacao(rs.getTimestamp("dt_avaliacao").toLocalDateTime());
        a.setCdPonto(rs.getLong("cd_ponto"));
        a.setCdCidadao(rs.getLong("cd_cidadao"));
        return a;
    }
}
