package com.example.SistemaBancario.exeption;

public class CuentaException extends RuntimeException {
    public CuentaException(String message) {
        super(message);
    }
}

