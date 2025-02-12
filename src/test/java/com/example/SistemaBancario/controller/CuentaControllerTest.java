package com.example.SistemaBancario.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.SistemaBancario.dto.TransaccionDTO;
import com.example.SistemaBancario.dto.TransferenciaResponseDTO;
import com.example.SistemaBancario.enums.TipoTransaccion;
import com.example.SistemaBancario.model.Cuenta;
import com.example.SistemaBancario.service.CuentaService;
import com.example.SistemaBancario.service.TransferenciaService;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CuentaControllerTest {
    
    @Mock
    private CuentaService cuentaService;    // mock del servicio de cuentas

    @Mock
    private TransferenciaService transferenciaService;    // mock del servicio de transferencias

    @InjectMocks
    private CuentaController cuentaController;    // inyecta los mocks en el controller

    private Cuenta cuenta;    // cuenta para usar en los tests

    @BeforeEach
    void setUp() {    // se ejecutoo antes de cada test
        cuenta = new Cuenta();
        cuenta.setNumeroCuenta(1L);
        cuenta.setSaldo(1000.0);
        cuenta.setMoneda("PESOS");
    }

    @Test
    void deberiaCrearCuenta() {    // test de crear cuenta
        when(cuentaService.crearCuenta(any(Cuenta.class))).thenReturn(cuenta);    // simulo creacion exitosa

        ResponseEntity<Cuenta> response = cuentaController.crearCuenta(cuenta);    // ejecutoo el endpoint

        assertEquals(HttpStatus.CREATED, response.getStatusCode());    // veriifico codigo 201
        assertEquals(cuenta, response.getBody());    // veriifico que devuelve la cuenta
    }

    @Test
    void deberiaObtenerCuentaPorNumero() {    // test de obtener por numero
        when(cuentaService.obtenerCuenta(1L)).thenReturn(cuenta);    // simulo busqueda exitosa

        ResponseEntity<Cuenta> response = cuentaController.obtenerCuenta(1L);    // ejecutoo el endpoint

        assertEquals(HttpStatus.OK, response.getStatusCode());    // veriifico codigo 200
        assertEquals(cuenta, response.getBody());    // veriifico que devuelve la cuenta
    }

    @Test
    void deberiaConsultarSaldo() {    // test de consultar saldo
        when(transferenciaService.consultarSaldo(1L)).thenReturn(1000.0);    // simulo consulta exitosa

        ResponseEntity<Double> response = cuentaController.consultarSaldo(1L);    // ejecutoo el endpoint

        assertEquals(HttpStatus.OK, response.getStatusCode());    // veriifico codigo 200
        assertEquals(1000.0, response.getBody());    // veriifico el saldo
    }

    @Test
    void deberiaObtenerCuentasPorCliente() {    // test de obtener cuentas por cliente
        List<Cuenta> cuentas = Arrays.asList(cuenta);
        when(cuentaService.obtenerCuentasPorCliente(1L)).thenReturn(cuentas);    // simulo busqueda exitosa

        ResponseEntity<List<Cuenta>> response = cuentaController.obtenerCuentasPorCliente(1L);    // ejecutoo el endpoint

        assertEquals(HttpStatus.OK, response.getStatusCode());    // veriifico codigo 200
        assertEquals(cuentas, response.getBody());    // veriifico la lista de cuentas
    }

    @Test
    void deberiaRegistrarCredito() {    // test de registrar credito
        TransferenciaResponseDTO responseDTO = new TransferenciaResponseDTO("EXITOSA", "Credito registrado");
        when(transferenciaService.registrarCredito(1L, 500.0)).thenReturn(responseDTO);    // simulo registro exitoso

        ResponseEntity<TransferenciaResponseDTO> response = cuentaController.registrarCredito(1L, 500.0);    // ejecutoo el endpoint

        assertEquals(HttpStatus.CREATED, response.getStatusCode());    // veriifico codigo 201
        assertEquals(responseDTO, response.getBody());    // veriifico la respuesta
    }

    @Test
    void deberiaObtenerTransaccionesPorTipo() {    // test de obtener transacciones por tipo
        List<TransaccionDTO> transacciones = Arrays.asList(new TransaccionDTO());
        when(transferenciaService.obtenerTransaccionesPorTipo(1L, TipoTransaccion.CREDITO)).thenReturn(transacciones);    // simulo busqueda exitosa

        ResponseEntity<List<TransaccionDTO>> response = cuentaController.obtenerTransaccionesPorTipo(1L, TipoTransaccion.CREDITO);    // ejecutoo el endpoint

        assertEquals(HttpStatus.OK, response.getStatusCode());    // veriifico codigo 200
        assertEquals(transacciones, response.getBody());    // veriifico la lista de transacciones
    }
}
