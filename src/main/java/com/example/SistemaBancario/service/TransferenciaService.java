package com.example.SistemaBancario.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.example.SistemaBancario.repository.TransaccionRepository;
import com.example.SistemaBancario.dto.TransaccionDTO;
import com.example.SistemaBancario.dto.TransferenciaRequestDTO;
import com.example.SistemaBancario.dto.TransferenciaResponseDTO;
import com.example.SistemaBancario.model.Cuenta;
import com.example.SistemaBancario.model.Transaccion;
import com.example.SistemaBancario.repository.CuentaRepository;
import com.example.SistemaBancario.enums.TipoTransaccion;

@Service
public class TransferenciaService {
    private final TransaccionRepository transaccionRepository; // repoo para guardar transaccionesss
    private final CuentaRepository cuentaRepository;
    private final BanelcoService banelcoService; // repoo para manejar trasnferencia interbancareias

    private static final String EXITOSA= "Exitosa";
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

    // metodo principal para reliazar las transferenciasss
    public TransferenciaResponseDTO realizarTransferencia(TransferenciaRequestDTO request) {
        validarMontoTransferencia(request.getMonto()); // valida el monto minimo
        validarLimiteDiario(request.getMonto(), request.getMoneda());// verificoc limite diario
        
        Cuenta origen = validarCuentaOrigen(request.getCuentaOrigen(), request.getMoneda(), request.getMonto()); // valido cuenta q envia
        Cuenta destino = validarCuentaDestino(request.getCuentaDestino(), request.getMoneda(), request.getMonto()); // valido cuenta q recube
        
        Double montoTotal = calcularMontoConCargos(request.getMonto(), request.getMoneda()); // calculo el monto final con cargos
        
        if (destino != null) { // si la cuenta destino existe en mi banco
            ejecutarTransferencia(origen, destino, montoTotal); // realizo la transferendia
            registrarTransaccion(origen, destino, montoTotal); // guardo el registro
        } else {
            ejecutarTransferenciaInterbancaria(origen, request.getCuentaDestino(), montoTotal); // si la cuenta no existe en mi banco, hago transferencia interbancaria usando banelco
        }
        
        return new TransferenciaResponseDTO(EXITOSA, "Transferencia realizada con éxito");
    }

    // obtengo todas las transacciones de uns cuenta ordemada por fecha
    public List<TransaccionDTO> obtenerTransacciones(Long numeroCuenta) {
        List<Transaccion> transacciones = transaccionRepository.findByCuentaNumeroCuentaOrderByFechaDesc(numeroCuenta);
        return convertirATransaccionDTO(transacciones); // convierto a dto paara mostrar
    }

    // consukto el saldo de una cuenta
    public Double consultarSaldo(Long numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
            .map(Cuenta::getSaldo) // d aca pbtengo el saldo
            .orElseThrow(() -> new TransferenciaException("No se encontro la cuenta"));
    }

    // obtengo las ultimas transacciones limitadas x cantidad
    public List<TransaccionDTO> obtenerUltimasTransacciones(Long numeroCuenta) {
        List<Transaccion> transacciones = transaccionRepository.findFirst10ByCuentaNumeroCuentaOrderByFechaDesc(numeroCuenta);
        return convertirATransaccionDTO(transacciones); // convierto a dto
    }

    // obtengo transacciones por tipo
    public List<TransaccionDTO> obtenerTransaccionesPorTipo(Long numeroCuenta, TipoTransaccion tipo) {
        List<Transaccion> transacciones = transaccionRepository.findByCuentaNumeroCuentaAndTipo(numeroCuenta, tipo);
        return convertirATransaccionDTO(transacciones);
    }

    // calculo el total por tipooo de transferencia
    public Double calcularTotalPorTipo(Long numeroCuenta, TipoTransaccion tipo) {
        return transaccionRepository.sumMontoByCuentaNumeroCuentaAndTipo(numeroCuenta, tipo);
    }

