package com.example.SistemaBancario.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.SistemaBancario.exeption.CuentaException;
import com.example.SistemaBancario.model.Cliente;
import com.example.SistemaBancario.model.Cuenta;
import com.example.SistemaBancario.repository.CuentaRepository;
import com.example.SistemaBancario.repository.TransaccionRepository;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {
    @Mock
    private CuentaRepository cuentaRepository;    // mock del repositorio de cuentas

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private ClienteService clienteService;    // mock del servicio de clientes

    @InjectMocks
    private CuentaService cuentaService;    // servicio a testear

    private Cuenta cuenta;    // cuenta para usar en los tests
    private Cliente cliente;    // cliente para usar en los tests

    @BeforeEach
    void setUp() {    // se ejecuta antes de cada test
        cliente = new Cliente();
        cliente.setId(1L);

        cuenta = new Cuenta();
        cuenta.setNumeroCuenta(1L);
        cuenta.setSaldo(2000.0);
        cuenta.setMoneda("PESOS");
        cuenta.setCliente(cliente);
    }

    @Test
    void deberiaCrearCuentaValida() {    // test crear cuenta valida
        when(cuentaRepository.existsByNumeroCuenta(anyLong())).thenReturn(false);
        when(clienteService.obtenerCliente(anyLong())).thenReturn(cliente);
        when(cuentaRepository.save(any(Cuenta.class))).thenReturn(cuenta);

        Cuenta resultado = cuentaService.crearCuenta(cuenta);

        assertNotNull(resultado);
        assertEquals(cuenta.getNumeroCuenta(), resultado.getNumeroCuenta());
    }

    @Test
    void deberiaLanzarExcepcionCuandoMonedaInvalida() {    // test moneda invalida
        cuenta.setMoneda("EUROS");

        assertThrows(CuentaException.class, () -> 
            cuentaService.crearCuenta(cuenta));
    }

    @Test
    void deberiaLanzarExcepcionCuandoSaldoInsuficiente() {    // test saldo bajo
        cuenta.setSaldo(500.0);

        assertThrows(CuentaException.class, () -> 
            cuentaService.crearCuenta(cuenta));
    }

    @Test
    void deberiaObtenerCuentasPorCliente() {    // test obtener cuentas por cliente
        List<Cuenta> cuentas = Arrays.asList(cuenta);
        when(clienteService.obtenerCliente(anyLong())).thenReturn(cliente);
        when(cuentaRepository.findByClienteId(anyLong())).thenReturn(cuentas);

        List<Cuenta> resultado = cuentaService.obtenerCuentasPorCliente(1L);

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    void deberiaObtenerCuentasPorMoneda() {    // test obtener cuentas por moneda
        List<Cuenta> cuentas = Arrays.asList(cuenta);
        when(cuentaRepository.findByMoneda("PESOS")).thenReturn(cuentas);

        List<Cuenta> resultado = cuentaService.obtenerCuentasPorMoneda("PESOS");

        assertFalse(resultado.isEmpty());
        assertEquals("PESOS", resultado.get(0).getMoneda());
    }

    @Test
    void validarLimiteDiarioExcedido() {
        //creo una solicitud d trans d 600.000
        cuenta.setSaldo(1000000.0);
        cuenta.setMoneda("PESOS");

        // simulo cuenta con saldo suficiente 1.000.000
        when(transaccionRepository.sumMontoByTipo(any())).thenReturn(400000.0);// cuando pregunto cuanto se transfirio hoy, digo q 400.000

        // ejecuto y espero q falle pq ya se transfirieron 400.000 y qiero 600.000 o mas (superando limite diario)
        CuentaException exception = assertThrows(
            CuentaException.class,
            () -> cuentaService.validarLimiteDiario(600000.0, "PESOS")
        );

        assertEquals("Se ha superado el limite diario de transferencias en PESOS", exception.getMessage());
    }

    @Test
    void validarMontoMinimo() {
        // solicitud con monto menor al minimo q es 100
        cuenta.setSaldo(50.0);
        
        //verifico q se lanze la execcion x monto min
        CuentaException exception = assertThrows(
            CuentaException.class,
            () -> cuentaService.validarMontoMinimo(cuenta.getSaldo())
        );

        assertEquals("El monto minimo de transferencia es 100.0", exception.getMessage());
    }

    @Test
    void validarSaldoInsuficiente() {
        //cuenta con saldo insuficiente simulo
        cuenta.setSaldo(1000.0); // saldo menor al monto a validar
        
        //verifico q se lanze la execcion x saldo insufiente
        CuentaException exception = assertThrows(
            CuentaException.class,
            () -> cuentaService.validarSaldoSuficiente(cuenta, 5000.0)
        );

        assertEquals("Saldo insuficiente", exception.getMessage());
    }

    @Test
    void validarMonedaDiferente() {
        //simulo una cuenta en pesossss
        cuenta.setMoneda("PESOS");

        //verifico q se lanze la execcion x moneda q no coincide
        CuentaException exception = assertThrows(
            CuentaException.class,
            () -> cuentaService.validarMoneda(cuenta, "DOLARES")
        );

        assertEquals("La moneda de la cuenta no coincide", exception.getMessage());
    }

    @Test
    void registrarCreditoExitoso() {

        //registro el credito de 500 pe
        cuentaService.registrarCredito(cuenta, 500.0);

        // verifico
        verify(cuentaRepository).save(argThat(c -> 
            Math.abs(c.getSaldo() - 2500.0)< 0.0001)); // 2000 + 500
    }

    @Test
    void registrarDebitoExitoso() {
        //registro el debito de 500 pe
        cuentaService.registrarDebito(cuenta, 500.0);

        // verifico
        verify(cuentaRepository).save(argThat(c -> 
            Math.abs(c.getSaldo() - 1500.0)< 0.0001)); // 2000 - 500
    }
}
