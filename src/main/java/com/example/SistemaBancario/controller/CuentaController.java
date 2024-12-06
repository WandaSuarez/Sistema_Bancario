package com.example.SistemaBancario.controller;

import com.example.SistemaBancario.model.Cuenta;
import com.example.SistemaBancario.service.CuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
@CrossOrigin(origins = "*")

public class CuentaController {
    @Autowired
    private CuentaService cuentaService;

    @PostMapping
    public ResponseEntity<Cuenta> crearCuenta(@RequestBody @Valid Cuenta cuenta) {
        return ResponseEntity.ok(cuentaService.crearCuenta(cuenta));
    }

    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<Cuenta> obtenerCuenta(@PathVariable Long numeroCuenta) {
        return ResponseEntity.ok(cuentaService.obtenerCuenta(numeroCuenta));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Cuenta>> obtenerCuentasPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(cuentaService.obtenerCuentasPorCliente(clienteId));
    }

    @GetMapping("/moneda/{moneda}")
    public ResponseEntity<List<Cuenta>> obtenerCuentasPorMoneda(@PathVariable String moneda) {
        return ResponseEntity.ok(cuentaService.obtenerCuentasPorMoneda(moneda));
    }

    @PutMapping("/{numeroCuenta}")
    public ResponseEntity<Cuenta> actualizarCuenta(
            @PathVariable Long numeroCuenta,
            @RequestBody @Valid Cuenta cuenta) {
        return ResponseEntity.ok(cuentaService.actualizarCuenta(numeroCuenta, cuenta));
    }

    @DeleteMapping("/{numeroCuenta}")
    public ResponseEntity<Void> eliminarCuenta(@PathVariable Long numeroCuenta) {
        cuentaService.eliminarCuenta(numeroCuenta);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/saldo-mayor/{saldo}")
    public ResponseEntity<List<Cuenta>> obtenerCuentasConSaldoMayor(@PathVariable Double saldo) {
        return ResponseEntity.ok(cuentaService.obtenerCuentasConSaldoMayor(saldo));
    }

    @GetMapping("/saldo-rango")
    public ResponseEntity<List<Cuenta>> obtenerCuentasPorRangoSaldo(
        @RequestParam Double minimo, 
        @RequestParam Double maximo) {
        return ResponseEntity.ok(cuentaService.findCuentasBySaldoRange(minimo, maximo));
    }

}