    // obtengo totaless para todos los tipos
    public Map<TipoTransaccion, Double> obtenerTotalesPorTipo(Long numeroCuenta) {
        Map<TipoTransaccion, Double> totales = new HashMap<>();
        for (TipoTransaccion tipo : TipoTransaccion.values()) {
            totales.put(tipo, calcularTotalPorTipo(numeroCuenta, tipo));
        }
        return totales;
    }

    // valido q el monto sea mayor al minimo
    private void validarMontoTransferencia(Double monto) {
        if (monto < MONTO_MINIMO) {
            throw new TransferenciaException("El monto minimo de transferencia es " + MONTO_MINIMO);
        }
    }

    // valido q no supere el limite diario
    private void validarLimiteDiario(Double montoTransferencia, String moneda) {
        Double totalDiario = transaccionRepository.sumMontoByTipo(TipoTransaccion.TRANSFERENCIA_SALIENTE); // sumo trans del dia
        if (totalDiario == null) {
            totalDiario = 0.0;
        }
        
        double limiteMoneda = "PESOS".equals(moneda) ? LIMITE_DIARIO_PESOS : LIMITE_DIARIO_DOLARES; // eligo el limite segun la moneda
        
        if (totalDiario + montoTransferencia > limiteMoneda) {
            throw new TransferenciaException("Se ha superado el limite diario de transferencias en " + moneda);
        }
    }
    

    // valido cuenta de origennnn
    private Cuenta validarCuentaOrigen(Long numeroCuenta, String moneda, Double monto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta) // busco la cuenta
            .orElseThrow(() -> new TransferenciaException("Cuenta origen no encontrada"));
            
        if (cuenta.getSaldo() < monto) { // valido el saldo suficiente
            throw new TransferenciaException("Saldo insuficiente");
        }

        if (!cuenta.getMoneda().equals(moneda)) { // valido moneda correcta
            throw new TransferenciaException("La moneda de la cuenta origen no coincide");
        }

