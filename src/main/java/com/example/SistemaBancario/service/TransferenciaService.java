package com.example.SistemaBancario.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.SistemaBancario.repository.TransaccionRepository;
import com.example.SistemaBancario.dto.TransaccionDTO;
import com.example.SistemaBancario.dto.TransferenciaRequestDTO;
import com.example.SistemaBancario.dto.TransferenciaResponseDTO;
import com.example.SistemaBancario.model.Cuenta;
import com.example.SistemaBancario.model.Transaccion;
import com.example.SistemaBancario.repository.CuentaRepository;
import com.example.SistemaBancario.enums.TipoTransaccion;

@Service
@Transactional
public class TransferenciaService {
    private final TransaccionRepository transaccionRepository;
    private final CuentaRepository cuentaRepository;
    private final BanelcoService banelcoService;

    private static final double MONTO_MINIMO = 100.0;
    private static final double LIMITE_PESOS = 1000000.0;
    private static final double LIMITE_DOLARES = 5000.0;
    private static final double CARGO_PESOS = 0.02;
    private static final double CARGO_DOLARES = 0.005;

    private static final double LIMITE_DIARIO_PESOS = 500000.0;
    private static final double LIMITE_DIARIO_DOLARES = 10000.0;

    public TransferenciaService(TransaccionRepository transaccionRepository, CuentaRepository cuentaRepository, BanelcoService banelcoService) {
        this.transaccionRepository = transaccionRepository;
        this.cuentaRepository = cuentaRepository;
        this.banelcoService = banelcoService;
    }

    public static class TransferenciaException extends RuntimeException {
        public TransferenciaException(String message) {
            super(message);
        }
    }

    public TransferenciaResponseDTO realizarTransferencia(TransferenciaRequestDTO request) {
        validarMontoTransferencia(request.getMonto());
        validarLimiteDiario(request.getMonto(), request.getMoneda());
        
        Cuenta origen = validarCuentaOrigen(request.getCuentaOrigen(), request.getMoneda(), request.getMonto());
        Cuenta destino = validarCuentaDestino(request.getCuentaDestino(), request.getMoneda(), request.getMonto());
        
        Double montoTotal = calcularMontoConCargos(request.getMonto(), request.getMoneda());
        
        if (destino != null) {
            ejecutarTransferencia(origen, destino, montoTotal);
            registrarTransaccion(origen, destino, montoTotal);
        } else {
            ejecutarTransferenciaInterbancaria(origen, request.getCuentaDestino(), montoTotal);
        }
        
        return new TransferenciaResponseDTO("EXITOSA", "Transferencia realizada con éxito");
    }

    public List<TransaccionDTO> obtenerTransacciones(Long numeroCuenta) {
        List<Transaccion> transacciones = transaccionRepository.findByCuentaNumeroCuentaOrderByFechaDesc(numeroCuenta);
        return convertirATransaccionDTO(transacciones);
    }

    public Double consultarSaldo(Long numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
            .map(Cuenta::getSaldo)
            .orElseThrow(() -> new TransferenciaException("No se encontro la cuenta"));
    }

    public List<TransaccionDTO> obtenerUltimasTransacciones(Long numeroCuenta, int limite) {
        List<Transaccion> transacciones = transaccionRepository.findLastTransactions(numeroCuenta, limite);
        return convertirATransaccionDTO(transacciones);
    }

    private void validarMontoTransferencia(Double monto) {
        if (monto < MONTO_MINIMO) {
            throw new TransferenciaException("El monto mínimo de transferencia es " + MONTO_MINIMO);
        }
    }
    


    private void validarLimiteDiario(Double montoTransferencia, String moneda) {
        Double totalDiario = transaccionRepository.sumMontoByTipo(TipoTransaccion.TRANSFERENCIA_SALIENTE);
        if (totalDiario == null) {
            totalDiario = 0.0;
        }
        
        double limiteMoneda = "PESOS".equals(moneda) ? LIMITE_DIARIO_PESOS : LIMITE_DIARIO_DOLARES;
        
        if (totalDiario + montoTransferencia > limiteMoneda) {
            throw new TransferenciaException("Se ha superado el límite diario de transferencias en " + moneda);
        }
    }
    

