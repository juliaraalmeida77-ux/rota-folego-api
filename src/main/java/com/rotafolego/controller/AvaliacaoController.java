package com.rotafolego.controller;

import com.rotafolego.dao.AvaliacaoDao;
import com.rotafolego.dto.AvaliacaoResponse;
import com.rotafolego.dto.AvaliacaoUpdateRequest;
import com.rotafolego.exception.RecursoNaoEncontradoException;
import com.rotafolego.model.Avaliacao;
import jakarta.validation.Valid;
import java.sql.SQLException;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Leitura, edicao e remocao de avaliacoes.
 * O cadastro e feito pela rota aninhada POST /pontos/{id}/avaliacoes (PontoController).
 */
@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoDao avaliacaoDao;
    private final ModelMapper mapper;

    public AvaliacaoController(AvaliacaoDao avaliacaoDao, ModelMapper mapper) {
        this.avaliacaoDao = avaliacaoDao;
        this.mapper = mapper;
    }

    @GetMapping
    public List<AvaliacaoResponse> listar() throws SQLException {
        return avaliacaoDao.listar().stream().map(a -> mapper.map(a, AvaliacaoResponse.class)).toList();
    }

    @GetMapping("/{id}")
    public AvaliacaoResponse buscar(@PathVariable Long id) throws SQLException {
        return mapper.map(buscarOuFalhar(id), AvaliacaoResponse.class);
    }

    @PutMapping("/{id}")
    public AvaliacaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AvaliacaoUpdateRequest req)
            throws SQLException {
        buscarOuFalhar(id);
        Avaliacao nova = mapper.map(req, Avaliacao.class);
        nova.setCdAvaliacao(id);
        avaliacaoDao.atualizar(nova);
        return mapper.map(buscarOuFalhar(id), AvaliacaoResponse.class);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) throws SQLException {
        buscarOuFalhar(id);
        avaliacaoDao.remover(id);
        return ResponseEntity.noContent().build();
    }

    private Avaliacao buscarOuFalhar(Long id) throws SQLException {
        return avaliacaoDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliacao nao encontrada: " + id));
    }
}
