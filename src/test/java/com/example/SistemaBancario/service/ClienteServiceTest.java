package com.example.SistemaBancario.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.SistemaBancario.exeption.ClienteExeption;
import com.example.SistemaBancario.model.Cliente;
import com.example.SistemaBancario.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {
    @Mock
    private ClienteRepository clienteRepository;// mock del repositorio

    @InjectMocks
    private ClienteService clienteService;// servicio a testear

    private Cliente cliente;// cliente para usar en los tests

    @BeforeEach
    void setUp() {    // se ejecuta antes de cada test
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        cliente.setDni("12345678");
        cliente.setPassword("Password1");
    }

    @Test
    void deberiaCrearClienteValido() {    // test crear cliente valido
        when(clienteRepository.existsByDni(anyString())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.crearCliente(cliente);

        assertNotNull(resultado);
        assertEquals(cliente.getDni(), resultado.getDni());
    }

    @Test
    void deberiaLanzarExcepcionCuandoDniDuplicado() {    // test dni duplicado
        when(clienteRepository.existsByDni(anyString())).thenReturn(true);

        assertThrows(ClienteExeption.class, () -> 
            clienteService.crearCliente(cliente));
    }

    @Test
    void deberiaObtenerClientePorId() {    // test buscar por id
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Cliente resultado = clienteService.obtenerCliente(1L);

        assertNotNull(resultado);
        assertEquals(cliente.getId(), resultado.getId());
    }

    @Test
    void deberiaValidarPasswordDebil() {    // test password invalida
        cliente.setPassword("perra");

        assertThrows(ClienteExeption.class, () ->
            clienteService.crearCliente(cliente));
    }

    @Test
    void deberiaActualizarCliente() {    // test actualizar cliente
        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente resultado = clienteService.actualizarCliente(1L, cliente);

        assertNotNull(resultado);
        assertEquals(cliente.getId(), resultado.getId());
    }
}
