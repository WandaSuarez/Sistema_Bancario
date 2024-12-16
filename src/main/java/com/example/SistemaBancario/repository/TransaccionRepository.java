package com.example.SistemaBancario.repository;

import com.example.SistemaBancario.model.Transaccion;
import com.example.SistemaBancario.enums.TipoTransaccion;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Repository
public class TransaccionRepository{
    private final List<Transaccion> transacciones = new ArrayList<>();

    // busca transacciones por numero de cuenta ordenadas por fecha descendente
    public List<Transaccion> findByCuentaNumeroCuentaOrderByFechaDesc(Long numeroCuenta){
        return transacciones.stream()
            .filter(t -> t.getCuenta().getNumeroCuenta().equals(numeroCuenta))
            .sorted(Comparator.comparing(Transaccion::getFecha).reversed())
            .toList();
    }
    
    // busca transacciones por numero de cuenta y tipo
    public List<Transaccion> findByCuentaNumeroCuentaAndTipo(Long numeroCuenta, TipoTransaccion tipo) {
        return transacciones.stream()
            .filter(t -> t.getCuenta().getNumeroCuenta().equals(numeroCuenta) && t.getTipo().equals(tipo))
            .toList();    }
    
    // busca las ultimas N transacciones de una cuenta
    public List<Transaccion> findFirst10ByCuentaNumeroCuentaOrderByFechaDesc(Long numeroCuenta){
        return transacciones.stream()
            .filter(t -> t.getCuenta().getNumeroCuenta().equals(numeroCuenta))
            .sorted(Comparator.comparing(Transaccion::getFecha).reversed())
            .limit(10)
            .toList();
        }
    
    // suma el monto total por tipo de transaccion
    public Double sumMontoByTipo(TipoTransaccion tipo){
        return transacciones.stream()
            .filter(t -> t.getTipo().equals(tipo))
            .mapToDouble(Transaccion::getMonto)
            .sum();
    }
    
    // suma el monto total por cuenta y tipo de transaccion
    public Double sumMontoByCuentaNumeroCuentaAndTipo(Long numeroCuenta, TipoTransaccion tipo){
        return transacciones.stream()
            .filter(t -> t.getCuenta().getNumeroCuenta().equals(numeroCuenta) && t.getTipo().equals(tipo))
            .mapToDouble(Transaccion::getMonto)
            .sum();
    }

    public void save(Transaccion transaccion) {
        transacciones.add(transaccion);
    }
}
