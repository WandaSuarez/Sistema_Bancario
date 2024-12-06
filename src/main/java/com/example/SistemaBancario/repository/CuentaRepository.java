package com.example.SistemaBancario.repository;

import com.example.SistemaBancario.model.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    Optional<Cuenta> findByNumeroCuenta(Long numeroCuenta);
    
    List<Cuenta> findByClienteId(Long clienteId);
    
    List<Cuenta> findByMoneda(String moneda);
    
    List<Cuenta> findBySaldoGreaterThan(Double saldo);
    
    List<Cuenta> findBySaldoLessThan(Double saldo);
    
    List<Cuenta> findByClienteIdAndMoneda(Long clienteId, String moneda);
    
    @Query("SELECT c FROM Cuenta c WHERE c.saldo BETWEEN :saldoMinimo AND :saldoMaximo")
    List<Cuenta> findCuentasBySaldoRange(
        @Param("saldoMinimo") Double saldoMinimo,
        @Param("saldoMaximo") Double saldoMaximo
    );
    
    boolean existsByNumeroCuenta(Long numeroCuenta);
    
    Long countByMoneda(String moneda);
    
    void deleteByNumeroCuenta(Long numeroCuenta);
}
