package com.example.SistemaBancario.repository;

import com.example.SistemaBancario.model.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByNumeroCuenta(Long numeroCuenta);// busco cuenta por num
    
    List<Cuenta> findByClienteId(Long clienteId); // busco todas las cuentas d un cliente
    
    List<Cuenta> findByMoneda(String moneda); // busco cuentas x tipo d moneda

    List<Cuenta> findBySaldoBetween(Double saldoMinimo, Double saldoMaximo); // busco cuentas con saldos entre 2 valores
    
    List<Cuenta> findBySaldoGreaterThan(Double saldo); // busco cuenta con saldo mayor a x
    
    boolean existsByNumeroCuenta(Long numeroCuenta); // verifico si existe una cuenta
        
    void deleteByNumeroCuenta(Long numeroCuenta); // elimino cuenta x num
}
