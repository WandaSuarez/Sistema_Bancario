package com.example.SistemaBancario.repository;

import com.example.SistemaBancario.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository 
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByDni(String dni); // busco cliente x dni  
    boolean existsByDni(String dni); // verifico si existe un cliente con ese dni
}
