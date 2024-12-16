package com.example.SistemaBancario.exeption;

public class ClienteExeption extends RuntimeException {
    public ClienteExeption(String message) {
        super(message);
    }
}

