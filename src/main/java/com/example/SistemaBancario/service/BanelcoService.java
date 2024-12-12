package com.example.SistemaBancario.service;

import org.springframework.stereotype.Service;

@Service
public class BanelcoService {
    public boolean realizarTransferenciaInterbancaria(Long cuentaOrigen, Long cuentaDestino, Double monto, String moneda) {
        // simulo una repuesta aleatoria del servicio externo
        return Math.random() > 0.3; // 70% d exito y 30 de fallo
    }
}
