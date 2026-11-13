package com.rotafolego.controller;

import com.rotafolego.dao.AvaliacaoDao;
import com.rotafolego.dao.PontoDescansoDao;
import com.rotafolego.dto.AvaliacaoRequest;
import com.rotafolego.dto.AvaliacaoResponse;
import com.rotafolego.dto.PontoRequest;
import com.rotafolego.dto.PontoResponse;
import com.rotafolego.exception.RecursoNaoEncontradoException;
import com.rotafolego.exception.RegraNegocioException;
import com.rotafolego.model.Avaliacao;
import com.rotafolego.model.PontoDescanso;
import jakarta.validation.Valid;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** CRUD de pontos de descanso + rotas aninhadas do 1:N (/pontos/{id}/avaliacoes). */
@RestController
@RequestMapping("/pontos")
public class PontoController {

    private final PontoDescansoDao pontoDao;
    private final AvaliacaoDao avaliacaoDao;
    private final ModelMapper mapper;

    public PontoController(PontoDescansoDao pontoDao, AvaliacaoDao avaliacaoDao, ModelMapper mapper) {
        this.pontoDao = pontoDao;
        this.avaliacaoDao = avaliacaoDao;
        this.mapper = mapper;
    }

    // ------------------------- pontos -------------------------

    @PostMapping
    public ResponseEntity<PontoResponse> criar(@Valid @RequestBody PontoRequest req) throws SQLException {
        validarDuplicidade(req, null);
        PontoDescanso ponto = mapper.map(req, PontoDescanso.class);
        pontoDao.inserir(ponto);
        PontoDescanso salvo = buscarOuFalhar(ponto.getCdPonto());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(salvo.getCdPonto()).toUri();
        return ResponseEntity.created(location).body(mapper.map(salvo, PontoResponse.class));
    }

    @GetMapping
    public List<PontoResponse> listar() throws SQLException {
        return pontoDao.listar().stream().map(p -> mapper.map(p, PontoResponse.class)).toList();
    }

    @GetMapping("/{id}")
    public PontoResponse buscar(@PathVariable Long id) throws SQLException {
        return mapper.map(buscarOuFalhar(id), PontoResponse.class);
    }

    @PutMapping("/{id}")
    public PontoResponse atualizar(@PathVariable Long id, @Valid @RequestBody PontoRequest req)
            throws SQLException {
        buscarOuFalhar(id);
        validarDuplicidade(req, id);
        PontoDescanso ponto = mapper.map(req, PontoDescanso.class);
        ponto.setCdPonto(id);
        pontoDao.atualizar(ponto);
        return mapper.map(buscarOuFalhar(id), PontoResponse.class);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) throws SQLException {
        buscarOuFalhar(id);
        pontoDao.remover(id); // ponto com avaliacoes/trajetos: o Oracle recusa (ORA-02292) e a API devolve 409
        return ResponseEntity.noContent().build();
    }

    // ------------------- avaliacoes (rotas aninhadas) -------------------

    @GetMapping("/{id}/avaliacoes")
    public List<AvaliacaoResponse> listarAvaliacoes(@PathVariable Long id) throws SQLException {
        buscarOuFalhar(id);
        return avaliacaoDao.listarPorPonto(id).stream()
                .map(a -> mapper.map(a, AvaliacaoResponse.class)).toList();
    }

    @PostMapping("/{id}/avaliacoes")
    public ResponseEntity<AvaliacaoResponse> criarAvaliacao(@PathVariable Long id,
            @Valid @RequestBody AvaliacaoRequest req) throws SQLException {
        buscarOuFalhar(id); // 404 se o ponto nao existe

        // REGRA DE NEGOCIO: um cidadao nao avalia o mesmo ponto duas vezes -> 409
        if (avaliacaoDao.existePorCidadaoEPonto(req.getCdCidadao(), id)) {
            throw new RegraNegocioException("O cidadao " + req.getCdCidadao()
                    + " ja avaliou o ponto " + id + ". Cada cidadao so pode avaliar um ponto uma vez.");
        }

        Avaliacao avaliacao = mapper.map(req, Avaliacao.class);
        avaliacao.setCdPonto(id);
        avaliacaoDao.inserir(avaliacao);

        Avaliacao salva = avaliacaoDao.buscarPorId(avaliacao.getCdAvaliacao())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliacao nao encontrada apos gravar"));
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/avaliacoes/{id}").buildAndExpand(salva.getCdAvaliacao()).toUri();
        return ResponseEntity.created(location).body(mapper.map(salva, AvaliacaoResponse.class));
    }

    // ------------------------- auxiliares -------------------------

    private PontoDescanso buscarOuFalhar(Long id) throws SQLException {
        return pontoDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ponto de descanso nao encontrado: " + id));
    }

    /** Regra: nao pode haver dois pontos com o mesmo nome e endereco. */
    private void validarDuplicidade(PontoRequest req, Long ignorarId) throws SQLException {
        if (pontoDao.existePorNomeEEndereco(req.getNmPonto(), req.getDsEndereco(), ignorarId)) {
            throw new RegraNegocioException("Ja existe um ponto de descanso com esse nome e endereco");
        }
    }
}
