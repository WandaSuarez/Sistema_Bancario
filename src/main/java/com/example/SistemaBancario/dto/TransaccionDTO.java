package com.example.SistemaBancario.dto;

public class TransaccionDTO {
    private String fecha;
    private String tipo;
    private String descripcionBreve;
    private Double monto;

    public TransaccionDTO(String fecha, String tipo, String descripcionBreve, Double monto) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.descripcionBreve = descripcionBreve;
        this.monto = monto;
    }

    // getters y setters
    public String getFecha() {
        return fecha;
    }
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    public String getDescripcionBreve() {
        return descripcionBreve;
    }
    public void setDescripcionBreve(String descripcionBreve) {
        this.descripcionBreve = descripcionBreve;
    }
    public Double getMonto() {
        return monto;
    }
    public void setMonto(Double monto) {
        this.monto = monto;
    }
}
