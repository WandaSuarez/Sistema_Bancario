package com.example.SistemaBancario.service;

import com.example.SistemaBancario.model.Cuenta;
import com.example.SistemaBancario.model.Cliente;
import com.example.SistemaBancario.repository.CuentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class CuentaService {
    @Autowired
    private CuentaRepository cuentaRepository;
    
    @Autowired
    private ClienteService clienteService;

    private static final double SALDO_MINIMO_PESOS = 1000.0;
    private static final double SALDO_MINIMO_DOLARES = 100.0;
    private static final double SALDO_MAXIMO = 10000000.0;

    public Cuenta crearCuenta(Cuenta cuenta) {
        if (cuentaRepository.existsByNumeroCuenta(cuenta.getNumeroCuenta())) {
            throw new RuntimeException("Ya existe una cuenta con ese número");
        }
        
        // Buscar y vincular el cliente
        Cliente cliente = clienteService.obtenerCliente(cuenta.getCliente().getId());
        cuenta.setCliente(cliente);
        
        validarTipoMoneda(cuenta.getMoneda());
        validarSaldoInicial(cuenta.getSaldo(), cuenta.getMoneda());
        validarLimitesCuenta(cuenta.getSaldo());
        
        return cuentaRepository.save(cuenta);
    }

    public Cuenta obtenerCuenta(Long numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
            .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
    }

    public List<Cuenta> obtenerCuentasPorCliente(Long clienteId) {
        // Verificar que el cliente existe
        clienteService.obtenerCliente(clienteId);
        return cuentaRepository.findByClienteId(clienteId);
    }

    public List<Cuenta> obtenerCuentasPorMoneda(String moneda) {
        validarTipoMoneda(moneda);
        return cuentaRepository.findByMoneda(moneda);
    }

    public Cuenta actualizarCuenta(Long numeroCuenta, Cuenta cuenta) {
        Cuenta cuentaExistente = obtenerCuenta(numeroCuenta);
        
        validarTipoMoneda(cuenta.getMoneda());
        validarLimitesCuenta(cuenta.getSaldo());
        
        cuenta.setNumeroCuenta(numeroCuenta);
        cuenta.setCliente(cuentaExistente.getCliente());
        
        return cuentaRepository.save(cuenta);
    }

    public void eliminarCuenta(Long numeroCuenta) {
        if (!cuentaRepository.existsByNumeroCuenta(numeroCuenta)) {
            throw new RuntimeException("Cuenta no encontrada");
        }
        cuentaRepository.deleteByNumeroCuenta(numeroCuenta);
    }

    public List<Cuenta> obtenerCuentasConSaldoMayor(Double saldo) {
        return cuentaRepository.findBySaldoGreaterThan(saldo);
    }

    public List<Cuenta> obtenerCuentasPorRangoDeSaldo(Double saldoMinimo, Double saldoMaximo) {
        return cuentaRepository.findCuentasBySaldoRange(saldoMinimo, saldoMaximo);
    }

    public void validarPropietarioCuenta(Long numeroCuenta, Long clienteId) {
        Cuenta cuenta = obtenerCuenta(numeroCuenta);
        if (!cuenta.getCliente().getId().equals(clienteId)) {
            throw new RuntimeException("La cuenta no pertenece al cliente");
        }
    }

    private void validarTipoMoneda(String moneda) {
        if (!moneda.equals("PESOS") && !moneda.equals("DOLARES")) {
            throw new RuntimeException("Tipo de moneda inválido. Solo se acepta PESOS o DOLARES");
        }
    }

    private void validarSaldoInicial(Double saldo, String moneda) {
        double saldoMinimo = moneda.equals("PESOS") ? SALDO_MINIMO_PESOS : SALDO_MINIMO_DOLARES;
        if (saldo < saldoMinimo) {
            throw new RuntimeException("El saldo inicial debe ser mayor a " + saldoMinimo + " " + moneda);
        }
    }

    private void validarLimitesCuenta(Double saldo) {
        if (saldo > SALDO_MAXIMO) {
            throw new RuntimeException("El saldo supera el límite permitido");
        }
    }

    public List<Cuenta> findCuentasBySaldoRange(Double minimo, Double maximo) {
        return cuentaRepository.findCuentasBySaldoRange(minimo, maximo);
    }
}
