package com.rotafolego.controller;

import com.rotafolego.dao.TipoPontoDao;
import com.rotafolego.dto.TipoPontoRequest;
import com.rotafolego.dto.TipoPontoResponse;
import com.rotafolego.exception.RecursoNaoEncontradoException;
import com.rotafolego.model.TipoPonto;
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

/** CRUD de tipos de ponto de descanso (banco, sombra, marquise, bebedouro...). */
@RestController
@RequestMapping("/tipos")
public class TipoPontoController {

    private final TipoPontoDao tipoDao;
    private final ModelMapper mapper;

    public TipoPontoController(TipoPontoDao tipoDao, ModelMapper mapper) {
        this.tipoDao = tipoDao;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<TipoPontoResponse> criar(@Valid @RequestBody TipoPontoRequest req) throws SQLException {
        TipoPonto tipo = mapper.map(req, TipoPonto.class);
        tipoDao.inserir(tipo);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(tipo.getCdTipo()).toUri();
        return ResponseEntity.created(location).body(mapper.map(tipo, TipoPontoResponse.class));
    }

    @GetMapping
    public List<TipoPontoResponse> listar() throws SQLException {
        return tipoDao.listar().stream().map(t -> mapper.map(t, TipoPontoResponse.class)).toList();
    }

    @GetMapping("/{id}")
    public TipoPontoResponse buscar(@PathVariable Long id) throws SQLException {
        return mapper.map(buscarOuFalhar(id), TipoPontoResponse.class);
    }

    @PutMapping("/{id}")
    public TipoPontoResponse atualizar(@PathVariable Long id, @Valid @RequestBody TipoPontoRequest req)
            throws SQLException {
        buscarOuFalhar(id);
        TipoPonto tipo = mapper.map(req, TipoPonto.class);
        tipo.setCdTipo(id);
        tipoDao.atualizar(tipo);
        return mapper.map(tipo, TipoPontoResponse.class);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) throws SQLException {
        buscarOuFalhar(id);
        tipoDao.remover(id); // se houver pontos desse tipo, o Oracle recusa (ORA-02292) e a API devolve 409
        return ResponseEntity.noContent().build();
    }

    private TipoPonto buscarOuFalhar(Long id) throws SQLException {
        return tipoDao.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de ponto nao encontrado: " + id));
    }
}
