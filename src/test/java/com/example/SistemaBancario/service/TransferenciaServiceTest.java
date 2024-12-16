package com.example.SistemaBancario.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

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
import com.example.SistemaBancario.dto.TransaccionDTO;
import com.example.SistemaBancario.dto.TransferenciaRequestDTO;
import com.example.SistemaBancario.dto.TransferenciaResponseDTO;
import com.example.SistemaBancario.enums.TipoTransaccion;
import com.example.SistemaBancario.model.Cuenta;
import com.example.SistemaBancario.model.Transaccion;

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
    
    

    @Test
    void validarLimiteDiarioExcedido() {
        //creo una solicitud d trans d 600.000
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setCuentaOrigen(1001L);
        request.setCuentaDestino(1002L);
        request.setMonto(600000.0);
        request.setMoneda("PESOS");

        // simulo cuenta con saldo suficiente 1.000.000
        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(1001L);
        cuentaOrigen.setSaldo(1000000.0);
        cuentaOrigen.setMoneda("PESOS");

        lenient().when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuentaOrigen)); // cuando busco la cuenta 1001, devuelvo esta cuenta q tiene saldo sufi
        lenient().when(transaccionRepository.sumMontoByTipo(any())).thenReturn(400000.0);// cuando pregunto cuanto se transfirio hoy, digo q 400.000

        // ejecuto y espero q falle pq ya se transfirieron 400.000 y qiero 600.000 o mas (superando limite diario)
        TransferenciaException exception = assertThrows(
            TransferenciaException.class,
            () -> transferenciaService.realizarTransferencia(request)
        );

        assertEquals("Se ha superado el limite diario de transferencias en PESOS", exception.getMessage());
    }

    @Test
    void validarMontoMinimoTransferencia() {
        // solicitud con monto menor al minimo q es 100
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setCuentaOrigen(1001L);
        request.setCuentaDestino(1002L);
        request.setMonto(50.0);
        request.setMoneda("PESOS");

        //verifico q se lanze la execcion x monto min
        TransferenciaException exception = assertThrows(
            TransferenciaException.class,
            () -> transferenciaService.realizarTransferencia(request)
        );

        assertEquals("El monto minimo de transferencia es 100.0", exception.getMessage());
    }


    @Test
    void validarSaldoInsuficiente() {
        // solicitud de 5.000
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setCuentaOrigen(1001L);
        request.setCuentaDestino(1002L);
        request.setMonto(5000.0);
        request.setMoneda("PESOS");

        //cuenta con saldo insuficiente simulo
        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(1001L);
        cuentaOrigen.setSaldo(1000.0); // saldo menor al monto a transferir
        cuentaOrigen.setMoneda("PESOS");

        when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuentaOrigen));// cuando busco la cuenta 1001, devuelvo la cuenta q tiene 1.000 d saldo
        when(transaccionRepository.sumMontoByTipo(any())).thenReturn(0.0);//cuando pregunto x el total d transferencia d el dia , devulvo 0

        //verifico q se lanze la execcion x saldo insufiente
        TransferenciaException exception = assertThrows(
            TransferenciaException.class,
            () -> transferenciaService.realizarTransferencia(request)
        );

        assertEquals("Saldo insuficiente", exception.getMessage());
    }


    @Test
    void validarMonedaDiferente() {
        //solcitud d cuenta en dolares
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setCuentaOrigen(1001L);
        request.setCuentaDestino(1002L);
        request.setMonto(1000.0);
        request.setMoneda("DOLARES");

        //simulo una cuenta en pesossss
        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(1001L);
        cuentaOrigen.setSaldo(5000.0);
        cuentaOrigen.setMoneda("PESOS"); // moneda diferente

        when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuentaOrigen)); //configuro q al buscar la cuenta, devuelva una en pesos
        when(transaccionRepository.sumMontoByTipo(any())).thenReturn(0.0); //cuando pregunto x el total d transferencia d el dia , devulvo 0

        //verifico q se lanze la execcion x moneda q no coincide
        TransferenciaException exception = assertThrows(
            TransferenciaException.class,
            () -> transferenciaService.realizarTransferencia(request)
        );

        assertEquals("La moneda de la cuenta origen no coincide", exception.getMessage());
    }

    @Test
    void validarCargoPesos() {
        // solicitud d trans de 400.000 pesosss
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setCuentaOrigen(1001L);
        request.setCuentaDestino(1002L);
        request.setMonto(400000.0); // monto dentro del limite diario
        request.setMoneda("PESOS");

        // cuenta con 50.000 pesos
        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(1001L);
        cuentaOrigen.setSaldo(500000.0);
        cuentaOrigen.setMoneda("PESOS");

        // cuenta con 1.000 pesos
        Cuenta cuentaDestino = new Cuenta();
        cuentaDestino.setNumeroCuenta(1002L);
        cuentaDestino.setSaldo(1000.0);
        cuentaDestino.setMoneda("PESOS");

        when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuentaOrigen)); // cuando busco la cuenta, devuelvo la q cree
        when(cuentaRepository.findByNumeroCuenta(1002L)).thenReturn(Optional.of(cuentaDestino));
        when(transaccionRepository.sumMontoByTipo(any())).thenReturn(0.0); // cuando pregunto el total d transferenfia, devuelvo 0

        //ejecuto
        transferenciaService.realizarTransferencia(request);

        //verifico q el saldo final sea 100.000 (500.000 - 400.000)
        verify(cuentaRepository).save(argThat(cuenta ->
            cuenta.getNumeroCuenta().equals(1001L) &&
            Math.abs(cuenta.getSaldo() - 100000.0) < 0.01 // 500.000 - 400.000
        ));
    }

    @Test
    void validarCargoDolares() {
        //creo soli d trans de 6.000 dolares
        TransferenciaRequestDTO request = new TransferenciaRequestDTO();
        request.setCuentaOrigen(1001L);
        request.setCuentaDestino(1002L);
        request.setMonto(6000.0); // monto mayor a 5000 
        request.setMoneda("DOLARES");

        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(1001L);
        cuentaOrigen.setSaldo(10000.0); // cuenta origen con 10.000 dolares
        cuentaOrigen.setMoneda("DOLARES");

        Cuenta cuentaDestino = new Cuenta();
        cuentaDestino.setNumeroCuenta(1002L);
        cuentaDestino.setSaldo(1000.0);// cuenta origen con 1.000 dolares
        cuentaDestino.setMoneda("DOLARES");

        when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuentaOrigen));// cuando busco la cuenta, devuelvo la q cree
        when(cuentaRepository.findByNumeroCuenta(1002L)).thenReturn(Optional.of(cuentaDestino));
        when(transaccionRepository.sumMontoByTipo(any())).thenReturn(0.0);//cuando pregunto x el total d transferencia d el dia , devulvo 0

        transferenciaService.realizarTransferencia(request);

        // verfico q se guarde en el repo una cuenta 1001 y saldo final d 3970 dps d restar la trans y el cargo
        verify(cuentaRepository).save(argThat(cuenta ->
            cuenta.getNumeroCuenta().equals(1001L) &&
            Math.abs(cuenta.getSaldo() - 3970.0) < 0.01 // 10000 - (6000 + 0.5% cargo)
        ));
    }


    @Test
    void registrarCreditoExitoso() {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(1001L);
        cuenta.setSaldo(1000.0);
        cuenta.setMoneda("PESOS");

        when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuenta)); // configuro q devuelva la cuenta al buscarlo

        //registro el credito de 500 pe
        TransferenciaResponseDTO response = transferenciaService.registrarCredito(1001L, 500.0);

        // verifico
        assertEquals("Exitosa", response.getEstado());
        assertEquals("Credito registrado con exito", response.getMensaje());
        verify(transaccionRepository, times(1)).save(any());
    }

    @Test
    void registrarDebitoSaldoInsuficiente() {
        // creo cuenta con saldo insuficiente
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(1001L);
        cuenta.setSaldo(100.0);
        cuenta.setMoneda("PESOS");

        when(cuentaRepository.findByNumeroCuenta(1001L)).thenReturn(Optional.of(cuenta));// configuro q devuelva la cuenta al buscarlo

        //verfico q lanzr la exepccion al intentar debitat mas del saldo dispo
        TransferenciaException exception = assertThrows(
            TransferenciaException.class,
            () -> transferenciaService.registrarDebito(1001L, 500.0)
        );

        //verifico msk de eror
        assertEquals("Saldo insuficiente", exception.getMessage());
    }

    @Test
    void obtenerTransaccionesExitoso() {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(1001L);

        Transaccion transaccion = new Transaccion();
        transaccion.setCuenta(cuenta);
        transaccion.setMonto(1000.0);
        transaccion.setTipo(TipoTransaccion.CREDITO);
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setDescripcionBreve("Test");

        // cuando busco la cuenta q cree, devuelvo la lista d transacciones
        when(transaccionRepository.findByCuentaNumeroCuentaOrderByFechaDesc(1001L))
            .thenReturn(List.of(transaccion));

        //obtengo las lista de transacciones
        List<TransaccionDTO> resultado = transferenciaService.obtenerTransacciones(1001L);

        //verifico q la lista no este vacia y tenga almenos un elemento
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    void calcularTotalPorTipoExitoso() {
        // cuando s pida la suma d todas las transacciones d tipo credito, devuelvo 1.500
        when(transaccionRepository.sumMontoByCuentaNumeroCuentaAndTipo(1001L, TipoTransaccion.CREDITO))
            .thenReturn(1500.0);

        // llamo al metodo real q calcule el total d transacciones
        Double total = transferenciaService.calcularTotalPorTipo(1001L, TipoTransaccion.CREDITO);

        //verfico q el total sea 1.500
        assertEquals(1500.0, total);
    }
}
