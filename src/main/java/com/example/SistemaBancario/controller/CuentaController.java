package com.example.SistemaBancario.controller;

import com.example.SistemaBancario.dto.TransaccionDTO;
import com.example.SistemaBancario.dto.TransferenciaResponseDTO;
import com.example.SistemaBancario.enums.TipoTransaccion;
import com.example.SistemaBancario.model.Cuenta;
import com.example.SistemaBancario.service.CuentaService;
import com.example.SistemaBancario.service.TransferenciaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cuentas")
@CrossOrigin(origins = "*") // permito peticiones d cualqier tipo

public class CuentaController {
    @Autowired // inyeccion automatica
    private CuentaService cuentaService;

    @Autowired // inyeccion automatica
    private TransferenciaService transferenciaService;
    private static final String FALLIDA = "FALLIDA";


    // LO PROBE EN EL POSTMANN!!!!!!!
    @PostMapping // enponid POST para crear las cuentas
    public ResponseEntity<Cuenta> crearCuenta(@RequestBody @Valid Cuenta cuenta) { // recibo y valido datos de la cuenta
        Cuenta nuevaCuenta = cuentaService.crearCuenta(cuenta);
        if (nuevaCuenta == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 404, errror
        }
        return new ResponseEntity<>(nuevaCuenta, HttpStatus.CREATED); // 201, creadoo
    }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @GetMapping("/{numeroCuenta}") // endpoin GET para obtener la cuenta  x numero de cuenta
    public ResponseEntity<Cuenta> obtenerCuenta(@PathVariable Long numeroCuenta) {
        Cuenta cuenta = cuentaService.obtenerCuenta(numeroCuenta);
        if (cuenta == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404, noe encontrado
        }
        return new ResponseEntity<>(cuenta, HttpStatus.OK); // 200, exitoosss  
    }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @GetMapping("/{cuentaId}/saldo")// endpoint GET para consultar el saldo
    public ResponseEntity<Double> consultarSaldo(@PathVariable Long cuentaId) {
            Double saldo = transferenciaService.consultarSaldo(cuentaId);
            if (saldo == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404, no encontrado
            }
            return new ResponseEntity<>(saldo, HttpStatus.OK); // 200, exikto  
        
    }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @GetMapping("/cliente/{clienteId}") // endpoin GET para obtener la cuenta x cliente id
    public ResponseEntity<List<Cuenta>> obtenerCuentasPorCliente(@PathVariable Long clienteId) {
        List<Cuenta> cuentas = cuentaService.obtenerCuentasPorCliente(clienteId);
        if (cuentas == null || cuentas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404, no encontrado
        }
        return new ResponseEntity<>(cuentas, HttpStatus.OK); // 200, exikto    
    }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @GetMapping("/moneda/{moneda}") // endpoin GET para filtrar cuentas x tipo moneda
    public ResponseEntity<List<Cuenta>> obtenerCuentasPorMoneda(@PathVariable String moneda) {
        List<Cuenta> cuentas = cuentaService.obtenerCuentasPorMoneda(moneda);
        if (cuentas == null || cuentas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404, no encontrado
        }
        return new ResponseEntity<>(cuentas, HttpStatus.OK); // 200, exikto  
    }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @PutMapping("/{numeroCuenta}") // endpoin PUT para actualizar la cuenta existente
    public ResponseEntity<Cuenta> actualizarCuenta(
            @PathVariable Long numeroCuenta,
            @RequestBody @Valid Cuenta cuenta) {
        Cuenta cuentaActualizada = cuentaService.actualizarCuenta(numeroCuenta, cuenta);
        if (cuentaActualizada == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404, no encontrado
        }
        return new ResponseEntity<>(cuentaActualizada, HttpStatus.OK); // 200
    }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @DeleteMapping("/{numeroCuenta}") // endpoin DELETE para eliminar cuenta
    public ResponseEntity<Void> eliminarCuenta(@PathVariable Long numeroCuenta) { // recibo el numero d la cuenta a eliminar
        try{
            cuentaService.eliminarCuenta(numeroCuenta);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204, sin contenido
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);// 404, no encontrado

        }
    }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @GetMapping("/saldo-rango")  // endpoint GET para buscar cuentas en un rango de saldo
    public ResponseEntity<List<Cuenta>> obtenerCuentasPorRangoSaldo(
            @RequestParam Double minimo,
            @RequestParam Double maximo) {
        List<Cuenta> cuentas = cuentaService.findCuentasBySaldoRange(minimo, maximo);
        if (cuentas == null || cuentas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404, no encontrado
        }
        return new ResponseEntity<>(cuentas, HttpStatus.OK); // 200
    }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @GetMapping("/{cuentaId}/transacciones") // endpoint GET para obt el historial de ttransferencia
    public ResponseEntity<List<TransaccionDTO>> obtenerTransacciones(@PathVariable Long cuentaId) { // recibo el id de la cuenta
        List<TransaccionDTO> transacciones = transferenciaService.obtenerTransacciones(cuentaId);
        if (transacciones == null || transacciones.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404, no encontrado
        }
        return new ResponseEntity<>(transacciones, HttpStatus.OK); // 200, exikto
    }

    
    // LO PROBE EN EL POSTMANN!!!!!!!
    //endpoint POST para registrar creditos
    @PostMapping("/{cuentaId}/credito")
    public ResponseEntity<TransferenciaResponseDTO> registrarCredito(
            @PathVariable Long cuentaId,
            @RequestBody Double monto) {
        try {
            TransferenciaResponseDTO response = transferenciaService.registrarCredito(cuentaId, monto); // llamo al servicio
            return new ResponseEntity<>(response, HttpStatus.CREATED); // 201
        } catch (Exception e) {
            return new ResponseEntity<>(
                new TransferenciaResponseDTO(FALLIDA, e.getMessage()),
                HttpStatus.BAD_REQUEST); // 400, soli incorrecta
        }
    }

    // LO PROBE EN EL POSTMANN!!!!!!!
    // endpoint POST para registrar debitooos
    @PostMapping("/{cuentaId}/debito")
    public ResponseEntity<TransferenciaResponseDTO> registrarDebito(
            @PathVariable Long cuentaId,
            @RequestBody Double monto) {
                try {
                    TransferenciaResponseDTO response = transferenciaService.registrarDebito(cuentaId, monto); // llamo al servicio
                    return new ResponseEntity<>(response, HttpStatus.CREATED); // 201
                } catch (Exception e) {
                    return new ResponseEntity<>(
                        new TransferenciaResponseDTO(FALLIDA, e.getMessage()),
                        HttpStatus.BAD_REQUEST); // 400, soli incorrecta
                }
    }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @GetMapping("/{cuentaId}/transacciones/{tipo}") // endpoint GET para obt las tranciones de "tipo", ya sea credito,debito,entrante y saliente
    public ResponseEntity<List<TransaccionDTO>> obtenerTransaccionesPorTipo(
            @PathVariable Long cuentaId,
            @PathVariable TipoTransaccion tipo) {
                List<TransaccionDTO> transacciones = transferenciaService.obtenerTransaccionesPorTipo(cuentaId, tipo);
                if (transacciones == null || transacciones.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404, no encontrado
                }
                return new ResponseEntity<>(transacciones, HttpStatus.OK); // 200, exitosos
            }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @GetMapping("/{cuentaId}/totales") // endpoint GET para obt totales acumulados por cada tipo de transaccionn
    public ResponseEntity<Map<TipoTransaccion, Double>> obtenerTotalesPorTipo( // retorno un mapa con totales por tipo
            @PathVariable Long cuentaId) {
                Map<TipoTransaccion, Double> totales = transferenciaService.obtenerTotalesPorTipo(cuentaId);
                if (totales == null || totales.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404, no encontrado
                }
                return new ResponseEntity<>(totales, HttpStatus.OK); // 200, éxito
            }

    
}