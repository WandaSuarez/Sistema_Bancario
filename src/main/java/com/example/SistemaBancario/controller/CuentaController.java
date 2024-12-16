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
@CrossOrigin(origins = "*") // permito peticiones d cualqier tipo

public class CuentaController {
    @Autowired // inyeccion automatica
    private CuentaService cuentaService;

    @PostMapping // enponid POST para crear las cuentas
    public ResponseEntity<Cuenta> crearCuenta(@RequestBody @Valid Cuenta cuenta) { // recibo y valido datos de la cuenta
        return ResponseEntity.ok(cuentaService.crearCuenta(cuenta));// devuelvo la cuenta creada
    }

    @GetMapping("/{numeroCuenta}") // endpoin GET para obtener la cuenta  x numero de cuenta
    public ResponseEntity<Cuenta> obtenerCuenta(@PathVariable Long numeroCuenta) {
        return ResponseEntity.ok(cuentaService.obtenerCuenta(numeroCuenta)); // devuelvo el num d cuenta encontrado
    }

    @GetMapping("/cliente/{clienteId}") // endpoin GET para obtener la cuenta x cliente id
    public ResponseEntity<List<Cuenta>> obtenerCuentasPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(cuentaService.obtenerCuentasPorCliente(clienteId)); // devuelvo la lista de cuentas del cliente id
    }

    @GetMapping("/moneda/{moneda}") // endpoin GET para filtrar cuentas x tipo moneda
    public ResponseEntity<List<Cuenta>> obtenerCuentasPorMoneda(@PathVariable String moneda) {
        return ResponseEntity.ok(cuentaService.obtenerCuentasPorMoneda(moneda)); // devunevo la lista de cuentas con esa moneda 
    }

    @PutMapping("/{numeroCuenta}") // endpoin PUT para actualizar la cuenta existente
    public ResponseEntity<Cuenta> actualizarCuenta(
            @PathVariable Long numeroCuenta,
            @RequestBody @Valid Cuenta cuenta) { // nuevos datos de la cuenta, el requestBody ->convierte el jsson q llega en la peticion a un obj
        return ResponseEntity.ok(cuentaService.actualizarCuenta(numeroCuenta, cuenta)); // devuelvo la cuenta actualizada
    }

    @DeleteMapping("/{numeroCuenta}") // endpoin DELETE para eliminar cuenta
    public ResponseEntity<Void> eliminarCuenta(@PathVariable Long numeroCuenta) { // recibo el numero d la cuenta a eliminar
        cuentaService.eliminarCuenta(numeroCuenta); // elimino
        return ResponseEntity.ok().build();// retorno repuesta exitosa sin contenido -> build -> construye la repuesta http final, s usa cuando no hay datos q deovolver, solo el estado del codigo
    }

    @GetMapping("/saldo-mayor/{saldo}") // endpoin GET para filtrar cuentas x saldo minimo
    public ResponseEntity<List<Cuenta>> obtenerCuentasConSaldoMayor(@PathVariable Double saldo) { // recibo el saldo cm variable d ruta
        return ResponseEntity.ok(cuentaService.obtenerCuentasConSaldoMayor(saldo)); // devuelvo cuentas q superan el saldo
    }

    @GetMapping("/saldo-rango")  // endpoint GET para buscar cuentas en un rango de saldo
    public ResponseEntity<List<Cuenta>> obtenerCuentasPorRangoSaldo(
        @RequestParam Double minimo, //saldo minimp
        @RequestParam Double maximo) { //saldo maximo
        return ResponseEntity.ok(cuentaService.findCuentasBySaldoRange(minimo, maximo)); // devuelvo cuentas dentro del rango
    }

}