    private Cuenta validarCuentaOrigen(Long numeroCuenta, String moneda, Double monto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
            .orElseThrow(() -> new TransferenciaException("Cuenta origen no encontrada"));
            
        if (cuenta.getSaldo() < monto) {
            throw new TransferenciaException("Saldo insuficiente");
        }

        if (!cuenta.getMoneda().equals(moneda)) {
            throw new TransferenciaException("La moneda de la cuenta origen no coincide");
        }

        return cuenta;
    }

    private Cuenta validarCuentaDestino(Long numeroCuenta, String moneda, Double monto) {
        Optional<Cuenta> cuentaOptional = cuentaRepository.findByNumeroCuenta(numeroCuenta);
        
        if (cuentaOptional.isEmpty()) {
            if (!banelcoService.realizarTransferenciaInterbancaria(numeroCuenta, numeroCuenta, monto, moneda)) {
                throw new TransferenciaException("La transferencia interbancaria no pudo completarse");
            }
            return null;
        }
        
        Cuenta cuenta = cuentaOptional.get();
        if (!cuenta.getMoneda().equals(moneda)) {
            throw new TransferenciaException("La moneda de la cuenta destino no coincide");
        }
        return cuenta;
    }

    private Double calcularMontoConCargos(Double monto, String moneda) {
        Double montoTotal = monto;
        
        if ("PESOS".equals(moneda) && monto > LIMITE_PESOS) {
            Double cargo = monto * CARGO_PESOS; // 2% si supera $1,000,000
            montoTotal += cargo;
        } else if ("DOLARES".equals(moneda) && monto > LIMITE_DOLARES) {
            Double cargo = monto * CARGO_DOLARES; // 0.5% si supera U$S5,000
            montoTotal += cargo;
        }
        
        return montoTotal;
    }
    

    private void ejecutarTransferencia(Cuenta origen, Cuenta destino, Double monto) {
        origen.setSaldo(origen.getSaldo() - monto);
        destino.setSaldo(destino.getSaldo() + monto);
        cuentaRepository.save(origen);
        cuentaRepository.save(destino);
    }

    private void ejecutarTransferenciaInterbancaria(Cuenta origen, Long cuentaDestino, Double monto) {
        origen.setSaldo(origen.getSaldo() - monto);
        cuentaRepository.save(origen);
        registrarTransaccionInterbancaria(origen, cuentaDestino, monto);
    }

    private void registrarTransaccion(Cuenta origen, Cuenta destino, Double monto) {
        registrarTransaccionSaliente(origen, destino.getNumeroCuenta(), monto);
        registrarTransaccionEntrante(destino, origen.getNumeroCuenta(), monto);
    }

    private void registrarTransaccionSaliente(Cuenta cuenta, Long cuentaDestino, Double monto) {
        Transaccion transaccion = new Transaccion();
        transaccion.setCuenta(cuenta);
        transaccion.setTipo(TipoTransaccion.TRANSFERENCIA_SALIENTE);
        transaccion.setMonto(monto);
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setDescripcionBreve("Transferencia a cuenta " + cuentaDestino);
        transaccionRepository.save(transaccion);
    }

    private void registrarTransaccionEntrante(Cuenta cuenta, Long cuentaOrigen, Double monto) {
        Transaccion transaccion = new Transaccion();
        transaccion.setCuenta(cuenta);
        transaccion.setTipo(TipoTransaccion.TRANSFERENCIA_ENTRANTE);
        transaccion.setMonto(monto);
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setDescripcionBreve("Transferencia desde cuenta " + cuentaOrigen);
        transaccionRepository.save(transaccion);
    }

    private void registrarTransaccionInterbancaria(Cuenta origen, Long cuentaDestino, Double monto) {
        registrarTransaccionSaliente(origen, cuentaDestino, monto);
    }

    private List<TransaccionDTO> convertirATransaccionDTO(List<Transaccion> transacciones) {
        return transacciones.stream()
            .map(t -> new TransaccionDTO(
                t.getFecha().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                t.getTipo().toString(),
                t.getDescripcionBreve(),
                t.getMonto()
            ))
            .toList();
    }
}