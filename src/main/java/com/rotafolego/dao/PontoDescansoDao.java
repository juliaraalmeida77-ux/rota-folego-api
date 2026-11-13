package com.rotafolego.dao;

import com.rotafolego.model.PontoDescanso;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/** Acesso a tb_ponto_descanso com JDBC puro (PreparedStatement). */
public class PontoDescansoDao {

    // SELECT base: traz o nome do tipo (JOIN) e a nota media do ponto (subconsulta).
    private static final String SELECT_BASE =
            "SELECT p.cd_ponto, p.nm_ponto, p.ds_endereco, p.vl_latitude, p.vl_longitude, "
          + "       p.st_coberto, p.dt_cadastro, p.cd_tipo, t.nm_tipo, "
          + "       (SELECT NVL(ROUND(AVG(a.nr_nota), 1), 0) FROM tb_avaliacao a "
          + "         WHERE a.cd_ponto = p.cd_ponto) AS nota_media "
          + "  FROM tb_ponto_descanso p "
          + " INNER JOIN tb_tipo_ponto t ON t.cd_tipo = p.cd_tipo ";

    private final DataSource ds;

    public PontoDescansoDao(DataSource ds) {
        this.ds = ds;
    }

    public void inserir(PontoDescanso p) throws SQLException {
        String sql = "INSERT INTO tb_ponto_descanso "
                   + "(cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, cd_tipo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ds.getConnection()) {
            long id = Sequencias.proximoValor(c, "sq_tb_ponto_descanso");
            try (PreparedStatement st = c.prepareStatement(sql)) {
                st.setLong(1, id);
                st.setString(2, p.getNmPonto());
                st.setString(3, p.getDsEndereco());
                setDouble(st, 4, p.getVlLatitude());
                setDouble(st, 5, p.getVlLongitude());
                st.setString(6, Boolean.TRUE.equals(p.getCoberto()) ? "S" : "N");
                st.setLong(7, p.getCdTipo());
                st.executeUpdate();
            }
            p.setCdPonto(id);
        }
    }

    public List<PontoDescanso> listar() throws SQLException {
        List<PontoDescanso> lista = new ArrayList<>();
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(SELECT_BASE + "ORDER BY p.nm_ponto");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<PontoDescanso> buscarPorId(long id) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(SELECT_BASE + "WHERE p.cd_ponto = ?")) {
            st.setLong(1, id);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /** @return quantidade de linhas atualizadas (0 = id nao existe) */
    public int atualizar(PontoDescanso p) throws SQLException {
        String sql = "UPDATE tb_ponto_descanso "
                   + "   SET nm_ponto = ?, ds_endereco = ?, vl_latitude = ?, vl_longitude = ?, "
                   + "       st_coberto = ?, cd_tipo = ? "
                   + " WHERE cd_ponto = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, p.getNmPonto());
            st.setString(2, p.getDsEndereco());
            setDouble(st, 3, p.getVlLatitude());
            setDouble(st, 4, p.getVlLongitude());
            st.setString(5, Boolean.TRUE.equals(p.getCoberto()) ? "S" : "N");
            st.setLong(6, p.getCdTipo());
            st.setLong(7, p.getCdPonto());
            return st.executeUpdate();
        }
    }

    /** @return quantidade de linhas removidas (0 = id nao existe) */
    public int remover(long id) throws SQLException {
        String sql = "DELETE FROM tb_ponto_descanso WHERE cd_ponto = ?";
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setLong(1, id);
            return st.executeUpdate();
        }
    }

    /** Regra: nao pode haver dois pontos com mesmo nome e endereco (ignora o proprio id no PUT). */
    public boolean existePorNomeEEndereco(String nome, String endereco, Long ignorarId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_ponto_descanso "
                   + " WHERE LOWER(nm_ponto) = LOWER(?) AND LOWER(ds_endereco) = LOWER(?) AND cd_ponto <> ?";
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, nome);
            st.setString(2, endereco);
            st.setLong(3, ignorarId == null ? -1L : ignorarId);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private PontoDescanso mapear(ResultSet rs) throws SQLException {
        PontoDescanso p = new PontoDescanso();
        p.setCdPonto(rs.getLong("cd_ponto"));
        p.setNmPonto(rs.getString("nm_ponto"));
        p.setDsEndereco(rs.getString("ds_endereco"));
        p.setVlLatitude(getDouble(rs, "vl_latitude"));
        p.setVlLongitude(getDouble(rs, "vl_longitude"));
        p.setCoberto("S".equals(rs.getString("st_coberto")));
        p.setDtCadastro(rs.getTimestamp("dt_cadastro").toLocalDateTime());
        p.setCdTipo(rs.getLong("cd_tipo"));
        p.setNmTipo(rs.getString("nm_tipo"));
        p.setNotaMedia(rs.getDouble("nota_media"));
        return p;
    }

    private static Double getDouble(ResultSet rs, String coluna) throws SQLException {
        double v = rs.getDouble(coluna);
        return rs.wasNull() ? null : v;
    }

    private static void setDouble(PreparedStatement st, int idx, Double v) throws SQLException {
        if (v == null) {
            st.setNull(idx, Types.NUMERIC);
        } else {
            st.setDouble(idx, v);
        }
    }
}
