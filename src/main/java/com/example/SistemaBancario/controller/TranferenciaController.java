package com.example.SistemaBancario.controller;

import com.example.SistemaBancario.dto.TransferenciaRequestDTO;
import com.example.SistemaBancario.dto.TransferenciaResponseDTO;
import com.example.SistemaBancario.enums.TipoTransaccion;
import com.example.SistemaBancario.dto.TransaccionDTO;

import com.example.SistemaBancario.service.TransferenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api") // ruta para lps endpoints
@CrossOrigin(origins = "*")
public class TranferenciaController {
    
    private TransferenciaService transferenciaService;
    private static final String FALLIDA = "FALLIDA";

    //@Autowired // inyeccion automatica
    public TranferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @PostMapping("/transfer") // endpoint POST para realizar transferencia
    public ResponseEntity<TransferenciaResponseDTO> realizarTransferencia(
            @RequestBody @Valid TransferenciaRequestDTO request) { //recibo y valido dattos
        try {
            TransferenciaResponseDTO response = transferenciaService.realizarTransferencia(request); // llamo al servicio d transferencia y guardo el resultado en response
            return ResponseEntity.ok(response);// si la transferencia es exitosa, de vuelvo un 200 
        } catch (Exception e) { // si ocurre algo inesperado
            return ResponseEntity.badRequest() // devuelvo un 400
                .body(new TransferenciaResponseDTO(FALLIDA, e.getMessage()));
        }
    }

    @GetMapping("/cuenta/{cuentaId}/transacciones") // endpoint GET para obt el historial de ttransferencia
    public ResponseEntity<List<TransaccionDTO>> obtenerTransacciones(
            @PathVariable Long cuentaId) { // recibo el id de la cuenta
        try {
            List<TransaccionDTO> transacciones = transferenciaService.obtenerTransacciones(cuentaId);
            return ResponseEntity.ok(transacciones); // devuelvo lsiat de transaccciones
        } catch (Exception e) {
            return ResponseEntity.notFound().build(); // 404 si no se encontro la cuenta
        }
    }

    @GetMapping("/cuenta/{cuentaId}/saldo")// endpoint GET para consultar el saldo
    public ResponseEntity<Double> consultarSaldo(@PathVariable Long cuentaId) {
        try {
            Double saldo = transferenciaService.consultarSaldo(cuentaId);
            return ResponseEntity.ok(saldo);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // endpoint GET para obt ultimas transacciones
    @GetMapping("/cuenta/{cuentaId}/ultimas-transacciones")
    public ResponseEntity<List<TransaccionDTO>> obtenerUltimasTransacciones(
            @PathVariable Long cuentaId) {
        try {
            List<TransaccionDTO> transacciones = transferenciaService.obtenerUltimasTransacciones(cuentaId);
            return ResponseEntity.ok(transacciones);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/cuenta/{cuentaId}/transacciones/{tipo}") // endpoint GET para obt las tranciones de "tipo", ya sea credito,debito,entrante y saliente
    public ResponseEntity<List<TransaccionDTO>> obtenerTransaccionesPorTipo(
            @PathVariable Long cuentaId,
            @PathVariable TipoTransaccion tipo) {
        return ResponseEntity.ok(transferenciaService.obtenerTransaccionesPorTipo(cuentaId, tipo));
    }

    @GetMapping("/cuenta/{cuentaId}/totales") // endpoint GET para obt totales acumulados por cada tipo de transaccionn
    public ResponseEntity<Map<TipoTransaccion, Double>> obtenerTotalesPorTipo( // retorno un mapa con totales por tipo
            @PathVariable Long cuentaId) {
        return ResponseEntity.ok(transferenciaService.obtenerTotalesPorTipo(cuentaId)); // llamo al servicio y retorno al resultamo
    }

    //endpoint POST para registrar creditos
    @PostMapping("/cuenta/{cuentaId}/credito")
    public ResponseEntity<TransferenciaResponseDTO> registrarCredito(
            @PathVariable Long cuentaId,
            @RequestBody Double monto) {
        try {
            TransferenciaResponseDTO response = transferenciaService.registrarCredito(cuentaId, monto); // llamo al servicio
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new TransferenciaResponseDTO(FALLIDA, e.getMessage()));
        }
    }

    // endpoint POST para registrar debitooos
    @PostMapping("/cuenta/{cuentaId}/debito")
    public ResponseEntity<TransferenciaResponseDTO> registrarDebito(
            @PathVariable Long cuentaId,
            @RequestBody Double monto) {
        try {
            TransferenciaResponseDTO response = transferenciaService.registrarDebito(cuentaId, monto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new TransferenciaResponseDTO(FALLIDA, e.getMessage()));
        }
    }

}
