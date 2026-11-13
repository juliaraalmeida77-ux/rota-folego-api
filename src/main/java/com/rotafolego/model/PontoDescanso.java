package com.rotafolego.model;

import java.time.LocalDateTime;

public class PontoDescanso {

    private Long cdPonto;

    private String nmPonto;

    private String dsEndereco;

    private Double vlLatitude;

    private Double vlLongitude;

    private Boolean coberto;

    private LocalDateTime dtCadastro;

    private Long cdTipo;

    private String nmTipo;

    private Double notaMedia;

    public Long getCdPonto() { return cdPonto; }
    public void setCdPonto(Long cdPonto) { this.cdPonto = cdPonto; }

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

    public LocalDateTime getDtCadastro() { return dtCadastro; }
    public void setDtCadastro(LocalDateTime dtCadastro) { this.dtCadastro = dtCadastro; }

    public Long getCdTipo() { return cdTipo; }
    public void setCdTipo(Long cdTipo) { this.cdTipo = cdTipo; }

    public String getNmTipo() { return nmTipo; }
    public void setNmTipo(String nmTipo) { this.nmTipo = nmTipo; }

    public Double getNotaMedia() { return notaMedia; }
    public void setNotaMedia(Double notaMedia) { this.notaMedia = notaMedia; }

}