        return cuenta;
    }

    // valido cuenta destinoo
    private Cuenta validarCuentaDestino(Long numeroCuenta, String moneda, Double monto) {
        Optional<Cuenta> cuentaOptional = cuentaRepository.findByNumeroCuenta(numeroCuenta); // busco la cuenta
        
        if (cuentaOptional.isEmpty()) { // si no existes, intento transferencia interbancaria
            if (!banelcoService.realizarTransferenciaInterbancaria(numeroCuenta, numeroCuenta, monto, moneda)) {
                throw new TransferenciaException("La transferencia interbancaria no pudo completarse");
            }
            return null;
        }
        
        Cuenta cuenta = cuentaOptional.get();
        if (!cuenta.getMoneda().equals(moneda)) { // valido moneda
            throw new TransferenciaException("La moneda de la cuenta destino no coincide");
        }
        return cuenta;
    }

    // calculo monto final con cargooooss
    private Double calcularMontoConCargos(Double monto, String moneda) {
        Double montoTotal = monto;
        
        if ("PESOS".equals(moneda) && monto > LIMITE_PESOS) {
            Double cargo = monto * CARGO_PESOS; // 2% si supera $1,000,000, pesos
            montoTotal += cargo;
        } else if ("DOLARES".equals(moneda) && monto > LIMITE_DOLARES) {
            Double cargo = monto * CARGO_DOLARES; // 0.5% si supera U$S5,000, dolares
            montoTotal += cargo;
        }
        
        return montoTotal;
    }
    

    // ejecuto la transferencia entre cuentas localesss
    private void ejecutarTransferencia(Cuenta origen, Cuenta destino, Double monto) {
        origen.setSaldo(origen.getSaldo() - monto); // resta d origen
        destino.setSaldo(destino.getSaldo() + monto); // suma a destinoo
        cuentaRepository.save(origen); // guardo los cambios
        cuentaRepository.save(destino);
    }

    // ejecuto transferencia a otro banco
    private void ejecutarTransferenciaInterbancaria(Cuenta origen, Long cuentaDestino, Double monto) {
        origen.setSaldo(origen.getSaldo() - monto); // solo resto d origenn
        cuentaRepository.save(origen);
        registrarTransaccionInterbancaria(origen, cuentaDestino, monto);// registro el movimiento
    }

    // registros las dos partes de la transferencia
    private void registrarTransaccion(Cuenta origen, Cuenta destino, Double monto) {
        registrarTransaccionSaliente(origen, destino.getNumeroCuenta(), monto); // la de salida
        registrarTransaccionEntrante(destino, origen.getNumeroCuenta(), monto); // la de entrada
    }

    // registro trasnfarencia saliente
    private void registrarTransaccionSaliente(Cuenta cuenta, Long cuentaDestino, Double monto) {
        Transaccion transaccion = new Transaccion();
        transaccion.setCuenta(cuenta);
        transaccion.setTipo(TipoTransaccion.TRANSFERENCIA_SALIENTE);
        transaccion.setMonto(monto);
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setDescripcionBreve("Transferencia a cuenta " + cuentaDestino);
        transaccionRepository.save(transaccion);
    }

    // registro trasnfarencia entrantee
    private void registrarTransaccionEntrante(Cuenta cuenta, Long cuentaOrigen, Double monto) {
        Transaccion transaccion = new Transaccion();
        transaccion.setCuenta(cuenta);
        transaccion.setTipo(TipoTransaccion.TRANSFERENCIA_ENTRANTE);
        transaccion.setMonto(monto);
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setDescripcionBreve("Transferencia desde cuenta " + cuentaOrigen);
        transaccionRepository.save(transaccion);
    }

    // registro transferencia a otro banco
    private void registrarTransaccionInterbancaria(Cuenta origen, Long cuentaDestino, Double monto) {
        registrarTransaccionSaliente(origen, cuentaDestino, monto);
    }

    // registro un credito en la cuenta
    public TransferenciaResponseDTO registrarCredito(Long cuentaId, Double monto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(cuentaId) // busco la cuenta
            .orElseThrow(() -> new TransferenciaException("Cuenta no encontrada"));
        
        cuenta.setSaldo(cuenta.getSaldo() + monto); // sumo lo qingreso
        cuentaRepository.save(cuenta);
    
        Transaccion transaccion = new Transaccion();
        transaccion.setCuenta(cuenta);
        transaccion.setTipo(TipoTransaccion.CREDITO);
        transaccion.setMonto(monto);
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setDescripcionBreve("Credito en cuenta");
        transaccionRepository.save(transaccion);
    
        return new TransferenciaResponseDTO(EXITOSA, "Credito registrado con exito");
    }
    
    // resgistro un debito en la cuenta
    public TransferenciaResponseDTO registrarDebito(Long cuentaId, Double monto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(cuentaId)
            .orElseThrow(() -> new TransferenciaException("Cuenta no encontrada"));
        
        if (cuenta.getSaldo() < monto) { // valido saldo suficiente
            throw new TransferenciaException("Saldo insuficiente");
        }
    
        cuenta.setSaldo(cuenta.getSaldo() - monto); // resto el monto
        cuentaRepository.save(cuenta);
    
        Transaccion transaccion = new Transaccion();
        transaccion.setCuenta(cuenta);
        transaccion.setTipo(TipoTransaccion.DEBITO);
        transaccion.setMonto(monto);
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setDescripcionBreve("Debito en cuenta");
        transaccionRepository.save(transaccion);
    
        return new TransferenciaResponseDTO(EXITOSA, "Debito registrado con exito");
    }
    
    // convierto transacciones a DTO para mostrarrrr
    private List<TransaccionDTO> convertirATransaccionDTO(List<Transaccion> transacciones) {
        return transacciones.stream()
            .map(t -> new TransaccionDTO(
                t.getFecha().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                t.getTipo(),
                t.getDescripcionBreve(),
                t.getMonto()
            ))
            .toList();
    }

}