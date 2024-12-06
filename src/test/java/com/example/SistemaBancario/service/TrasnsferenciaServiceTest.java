// package com.example.SistemaBancario.service;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;

// import com.example.SistemaBancario.model.Cuenta;
// import com.example.SistemaBancario.repository.CuentaRepository;
// import com.example.SistemaBancario.dto.TransferenciaRequestDTO;

// import com.example.SistemaBancario.repository.TransaccionRepository;

// @SpringBootTest
// public class TrasnsferenciaServiceTest {
//     @MockBean
//     private CuentaRepository cuentaRepository;

//     @MockBean
//     private TransaccionRepository transaccionRepository;

//     @Autowired
//     private TransferenciaService transferenciaService;

    
//     void realizarTransferenciaExitosa(){
//         Cuenta origen = new Cuenta();
//         origen.setNumeroCuenta(1006L);
//         origen.setSaldo(1000.0);
//         origen.getMoneda("PESOS");

//         Cuenta destino = new Cuenta();
//         destino.setNumeroCuenta(1005L);
//         destino.getSaldo(500.0);
//         destino.setMoneda("PESOS");

//         when(cuentaRepository.findByNumeroCuenta(1006L)).thenReturn(Optional.of(origen));
//         when(cuentaRepository.findByNumeroCuenta(1005L)).thenReturn(Optional.of(origen));

//         TransferenciaRequestDTO response = transferenciaService.realizarTransferencia(request);

//         assertEquals("EXITOSA", response.getEstado());
//     }
// }
