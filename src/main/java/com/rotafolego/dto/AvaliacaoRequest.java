package com.rotafolego.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AvaliacaoRequest {

    @NotNull(message = "nrNota e obrigatorio")
    @Min(value = 1, message = "nrNota deve ser de 1 a 5")
    @Max(value = 5, message = "nrNota deve ser de 1 a 5")
    private Integer nrNota;

    @Size(max = 200, message = "dsComentario deve ter no maximo 200 caracteres")
    private String dsComentario;

    @NotNull(message = "cdCidadao e obrigatorio")
    @Positive(message = "cdCidadao deve ser positivo")
    private Long cdCidadao;

    public Integer getNrNota() { return nrNota; }
    public void setNrNota(Integer nrNota) { this.nrNota = nrNota; }

    public String getDsComentario() { return dsComentario; }
    public void setDsComentario(String dsComentario) { this.dsComentario = dsComentario; }

    public Long getCdCidadao() { return cdCidadao; }
    public void setCdCidadao(Long cdCidadao) { this.cdCidadao = cdCidadao; }

}
