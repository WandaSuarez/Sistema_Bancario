package com.example.SistemaBancario.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.SistemaBancario.dto.TransferenciaRequestDTO;
import com.example.SistemaBancario.dto.TransferenciaResponseDTO;
import com.example.SistemaBancario.service.TransferenciaService;

@ExtendWith(MockitoExtension.class)
class TranferenciaControllerTest {
    @Mock
    private TransferenciaService transferenciaService;    // mock del servicio de transferencias

    @InjectMocks
    private TranferenciaController transferenciaController;    // inyecta el mock en el controller

    private TransferenciaRequestDTO requestDTO;    // dto para usar en los tests
    private TransferenciaResponseDTO responseDTO;    // dto de respuesta para los tests

    @BeforeEach
    void setUp() {    // se ejecuto antes de cada test
        requestDTO = new TransferenciaRequestDTO();
        requestDTO.setCuentaOrigen(1L);
        requestDTO.setCuentaDestino(2L);
        requestDTO.setMonto(1000.0);
        requestDTO.setMoneda("PESOS");

        responseDTO = new TransferenciaResponseDTO("EXITOSA", "Transferencia realizada");
    }

    @Test
    void deberiaRealizarTransferenciaExitosa() {    // test de transferencia exitosa
        when(transferenciaService.realizarTransferencia(any(TransferenciaRequestDTO.class)))
            .thenReturn(responseDTO);    // simula transferencia exitosa

        ResponseEntity<TransferenciaResponseDTO> response = 
            transferenciaController.realizarTransferencia(requestDTO);    // ejecuto el endpoint

        assertEquals(HttpStatus.CREATED, response.getStatusCode());    // verificoo codigo 201
    }

    @SuppressWarnings("null")
    @Test
    void deberiaRetornarBadRequestCuandoHayError() {    // test de error en transferencia
        when(transferenciaService.realizarTransferencia(any(TransferenciaRequestDTO.class)))
            .thenThrow(new RuntimeException("Error en transferencia"));    // simula error

        ResponseEntity<TransferenciaResponseDTO> response = 
            transferenciaController.realizarTransferencia(requestDTO);    // ejecuto el endpoint

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());    // verificoo codigo 400
        assertNotNull(response.getBody());    // verificoo que hay mensaje de error
        assertEquals("FALLIDA", response.getBody().getEstado());    // verificoo estado fallido
    }

    @Test
    void deberiaRetornarBadRequestCuandoRequestEsNull() {    // test cuando el request es null
        ResponseEntity<TransferenciaResponseDTO> response = 
            transferenciaController.realizarTransferencia(null);    // ejecuto con null
            
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());    // verificoo codigo 400
    }

    @Test
    void deberiaRetornarBadRequestCuandoMontoEsCero() {    // test con monto cero
        requestDTO.setMonto(0.0);
        ResponseEntity<TransferenciaResponseDTO> response = 
            transferenciaController.realizarTransferencia(requestDTO);
            
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());    // verificoo codigo 400
    }

    @Test
    void deberiaRetornarBadRequestCuandoCuentasIguales() {    // test cuando origen y destino son iguales
        requestDTO.setCuentaOrigen(1L);
        requestDTO.setCuentaDestino(1L);
        
        ResponseEntity<TransferenciaResponseDTO> response = 
            transferenciaController.realizarTransferencia(requestDTO);
            
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());    // verificoo codigo 400
    }
}
