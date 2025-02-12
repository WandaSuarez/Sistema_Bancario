package com.example.SistemaBancario.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TransferenciaResponseDTOtest {  // Note the "Test" suffix

    @Test
    void deberiaCrearDTOConConstructor() {
        TransferenciaResponseDTO dto = new TransferenciaResponseDTO("Exitosa", "Transferencia realizada");
        
        assertNotNull(dto);
        assertEquals("Exitosa", dto.getEstado());
        assertEquals("Transferencia realizada", dto.getMensaje());
    }
   
    @Test
    void deberiaModificarEstadoYMensaje() {
        TransferenciaResponseDTO dto = new TransferenciaResponseDTO("Inicial", "Mensaje inicial");
        
        dto.setEstado("Fallida");
        dto.setMensaje("Error en la transferencia");
        
        assertEquals("Fallida", dto.getEstado());
        assertEquals("Error en la transferencia", dto.getMensaje());
    }

    @Test
    void deberiaValidarEstadoNulo() {
        TransferenciaResponseDTO dto = new TransferenciaResponseDTO(null, "mensaje");
        
        assertNull(dto.getEstado());
        assertNotNull(dto.getMensaje());
    }

    @Test
    void deberiaValidarMensajeNulo() {
        TransferenciaResponseDTO dto = new TransferenciaResponseDTO("Exitosa", null);
        
        assertNotNull(dto.getEstado());
        assertNull(dto.getMensaje());
    }

    @Test
    void deberiaValidarEstadosValidos() {
        TransferenciaResponseDTO dto = new TransferenciaResponseDTO("Exitosa", "OK");
        assertEquals("Exitosa", dto.getEstado());
        
        dto.setEstado("Pendiente");
        assertEquals("Pendiente", dto.getEstado());
        
        dto.setEstado("Fallida");
        assertEquals("Fallida", dto.getEstado());
    }
}
