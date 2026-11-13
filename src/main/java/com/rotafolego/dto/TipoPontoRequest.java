package com.rotafolego.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TipoPontoRequest {

    @NotBlank(message = "nmTipo e obrigatorio")
    @Size(max = 30, message = "nmTipo deve ter no maximo 30 caracteres")
    private String nmTipo;

    public String getNmTipo() { return nmTipo; }
    public void setNmTipo(String nmTipo) { this.nmTipo = nmTipo; }

}
