package com.example.SistemaBancario.repository;

import com.example.SistemaBancario.model.Transaccion;
import com.example.SistemaBancario.enums.TipoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    //busco x cuenta ordenado x fecha descendente
    List<Transaccion> findByCuentaNumeroCuentaOrderByFechaDesc(Long numeroCuenta);

    // buscco por tipo de transaccion
    List<Transaccion> findByTipo(TipoTransaccion tipo);
    
    // buscco transacciones por rango de fechas
    List<Transaccion> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // buscco por cuenta y tipo
    List<Transaccion> findByCuentaNumeroCuentaAndTipo(Long numeroCuenta, TipoTransaccion tipo);
    
    // buscco transacciones con monto mayor a
    List<Transaccion> findByMontoGreaterThan(Double monto);
    
    // buscco ulm N transacciones de una cuenta
    @Query("SELECT t FROM Transaccion t WHERE t.cuenta.numeroCuenta = :numeroCuenta ORDER BY t.fecha DESC LIMIT :limit")
    List<Transaccion> findLastTransactions(@Param("numeroCuenta") Long numeroCuenta, @Param("limit") int limit);
    
    // Obtener suma total de transacciones por tipo
    @Query("SELECT SUM(t.monto) FROM Transaccion t WHERE t.tipo = :tipo")
    Double sumMontoByTipo(@Param("tipo") TipoTransaccion tipo);
    
    // Contar transacciones por cuenta y tipo
    long countByCuentaNumeroCuentaAndTipo(Long numeroCuenta, TipoTransaccion tipo);
    
    // buscco transacciones por descripción que contenga
    List<Transaccion> findByDescripcionBreveContaining(String descripcion);
    
    // Eliminar transacciones antiguas
    void deleteByFechaBefore(LocalDateTime fecha);

}
