package com.rotafolego.handler;

import com.rotafolego.dto.ErroResponse;
import com.rotafolego.exception.RecursoNaoEncontradoException;
import com.rotafolego.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Transforma toda excecao num JSON padronizado (ErroResponse).
 * Estende ResponseEntityExceptionHandler para cobrir tambem os erros do proprio Spring MVC:
 * 400 (JSON invalido / tipo errado), 404 (rota inexistente), 405 (metodo nao suportado), 415...
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ----- excecoes do dominio -----

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex, HttpServletRequest req) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    // ----- erros do banco (o detalhe do SQL fica so no log do servidor) -----

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErroResponse> erroBanco(SQLException ex, HttpServletRequest req) {
        log.error("Erro de banco de dados (ORA-{})", ex.getErrorCode(), ex);
        String caminho = req.getRequestURI();
        return switch (ex.getErrorCode()) {
            case 1 -> responder(HttpStatus.CONFLICT, "Ja existe um registro com esses dados", caminho);
            case 2292 -> responder(HttpStatus.CONFLICT, "Nao e possivel remover: existem registros dependentes", caminho);
            case 2291 -> responder(HttpStatus.BAD_REQUEST, "Referencia invalida: confira cdTipo, cdPonto e cdCidadao", caminho);
            case 2290, 1400, 1438, 12899 -> responder(HttpStatus.BAD_REQUEST, "Dados invalidos para gravacao", caminho);
            default -> responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao acessar os dados", caminho);
        };
    }

    // ----- qualquer outro erro inesperado: 500 generico -----

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor", req.getRequestURI());
    }

    // ----- erros do Spring MVC -----

    /** 400 com a lista de campos invalidos (Bean Validation). */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErroResponse.Detalhe> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ErroResponse.Detalhe(f.getField(), f.getDefaultMessage()))
                .toList();
        ErroResponse erro = new ErroResponse(400, "Bad Request", "Dados de entrada invalidos",
                caminho(request), detalhes);
        return new ResponseEntity<>(erro, headers, status);
    }

    /** Todos os demais erros do MVC (400, 404, 405, 415...) saem no mesmo formato. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        int codigo = statusCode.value();
        HttpStatus http = HttpStatus.resolve(codigo);
        String motivo = http != null ? http.getReasonPhrase() : "Erro";

        String mensagem;
        if (ex instanceof HttpMessageNotReadableException) {
            mensagem = "Corpo da requisicao ausente ou com JSON invalido";
        } else if (codigo == 400) {
            mensagem = "Requisicao invalida";
        } else if (codigo == 404) {
            mensagem = "Rota nao encontrada";
        } else if (codigo == 405) {
            mensagem = "Metodo HTTP nao suportado para esta rota";
        } else if (codigo == 415) {
            mensagem = "Tipo de conteudo nao suportado (use application/json)";
        } else if (codigo >= 500) {
            mensagem = "Erro interno do servidor";
        } else {
            mensagem = "Nao foi possivel processar a requisicao";
        }

        ErroResponse erro = new ErroResponse(codigo, motivo, mensagem, caminho(request), null);
        return new ResponseEntity<>(erro, headers, statusCode);
    }

    // ----- auxiliares -----

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem, String caminho) {
        ErroResponse erro = new ErroResponse(status.value(), status.getReasonPhrase(), mensagem, caminho, null);
        return ResponseEntity.status(status).body(erro);
    }

    private String caminho(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
