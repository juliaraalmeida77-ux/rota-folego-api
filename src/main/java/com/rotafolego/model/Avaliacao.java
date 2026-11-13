package com.rotafolego.model;

import java.time.LocalDateTime;

public class Avaliacao {

    private Long cdAvaliacao;

    private Integer nrNota;

    private String dsComentario;

    private LocalDateTime dtAvaliacao;

    private Long cdPonto;

    private Long cdCidadao;

    public Long getCdAvaliacao() { return cdAvaliacao; }
    public void setCdAvaliacao(Long cdAvaliacao) { this.cdAvaliacao = cdAvaliacao; }

    public Integer getNrNota() { return nrNota; }
    public void setNrNota(Integer nrNota) { this.nrNota = nrNota; }

    public String getDsComentario() { return dsComentario; }
    public void setDsComentario(String dsComentario) { this.dsComentario = dsComentario; }

    public LocalDateTime getDtAvaliacao() { return dtAvaliacao; }
    public void setDtAvaliacao(LocalDateTime dtAvaliacao) { this.dtAvaliacao = dtAvaliacao; }

    public Long getCdPonto() { return cdPonto; }
    public void setCdPonto(Long cdPonto) { this.cdPonto = cdPonto; }

    public Long getCdCidadao() { return cdCidadao; }
    public void setCdCidadao(Long cdCidadao) { this.cdCidadao = cdCidadao; }

}
