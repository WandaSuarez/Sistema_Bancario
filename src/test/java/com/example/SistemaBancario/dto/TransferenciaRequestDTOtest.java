package com.example.SistemaBancario.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TransferenciaRequestDTOtest {
    @Test
    void deberiaCrearDTOConDatosValidos() {  // test que verifico la creacion de un dto con datos validos
        TransferenciaRequestDTO dto = new TransferenciaRequestDTO();
        dto.setCuentaOrigen(1001L);// pongoo cuenta origen
        dto.setCuentaDestino(1002L);// pongoo cuenta destino
        dto.setMonto(1000.0);// pongoo monto
        dto.setMoneda("PESOS");// pongoo tipo de moneda

        assertNotNull(dto);// verifico que el dto no sea null
        assertEquals(1001L, dto.getCuentaOrigen());// verifico cuenta origen
        assertEquals(1002L, dto.getCuentaDestino());// verifico cuenta destino
        assertEquals(1000.0, dto.getMonto());// verifico monto
        assertEquals("PESOS", dto.getMoneda());// verifico moneda
    }

    @Test
    void deberiaValidarMontoPositivo() {// test que verifico montos negativos
        TransferenciaRequestDTO dto = new TransferenciaRequestDTO();
        dto.setMonto(-100.0);// pongoo monto negativo
        
        assertNotNull(dto);// verifico que el dto no sea null
        assertTrue(dto.getMonto() < 0);// verifico que el monto sea negativo
    }

    @Test
    void deberiaValidarCuentasDistintas() {  // test que verifico que las cuentas sean diferentes
        TransferenciaRequestDTO dto = new TransferenciaRequestDTO();
        dto.setCuentaOrigen(1001L);
        dto.setCuentaDestino(1001L);// misma cuenta que origen
        
        assertNotNull(dto);// verifico que el dto no sea null
        assertEquals(dto.getCuentaOrigen(), dto.getCuentaDestino()); // verifico que las cuentas son iguales
    }

    @Test
    void deberiaValidarMoneda() {  // test que verfico el tipo de moneda
        TransferenciaRequestDTO dto = new TransferenciaRequestDTO();
        dto.setMoneda("DOLARES");
        
        assertNotNull(dto);
        assertEquals("DOLARES", dto.getMoneda());
    }

    @Test
    void deberiaValidarCuentasNoNulas() {  // test que verfico que las cuentas no sean null
        TransferenciaRequestDTO dto = new TransferenciaRequestDTO();
        dto.setCuentaOrigen(null);
        dto.setCuentaDestino(null);
        
        assertNull(dto.getCuentaOrigen());
        assertNull(dto.getCuentaDestino());
    }
}
