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
import com.example.SistemaBancario.service.TransferenciaService.TransferenciaException;
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
        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(1001L);
        cuentaOrigen.setSaldo(5000.0);
        cuentaOrigen.setMoneda("PESOS");

        Cuenta cuentaDestino = new Cuenta();
        cuentaDestino.setNumeroCuenta(1002L);
        cuentaDestino.setSaldo(1000.0);
        cuentaDestino.setMoneda("PESOS");

        lenient().when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuentaOrigen));
        lenient().when(cuentaRepository.findByNumeroCuenta(1002L)).thenReturn(Optional.of(cuentaDestino));
        lenient().when(transaccionRepository.sumMontoByTipo(any())).thenReturn(0.0);
    }

    @Test
    void realizarTransferenciaExitosa() {
        // Arrange
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setCuentaOrigen(1001L);
        request.setCuentaDestino(1002L);
        request.setMonto(1000.0);
        request.setMoneda("PESOS");

        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(1001L);
        cuentaOrigen.setSaldo(5000.0);
        cuentaOrigen.setMoneda("PESOS");

        Cuenta cuentaDestino = new Cuenta();
        cuentaDestino.setNumeroCuenta(1002L);
        cuentaDestino.setSaldo(1000.0);
        cuentaDestino.setMoneda("PESOS");

        lenient().when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuentaOrigen));
        lenient().when(cuentaRepository.findByNumeroCuenta(1002L)).thenReturn(Optional.of(cuentaDestino));
        lenient().when(transaccionRepository.sumMontoByTipo(any())).thenReturn(0.0);

        // Act
        TransferenciaResponseDTO response = transferenciaService.realizarTransferencia(request);

        // Assert
        assertEquals("EXITOSA", response.getEstado());
        assertEquals("Transferencia realizada con éxito", response.getMensaje());
    }
    

    @Test
    void validarLimiteDiarioExcedido() {
        // Arrange
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setCuentaOrigen(1001L);
        request.setCuentaDestino(1002L);
        request.setMonto(600000.0);
        request.setMoneda("PESOS");

        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(1001L);
        cuentaOrigen.setSaldo(1000000.0);
        cuentaOrigen.setMoneda("PESOS");

        lenient().when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuentaOrigen));
        lenient().when(transaccionRepository.sumMontoByTipo(any())).thenReturn(400000.0);

        // Act & Assert
        TransferenciaException exception = assertThrows(
            TransferenciaException.class,
            () -> transferenciaService.realizarTransferencia(request)
        );

        assertEquals("Se ha superado el límite diario de transferencias en PESOS", exception.getMessage());
    }

    @Test
    void validarMontoMinimoTransferencia() {
        // Arrange
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setCuentaOrigen(1001L);
        request.setCuentaDestino(1002L);
        request.setMonto(50.0);
        request.setMoneda("PESOS");

        // Act & Assert
        TransferenciaException exception = assertThrows(
            TransferenciaException.class,
            () -> transferenciaService.realizarTransferencia(request)
        );

        assertEquals("El monto mínimo de transferencia es 100.0", exception.getMessage());
    }


    @Test
    void validarSaldoInsuficiente() {
        // Arrange
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setCuentaOrigen(1001L);
        request.setCuentaDestino(1002L);
        request.setMonto(5000.0);
        request.setMoneda("PESOS");

        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(1001L);
        cuentaOrigen.setSaldo(1000.0); // Saldo menor al monto a transferir
        cuentaOrigen.setMoneda("PESOS");

        when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuentaOrigen));
        when(transaccionRepository.sumMontoByTipo(any())).thenReturn(0.0);

        // Act & Assert
        TransferenciaException exception = assertThrows(
            TransferenciaException.class,
            () -> transferenciaService.realizarTransferencia(request)
        );

        assertEquals("Saldo insuficiente", exception.getMessage());
    }


    @Test
    void validarMonedaDiferente() {
        // Arrange
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setCuentaOrigen(1001L);
        request.setCuentaDestino(1002L);
        request.setMonto(1000.0);
        request.setMoneda("DOLARES");

        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(1001L);
        cuentaOrigen.setSaldo(5000.0);
        cuentaOrigen.setMoneda("PESOS"); // Moneda diferente a la de la transferencia

        when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuentaOrigen));
        when(transaccionRepository.sumMontoByTipo(any())).thenReturn(0.0);

        // Act & Assert
        TransferenciaException exception = assertThrows(
            TransferenciaException.class,
            () -> transferenciaService.realizarTransferencia(request)
        );

        assertEquals("La moneda de la cuenta origen no coincide", exception.getMessage());
    }

    @Test
    void validarCargoPesos() {
        // Arrange
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setCuentaOrigen(1001L);
        request.setCuentaDestino(1002L);
        request.setMonto(400000.0); // Monto reducido para estar dentro del límite diario
        request.setMoneda("PESOS");

        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(1001L);
        cuentaOrigen.setSaldo(500000.0);
        cuentaOrigen.setMoneda("PESOS");

        Cuenta cuentaDestino = new Cuenta();
        cuentaDestino.setNumeroCuenta(1002L);
        cuentaDestino.setSaldo(1000.0);
        cuentaDestino.setMoneda("PESOS");

        when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findByNumeroCuenta(1002L)).thenReturn(Optional.of(cuentaDestino));
        when(transaccionRepository.sumMontoByTipo(any())).thenReturn(0.0);

        // Act
        transferenciaService.realizarTransferencia(request);

        // Assert
        verify(cuentaRepository).save(argThat(cuenta -> 
            cuenta.getNumeroCuenta().equals(1001L) && 
            Math.abs(cuenta.getSaldo() - 100000.0) < 0.01 // 500000 - 400000
        ));
    }

    @Test
    void validarCargoDolares() {
        // Arrange
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setCuentaOrigen(1001L);
        request.setCuentaDestino(1002L);
        request.setMonto(6000.0); // Monto mayor a 5000 USD
        request.setMoneda("DOLARES");

        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(1001L);
        cuentaOrigen.setSaldo(10000.0);
        cuentaOrigen.setMoneda("DOLARES");

        Cuenta cuentaDestino = new Cuenta();
        cuentaDestino.setNumeroCuenta(1002L);
        cuentaDestino.setSaldo(1000.0);
        cuentaDestino.setMoneda("DOLARES");

        when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuentaOrigen));
        when(cuentaRepository.findByNumeroCuenta(1002L)).thenReturn(Optional.of(cuentaDestino));
        when(transaccionRepository.sumMontoByTipo(any())).thenReturn(0.0);

        // Act
        transferenciaService.realizarTransferencia(request);

        // Assert
        verify(cuentaRepository).save(argThat(cuenta -> 
            cuenta.getNumeroCuenta().equals(1001L) && 
            Math.abs(cuenta.getSaldo() - 3970.0) < 0.01 // 10000 - (6000 + 0.5% cargo)
        ));
    }


}
