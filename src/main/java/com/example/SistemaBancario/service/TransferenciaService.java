package com.example.SistemaBancario.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private TransaccionRepository transaccionRepository;
    
    @Autowired
    private CuentaRepository cuentaRepository;

    private static final double MONTO_MINIMO = 100.0;
    private static final double LIMITE_DIARIO = 500000.0;

    public TransferenciaResponseDTO realizarTransferencia(TransferenciaRequestDTO request) {
        validarMontoTransferencia(request.getMonto());
        validarLimiteDiario(request.getCuentaOrigen(), request.getMonto());
        
        Cuenta origen = validarCuentaOrigen(request.getCuentaOrigen(), request.getMonto());
        Cuenta destino = validarCuentaDestino(request.getCuentaDestino(), request.getMoneda());
        
        ejecutarTransferencia(origen, destino, request.getMonto());
        registrarTransaccion(origen, destino, request.getMonto());
        
        return new TransferenciaResponseDTO("EXITOSA", "Transferencia realizada con éxito");
    }

    public List<TransaccionDTO> obtenerTransacciones(Long numeroCuenta) {
        List<Transaccion> transacciones = transaccionRepository.findByCuentaNumeroCuentaOrderByFechaDesc(numeroCuenta);
        return convertirATransaccionDTO(transacciones);
    }

    public Double consultarSaldo(Long numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
            .orElseThrow(() -> new RuntimeException("No se encontró la cuenta"));
        return cuenta.getSaldo();
    }

    public List<TransaccionDTO> obtenerUltimasTransacciones(Long numeroCuenta, int limite) {
        List<Transaccion> transacciones = transaccionRepository.findLastTransactions(numeroCuenta, limite);
        return convertirATransaccionDTO(transacciones);
    }

    private void validarMontoTransferencia(Double monto) {
        if (monto < MONTO_MINIMO) {
            throw new RuntimeException("El monto mínimo de transferencia es " + MONTO_MINIMO);
        }
    }

    private void validarLimiteDiario(Long numeroCuenta, Double montoTransferencia) {
        Double totalDiario = transaccionRepository.sumMontoByTipo(TipoTransaccion.TRANSFERENCIA_SALIENTE);
        if (totalDiario == null) {
            totalDiario = 0.0;
        }
        
        if (totalDiario + montoTransferencia > LIMITE_DIARIO) {
            throw new RuntimeException("Se ha superado el límite diario de transferencias");
        }
    }
    

    private Cuenta validarCuentaOrigen(Long numeroCuenta, Double monto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
            .orElseThrow(() -> new RuntimeException("Cuenta origen no encontrada"));
            
        if (cuenta.getSaldo() < monto) {
            throw new RuntimeException("Saldo insuficiente");
        }
        return cuenta;
    }

    private Cuenta validarCuentaDestino(Long numeroCuenta, String moneda) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
            .orElseThrow(() -> new RuntimeException("Cuenta destino no encontrada"));
               
        if (!cuenta.getMoneda().equals(moneda)) {
            throw new RuntimeException("La moneda de la cuenta destino no coincide");
        }
        return cuenta;
    }
    

    private void ejecutarTransferencia(Cuenta origen, Cuenta destino, Double monto) {
        origen.setSaldo(origen.getSaldo() - monto);
        destino.setSaldo(destino.getSaldo() + monto);
        cuentaRepository.save(origen);
        cuentaRepository.save(destino);
    }

    private void registrarTransaccion(Cuenta origen, Cuenta destino, Double monto) {
        // Registro de transacción saliente
        Transaccion salienteTransaccion = new Transaccion();
        salienteTransaccion.setCuenta(origen);
        salienteTransaccion.setTipo(TipoTransaccion.TRANSFERENCIA_SALIENTE);
        salienteTransaccion.setMonto(monto);
        salienteTransaccion.setFecha(LocalDateTime.now());  // Agregar esta línea
        salienteTransaccion.setDescripcionBreve("Transferencia a cuenta " + destino.getNumeroCuenta());
        transaccionRepository.save(salienteTransaccion);

        // Registro de transacción entrante
        Transaccion entranteTransaccion = new Transaccion();
        entranteTransaccion.setCuenta(destino);
        entranteTransaccion.setTipo(TipoTransaccion.TRANSFERENCIA_ENTRANTE);
        entranteTransaccion.setMonto(monto);
        entranteTransaccion.setFecha(LocalDateTime.now());  // Agregar esta línea
        entranteTransaccion.setDescripcionBreve("Transferencia desde cuenta " + origen.getNumeroCuenta());
        transaccionRepository.save(entranteTransaccion);
    }

    private List<TransaccionDTO> convertirATransaccionDTO(List<Transaccion> transacciones) {
        return transacciones.stream()
            .map(t -> new TransaccionDTO(
                t.getFecha().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                t.getTipo().toString(),
                t.getDescripcionBreve(),
                t.getMonto()
            ))
            .collect(Collectors.toList());
    }
}
