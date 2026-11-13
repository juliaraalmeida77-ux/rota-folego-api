package com.rotafolego.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

/** Corpo padrao de TODO erro da API (nunca mostra stack trace nem SQL). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErroResponse {

    public record Detalhe(String campo, String mensagem) { }

    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String erro;
    private final String mensagem;
    private final String caminho;
    private final List<Detalhe> detalhes;

    public ErroResponse(int status, String erro, String mensagem, String caminho, List<Detalhe> detalhes) {
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.caminho = caminho;
        this.detalhes = detalhes;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getErro() { return erro; }
    public String getMensagem() { return mensagem; }
    public String getCaminho() { return caminho; }
    public List<Detalhe> getDetalhes() { return detalhes; }
}
