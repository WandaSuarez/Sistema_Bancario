package com.example.SistemaBancario.dto;

public class TransferenciaRequestDTO {
    private Long cuentaOrigen;
    private Long cuentaDestino;
    private Double monto;
    private String moneda;
 
    // getters y seteers
    public Long getCuentaOrigen() {
        return cuentaOrigen;
    }
    public void setCuentaOrigen(Long cuentaOrigen) {
        this.cuentaOrigen = cuentaOrigen;
    }
    public Long getCuentaDestino() {
        return cuentaDestino;
    }
    public void setCuentaDestino(Long cuentaDestino) {
        this.cuentaDestino = cuentaDestino;
    }
    public Double getMonto() {
        return monto;
    }
    public void setMonto(Double monto) {
        this.monto = monto;
    }
    public String getMoneda() {
        return moneda;
    }
    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }
}
