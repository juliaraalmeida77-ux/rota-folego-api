package com.rotafolego.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AvaliacaoUpdateRequest {

    @NotNull(message = "nrNota e obrigatorio")
    @Min(value = 1, message = "nrNota deve ser de 1 a 5")
    @Max(value = 5, message = "nrNota deve ser de 1 a 5")
    private Integer nrNota;

    @Size(max = 200, message = "dsComentario deve ter no maximo 200 caracteres")
    private String dsComentario;

    public Integer getNrNota() { return nrNota; }
    public void setNrNota(Integer nrNota) { this.nrNota = nrNota; }

    public String getDsComentario() { return dsComentario; }
    public void setDsComentario(String dsComentario) { this.dsComentario = dsComentario; }

}
