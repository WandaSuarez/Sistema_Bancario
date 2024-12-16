package com.example.SistemaBancario.service;

import com.example.SistemaBancario.exeption.ClienteExeption;
import com.example.SistemaBancario.model.Cliente;
import com.example.SistemaBancario.repository.ClienteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ClienteService {
    private static final String CLIENTE_NO_ENCONTRADO = "Cliente no encontrado";
    private ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // creo  nuevo cliente
    public Cliente crearCliente(Cliente cliente) {
        validarDatosCliente(cliente);
        validarDniUnico(cliente.getDni());
        validarPassword(cliente.getPassword());
        return clienteRepository.save(cliente);
    }

    //busco cliente x id
    public Cliente obtenerCliente(Long id) {
        return clienteRepository.findById(id)
            .orElseThrow(() -> new ClienteExeption(CLIENTE_NO_ENCONTRADO));
    }

    //busco cliente x dni
    public Cliente obtenerClientePorDni(String dni) {
        return clienteRepository.findByDni(dni)
            .orElseThrow(() -> new ClienteExeption(CLIENTE_NO_ENCONTRADO));
    }

    // obtengo tods los clientes
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    //actualizo datos del cliente
    public Cliente actualizarCliente(Long id, Cliente cliente) {
        if (!clienteRepository.existsById(id)) { // verifico si esxiste
            throw new ClienteExeption(CLIENTE_NO_ENCONTRADO);
        }
        validarDatosCliente(cliente); // valido
        cliente.setId(id); // mantengo el id original
        return clienteRepository.save(cliente); // guardo los cambios
    }

    public void eliminarCliente(Long id) { // elimino cliente
        if (!clienteRepository.existsById(id)) { // verifico q exista
            throw new ClienteExeption(CLIENTE_NO_ENCONTRADO);
        }
        clienteRepository.deleteById(id); // lo elimino
    }

    // valido datos obligatorios
    private void validarDatosCliente(Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            throw new ClienteExeption("El nombre es obligatorio");
        }
        if (cliente.getApellido() == null || cliente.getApellido().trim().isEmpty()) {
            throw new ClienteExeption("El apellido es obligatorio");
        }
        if (cliente.getDni() == null || !cliente.getDni().matches("\\d{8}")) {
            throw new ClienteExeption("DNI invalido. Debe tener 8 digitos");
        }
    }

    // verifico dni no duplicados
    private void validarDniUnico(String dni) {
        if (clienteRepository.existsByDni(dni)) {
            throw new ClienteExeption("Ya existe un cliente con ese DNI");
        }
    }

    // valido reqisitos de contraseña
    private void validarPassword(String password) {
        if (password == null || password.length() < 6) {
            throw new ClienteExeption("La contraseña debe tener al menos 6 caracteres");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new ClienteExeption("La contraseña debe contener al menos una mayuscula");
        }
        if (!password.matches(".*\\d.*")) {
            throw new ClienteExeption("La contraseña debe contener al menos un numero");
        }
    }
}
