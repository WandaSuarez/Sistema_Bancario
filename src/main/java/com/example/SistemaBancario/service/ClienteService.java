package com.example.SistemaBancario.service;

import com.example.SistemaBancario.model.Cliente;
import com.example.SistemaBancario.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;

    public Cliente crearCliente(Cliente cliente) {
        validarDatosCliente(cliente);
        validarDniUnico(cliente.getDni());
        validarPassword(cliente.getPassword());
        return clienteRepository.save(cliente);
    }

    public Cliente obtenerCliente(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    public Cliente obtenerClientePorDni(String dni) {
        return clienteRepository.findByDni(dni)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public Cliente actualizarCliente(Long id, Cliente cliente) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado");
        }
        validarDatosCliente(cliente);
        cliente.setId(id);
        return clienteRepository.save(cliente);
    }

    public void eliminarCliente(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado");
        }
        clienteRepository.deleteById(id);
    }

    public Cliente validarCredenciales(String dni, String password) {
        Cliente cliente = clienteRepository.findByDni(dni)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        
        if (!cliente.getPassword().equals(password)) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        return cliente;
    }

    private void validarDatosCliente(Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre es obligatorio");
        }
        if (cliente.getApellido() == null || cliente.getApellido().trim().isEmpty()) {
            throw new RuntimeException("El apellido es obligatorio");
        }
        if (cliente.getDni() == null || !cliente.getDni().matches("\\d{8}")) {
            throw new RuntimeException("DNI inválido. Debe tener 8 dígitos");
        }
    }

    private void validarDniUnico(String dni) {
        if (clienteRepository.existsByDni(dni)) {
            throw new RuntimeException("Ya existe un cliente con ese DNI");
        }
    }

    private void validarPassword(String password) {
        if (password == null || password.length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("La contraseña debe contener al menos una mayúscula");
        }
        if (!password.matches(".*\\d.*")) {
            throw new RuntimeException("La contraseña debe contener al menos un número");
        }
    }
}
