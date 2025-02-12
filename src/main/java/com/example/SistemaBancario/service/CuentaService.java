package com.example.SistemaBancario.service;

import com.example.SistemaBancario.model.Cuenta;
import com.example.SistemaBancario.enums.TipoTransaccion;
import com.example.SistemaBancario.exeption.CuentaException;
import com.example.SistemaBancario.model.Cliente;
import com.example.SistemaBancario.repository.CuentaRepository;
import com.example.SistemaBancario.repository.TransaccionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class CuentaService {
    private final CuentaRepository cuentaRepository;
    private final ClienteService clienteService;
    private final TransaccionRepository transaccionRepository;

    private static final String MONEDA_PESOS = "PESOS";
    private static final double SALDO_MINIMO_PESOS = 1000.0;
    private static final double SALDO_MINIMO_DOLARES = 100.0;
    private static final double SALDO_MAXIMO = 10000000.0;

    private static final double MONTO_MINIMO = 100.0;
    private static final double LIMITE_DIARIO_PESOS = 500000.0; // limite diario para transferencias en pesos
    private static final double LIMITE_DIARIO_DOLARES = 10000.0; // limite diario para transferencias en dolares

    public CuentaService(CuentaRepository cuentaRepository, ClienteService clienteService, TransaccionRepository transaccionRepository) {
        this.cuentaRepository = cuentaRepository;
        this.clienteService = clienteService;
        this.transaccionRepository = transaccionRepository;
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
        if (!moneda.equals(MONEDA_PESOS) && !moneda.equals("DOLARES")) {
            throw new CuentaException("Tipo de moneda invalido. Solo se acepta PESOS o DOLARES");
        }
    }

    // valido saldo minimo segun moneda
    private void validarSaldoInicial(Double saldo, String moneda) {
        double saldoMinimo = moneda.equals(MONEDA_PESOS) ? SALDO_MINIMO_PESOS : SALDO_MINIMO_DOLARES;
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

    // valido q no supere el limite diario
    public void validarLimiteDiario(Double monto, String moneda) {
        Double totalDiario = transaccionRepository.sumMontoByTipo(TipoTransaccion.TRANSFERENCIA_SALIENTE); // sumo trans del dia
        if (totalDiario == null) {
            totalDiario = 0.0;
        }
        
        double limiteMoneda = MONEDA_PESOS.equals(moneda) ? LIMITE_DIARIO_PESOS : LIMITE_DIARIO_DOLARES; // eligo el limite segun la moneda
        
        if (totalDiario + monto > limiteMoneda) {
            throw new CuentaException("Se ha superado el limite diario de transferencias en " + moneda);
        }
    }

    // valido q el monto sea mayor al minimo
    public void validarMontoMinimo(Double monto) {
        if (monto < MONTO_MINIMO) {
            throw new CuentaException("El monto minimo de transferencia es " + MONTO_MINIMO);
        }
    }

    // valido el saldo suficiente
    public void validarSaldoSuficiente(Cuenta cuenta, Double monto) {
        if (cuenta.getSaldo() < monto) {
            throw new CuentaException("Saldo insuficiente");
        }
    }

    // valido moneda correcta
    public void validarMoneda(Cuenta cuenta, String moneda) {
        if (!cuenta.getMoneda().equals(moneda)) {
            throw new CuentaException("La moneda de la cuenta no coincide");
        }
    }

    // registro un credito en la cuenta
    public void registrarCredito(Cuenta cuenta, Double monto) {
        cuenta.setSaldo(cuenta.getSaldo() + monto); // sumo lo q ingreso
        cuentaRepository.save(cuenta);
    }

    // registro un debito en la cuenta
    public void registrarDebito(Cuenta cuenta, Double monto) {
        validarSaldoSuficiente(cuenta, monto); // valido saldo suficiente
        cuenta.setSaldo(cuenta.getSaldo() - monto); // resto el monto
        cuentaRepository.save(cuenta);
    }
    }
