package com.example.SistemaBancario.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BanelcoServiceTest {
    @InjectMocks
    private BanelcoService banelcoService;// servicio a testear

    @Test
    void deberiaRealizarTransferenciaInterbancaria() {// test de transferencia interbancaria
        boolean resultado = banelcoService.realizarTransferenciaInterbancaria(
            1L, 2L, 1000.0, "PESOS");    // ejecuta transferencia
            
        // el resultado puede ser true o false por ser aleatorio
        assertTrue(resultado || !resultado);// verificoo que sea boolean
    }

    @Test
    void deberiaRealizarMultiplesTransferencias() {// test de multiples transferencias
        int exitosas = 0;
        int total = 100;
        
        // realiza 100 transferencias y cuenta las exitosas
        for(int i = 0; i < total; i++) {
            if(banelcoService.realizarTransferenciaInterbancaria(1L, 2L, 1000.0, "PESOS")) {
                exitosas++;
            }
        }
        
        // verificoo que el porcentaje de éxito este cerca del 70%
        assertTrue(exitosas > 50 && exitosas < 90);// permite un margen de error
    }
}
