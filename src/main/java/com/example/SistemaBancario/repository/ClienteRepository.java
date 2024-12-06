package com.example.SistemaBancario.repository;

import com.example.SistemaBancario.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository 
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDni(String dni);
    
    boolean existsByDni(String dni);
    
    List<Cliente> findByNombreContainingOrApellidoContaining(String nombre, String apellido);
    
    List<Cliente> findByNombreOrderByApellidoAsc(String nombre);
    
    List<Cliente> findByApellidoOrderByNombreAsc(String apellido);
    
    void deleteByDni(String dni);
}
