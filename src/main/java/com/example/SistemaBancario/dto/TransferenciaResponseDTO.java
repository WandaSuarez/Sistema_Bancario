package com.example.SistemaBancario.dto;

public class TransferenciaResponseDTO {
    private String estado;
    private String mensaje;

    // constructor
    public TransferenciaResponseDTO(String estado, String mensaje) {
        this.estado = estado;
        this.mensaje = mensaje;
    }

    // getter y setters
    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
