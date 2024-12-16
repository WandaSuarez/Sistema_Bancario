package com.example.SistemaBancario.service;

import com.example.SistemaBancario.model.Cuenta;
import com.example.SistemaBancario.exeption.CuentaException;
import com.example.SistemaBancario.model.Cliente;
import com.example.SistemaBancario.repository.CuentaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class CuentaService {
    private final CuentaRepository cuentaRepository;
    private final ClienteService clienteService;

    private static final double SALDO_MINIMO_PESOS = 1000.0;
    private static final double SALDO_MINIMO_DOLARES = 100.0;
    private static final double SALDO_MAXIMO = 10000000.0;

    public CuentaService(CuentaRepository cuentaRepository, ClienteService clienteService) {
        this.cuentaRepository = cuentaRepository;
        this.clienteService = clienteService;
    }

    // creo una nueva cuenta
    public Cuenta crearCuenta(Cuenta cuenta) {
        if (cuentaRepository.existsByNumeroCuenta(cuenta.getNumeroCuenta())) {  // verificoo si ya existe
            throw new CuentaException("Ya existe una cuenta con ese numero");
        }
        
        Cliente cliente = clienteService.obtenerCliente(cuenta.getCliente().getId());  // obtenfo el cliente
        cuenta.setCliente(cliente);  // asocio el cliente a la cuenta
        
        validarTipoMoneda(cuenta.getMoneda());  // valido tipo de moneda
        validarSaldoInicial(cuenta.getSaldo(), cuenta.getMoneda());  // valido saldo inicial
        validarLimitesCuenta(cuenta.getSaldo());  // valido limites de saldo
        
        return cuentaRepository.save(cuenta);  // guaro la cuenta
    }

    // busca cuenta por numero
    public Cuenta obtenerCuenta(Long numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
            .orElseThrow(() -> new CuentaException("Cuenta no encontrada"));
    }

    // lista cuentas de un cliente
    public List<Cuenta> obtenerCuentasPorCliente(Long clienteId) {
        clienteService.obtenerCliente(clienteId);  // verifico que existe el cliente
        return cuentaRepository.findByClienteId(clienteId);
    }

    // lista cuentas por tipo de moneda
    public List<Cuenta> obtenerCuentasPorMoneda(String moneda) {
        validarTipoMoneda(moneda);
        return cuentaRepository.findByMoneda(moneda);
    }

    // actualizo datos de cuenta
    public Cuenta actualizarCuenta(Long numeroCuenta, Cuenta cuenta) {
        Cuenta cuentaExistente = obtenerCuenta(numeroCuenta);
        validarTipoMoneda(cuenta.getMoneda());
        validarLimitesCuenta(cuenta.getSaldo());
        cuenta.setNumeroCuenta(numeroCuenta);
        cuenta.setCliente(cuentaExistente.getCliente());
        return cuentaRepository.save(cuenta);
    }

    // elimino una cuenta existente
    public void eliminarCuenta(Long numeroCuenta) {
        if (!cuentaRepository.existsByNumeroCuenta(numeroCuenta)) {
            throw new CuentaException("Cuenta no encontrada");
        }
        cuentaRepository.deleteByNumeroCuenta(numeroCuenta);
    }

    // lista cuentas con saldo mayor al especificado
    public List<Cuenta> obtenerCuentasConSaldoMayor(Double saldo) {
        return cuentaRepository.findBySaldoGreaterThan(saldo);
    }

    // valido que la moneda sea pesos o dolares
    private void validarTipoMoneda(String moneda) {
        if (!moneda.equals("PESOS") && !moneda.equals("DOLARES")) {
            throw new CuentaException("Tipo de moneda invalido. Solo se acepta PESOS o DOLARES");
        }
    }

    // valido saldo minimo segun moneda
    private void validarSaldoInicial(Double saldo, String moneda) {
        double saldoMinimo = moneda.equals("PESOS") ? SALDO_MINIMO_PESOS : SALDO_MINIMO_DOLARES;
        if (saldo < saldoMinimo) {
            throw new CuentaException("El saldo inicial debe ser mayor a " + saldoMinimo + " " + moneda);
        }
    }

    // valido que no supere el saldo maximo
    private void validarLimitesCuenta(Double saldo) {
        if (saldo > SALDO_MAXIMO) {
            throw new CuentaException("El saldo supera el limite permitido");
        }
    }

    // busco cuentas en un rango de saldo
    public List<Cuenta> findCuentasBySaldoRange(Double minimo, Double maximo) {
        return cuentaRepository.findBySaldoBetween(minimo, maximo);
    }
}
