package com.example.SistemaBancario.controller;

import com.example.SistemaBancario.dto.TransferenciaRequestDTO;
import com.example.SistemaBancario.dto.TransferenciaResponseDTO;
import com.example.SistemaBancario.dto.TransaccionDTO;

import com.example.SistemaBancario.service.TransferenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TranferenciaController {
    @Autowired
    private TransferenciaService transferenciaService;

    @PostMapping("/transfer")
    public ResponseEntity<TransferenciaResponseDTO> realizarTransferencia(
            @RequestBody @Valid TransferenciaRequestDTO request) {
        try {
            TransferenciaResponseDTO response = transferenciaService.realizarTransferencia(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new TransferenciaResponseDTO("FALLIDA", e.getMessage()));
        }
    }

    @GetMapping("/cuenta/{cuentaId}/transacciones")
    public ResponseEntity<List<TransaccionDTO>> obtenerTransacciones(
            @PathVariable Long cuentaId) {
        try {
            List<TransaccionDTO> transacciones = transferenciaService.obtenerTransacciones(cuentaId);
            return ResponseEntity.ok(transacciones);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/cuenta/{cuentaId}/saldo")
    public ResponseEntity<Double> consultarSaldo(@PathVariable Long cuentaId) {
        try {
            Double saldo = transferenciaService.consultarSaldo(cuentaId);
            return ResponseEntity.ok(saldo);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/cuenta/{cuentaId}/ultimas-transacciones")
    public ResponseEntity<List<TransaccionDTO>> obtenerUltimasTransacciones(
            @PathVariable Long cuentaId,
            @RequestParam(defaultValue = "10") int limite) {
        try {
            List<TransaccionDTO> transacciones = transferenciaService.obtenerUltimasTransacciones(cuentaId, limite);
            return ResponseEntity.ok(transacciones);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
