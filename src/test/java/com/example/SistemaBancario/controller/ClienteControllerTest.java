package com.example.SistemaBancario.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.SistemaBancario.model.Cliente;
import com.example.SistemaBancario.service.ClienteService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest
class ClienteControllerTest {
    @Mock
    private ClienteService clienteService;    // mock del servicio de clientes

    @InjectMocks 
    private ClienteController clienteController;  // inyecta los mocks en el controller

    private Cliente cliente;

    @BeforeEach
    void setUp() {    // se ejecuta antes de cada test
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Test");
        cliente.setDni("12345678");
    }

    @Test
    void deberiaCrearCliente() {   // test de crear cliente
        when(clienteService.crearCliente(any(Cliente.class))).thenReturn(cliente);  // simuloo respuesta del service

        ResponseEntity<Cliente> response = clienteController.crearCliente(cliente);  // ejecuta el endpoint

        assertEquals(HttpStatus.CREATED, response.getStatusCode());  // verifico codigo 201
        assertEquals(cliente, response.getBody());  // verifico que devuelve el cliente
    }

    @Test
    void deberiaObtenerClientePorId() {   // test de obtener por id
        when(clienteService.obtenerCliente(1L)).thenReturn(cliente);  // simuloo busqueda por id

        ResponseEntity<Cliente> response = clienteController.obtenerCliente(1L);  // ejecuta el endpoint

        assertEquals(HttpStatus.OK, response.getStatusCode());  // verifico codigo 200
        assertEquals(cliente, response.getBody());  // verifico que devuelve el cliente
    }

    @Test
    void deberiaObtenerClientePorDni() {   // test de obtener por dni
        when(clienteService.obtenerClientePorDni("12345678")).thenReturn(cliente);  // simuloo busqueda por dni

        ResponseEntity<Cliente> response = clienteController.obtenerClientePorDni("12345678");  // ejecuta el endpoint

        assertEquals(HttpStatus.OK, response.getStatusCode());  // verifico codigo 200
        assertEquals(cliente, response.getBody());  // verifico que devuelve el cliente
    }

    @Test
    void deberiaListarClientes() {   // test de listar todos
        List<Cliente> clientes = Arrays.asList(cliente);  // crea lista con un cliente
        when(clienteService.listarClientes()).thenReturn(clientes);  // simuloo listado

        ResponseEntity<List<Cliente>> response = clienteController.listarClientes();  // ejecuta el endpoint

        assertEquals(HttpStatus.OK, response.getStatusCode());  // verifico codigo 200
        assertEquals(clientes, response.getBody());  // verifico que devuelve la lista
    }

    @SuppressWarnings("null")
    @Test
    void deberiaListarClientesVacio() {   // test cuando no hay clientes
        List<Cliente> clientes = Collections.emptyList();  // creo lista vacia 
        when(clienteService.listarClientes()).thenReturn(clientes);  // simuloo listado vacio

        ResponseEntity<List<Cliente>> response = clienteController.listarClientes();  // ejecuta el endpoint

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());  // verifico codigo 404
        assertTrue(response.getBody() == null || response.getBody().isEmpty());  // verifico que devuelve lista vacía
    }

    @Test
    void deberiaRetornarNotFoundCuandoNoExisteCliente() {   // test cuando no existe el cliente
        when(clienteService.obtenerCliente(1L)).thenReturn(null);  // simuloo que no encuentra cliente
        
        ResponseEntity<Cliente> response = clienteController.obtenerCliente(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());  // verifico codigo 404
    }

    @Test
    void deberiaRetornarBadRequestAlCrearClienteInvalido() {   // test creo cliente invalido
        when(clienteService.crearCliente(any(Cliente.class))).thenReturn(null);  // simuloo error al creo
        
        ResponseEntity<Cliente> response = clienteController.crearCliente(new Cliente());
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());  // verifico codigo 400
    }

    @Test
    void deberiaRetornarNotFoundAlActualizarClienteInexistente() {   // test actualizar cliente que no existe
        when(clienteService.actualizarCliente(eq(1L), any(Cliente.class))).thenReturn(null);  // simuloo que no encuentra
        
        ResponseEntity<Cliente> response = clienteController.actualizarCliente(1L, new Cliente());
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());  // verifico codigo 404
    }
}
