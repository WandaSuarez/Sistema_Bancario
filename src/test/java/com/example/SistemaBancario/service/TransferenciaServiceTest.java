package com.example.SistemaBancario.service;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.SistemaBancario.repository.TransaccionRepository;
import com.example.SistemaBancario.repository.CuentaRepository;
import com.example.SistemaBancario.dto.TransferenciaRequestDTO;
import com.example.SistemaBancario.dto.TransferenciaResponseDTO;
import com.example.SistemaBancario.model.Cuenta;

@ExtendWith(MockitoExtension.class) 
class TransferenciaServiceTest {
    
    @Mock
    private TransaccionRepository transaccionRepository;
    
    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private BanelcoService banelcoService;

    @InjectMocks
    private TransferenciaService transferenciaService;

    @BeforeEach
    void setUp(){
        // simulo q tiene 5000
        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(1001L);
        cuentaOrigen.setSaldo(5000.0);
        cuentaOrigen.setMoneda("PESOS");

        // simulo q tiene 1000
        Cuenta cuentaDestino = new Cuenta();
        cuentaDestino.setNumeroCuenta(1002L);
        cuentaDestino.setSaldo(1000.0);
        cuentaDestino.setMoneda("PESOS");

        //configuro los mockks para simular las respuestass dle repo
        // cuando el servicio busqe la cuenta 1001 en el repo, devuelvo la cuenta origen q cree para el test
        lenient().when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuentaOrigen));
        lenient().when(cuentaRepository.findByNumeroCuenta(1002L)).thenReturn(Optional.of(cuentaDestino));
        // cuando el servicio consulte la suma total d transascciones  dc cualqier tipo, devuelvo 0
        lenient().when(transaccionRepository.sumMontoByTipo(any())).thenReturn(0.0);
    }

    @Test
    void realizarTransferenciaExitosa() {
        // Preparar
        // creo solicitud de transferencia
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setCuentaOrigen(1001L);
        request.setCuentaDestino(1002L);
        request.setMonto(1000.0);
        request.setMoneda("PESOS");
    
        //ejecuto la trans
        TransferenciaResponseDTO response = transferenciaService.realizarTransferencia(request);
    
        // verifico q la transferencia fue exitosa
        assertEquals("Exitosa", response.getEstado());
        assertEquals("Transferencia realizada con éxito", response.getMensaje());
    }
}
