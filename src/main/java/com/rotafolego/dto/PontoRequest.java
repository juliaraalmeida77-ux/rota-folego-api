package com.rotafolego.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class PontoRequest {

    @NotBlank(message = "nmPonto e obrigatorio")
    @Size(max = 80, message = "nmPonto deve ter no maximo 80 caracteres")
    private String nmPonto;

    @NotBlank(message = "dsEndereco e obrigatorio")
    @Size(max = 120, message = "dsEndereco deve ter no maximo 120 caracteres")
    private String dsEndereco;

    @DecimalMin(value = "-90.0", message = "vlLatitude deve estar entre -90 e 90")
    @DecimalMax(value = "90.0", message = "vlLatitude deve estar entre -90 e 90")
    private Double vlLatitude;

    @DecimalMin(value = "-180.0", message = "vlLongitude deve estar entre -180 e 180")
    @DecimalMax(value = "180.0", message = "vlLongitude deve estar entre -180 e 180")
    private Double vlLongitude;

    @NotNull(message = "coberto e obrigatorio (true ou false)")
    private Boolean coberto;

    @NotNull(message = "cdTipo e obrigatorio")
    @Positive(message = "cdTipo deve ser positivo")
    private Long cdTipo;

    public String getNmPonto() { return nmPonto; }
    public void setNmPonto(String nmPonto) { this.nmPonto = nmPonto; }

    public String getDsEndereco() { return dsEndereco; }
    public void setDsEndereco(String dsEndereco) { this.dsEndereco = dsEndereco; }

    public Double getVlLatitude() { return vlLatitude; }
    public void setVlLatitude(Double vlLatitude) { this.vlLatitude = vlLatitude; }

    public Double getVlLongitude() { return vlLongitude; }
    public void setVlLongitude(Double vlLongitude) { this.vlLongitude = vlLongitude; }

    public Boolean getCoberto() { return coberto; }
    public void setCoberto(Boolean coberto) { this.coberto = coberto; }

    public Long getCdTipo() { return cdTipo; }
    public void setCdTipo(Long cdTipo) { this.cdTipo = cdTipo; }

}
