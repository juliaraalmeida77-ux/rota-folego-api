package com.rotafolego.exception;

/** Regra de negocio violada (vira HTTP 409). Ex.: cidadao avaliando o mesmo ponto duas vezes. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
