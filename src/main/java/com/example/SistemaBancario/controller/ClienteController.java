package com.example.SistemaBancario.controller;

import com.example.SistemaBancario.model.Cliente;
import com.example.SistemaBancario.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController// indico q es un controlador restt
@RequestMapping("/api/clientes") // ruta para los endpoint
@CrossOrigin(origins = "*") // perimito peticiones d cualqier origen
public class ClienteController {
    @Autowired // inyeccion automatica d dependencia, evito crear objetos manualemente
    private ClienteService clienteService; // inyeccion dels servicio d clientes

    @PostMapping // enponid POST para crear los clientes
    public ResponseEntity<Cliente> crearCliente(@RequestBody @Valid Cliente cliente) { // recibo y valido datos del cliente
        return ResponseEntity.ok(clienteService.crearCliente(cliente));
    }

    @GetMapping("/{id}") // endpoin GET para obtener el cliente x id
    public ResponseEntity<Cliente> obtenerCliente(@PathVariable Long id) { // recibo el id cm variable d ruta
        return ResponseEntity.ok(clienteService.obtenerCliente(id)); // devulvo el id encontrado
    }

    @GetMapping("/dni/{dni}") // endpoin GET para obtener el cliente x dni
    public ResponseEntity<Cliente> obtenerClientePorDni(@PathVariable String dni) {
        return ResponseEntity.ok(clienteService.obtenerClientePorDni(dni));// devulvo el dni encontrado
    }

    @GetMapping // endpoin GET para listar a tds los clientes
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteService.listarClientes()); // retorno lista d todos los clientes
    }

    @PutMapping("/{id}") // endpoint PUT para actualizar cliente
    public ResponseEntity<Cliente> actualizarCliente(
            @PathVariable Long id,
            @RequestBody @Valid Cliente cliente) { // nuevos datos del cliente, el requestBody ->convierte el jsson q llega en la peticion a un obj
        return ResponseEntity.ok(clienteService.actualizarCliente(id, cliente)); // devuelvo cliente actualizado
    }

    @DeleteMapping("/{id}") // endpoin DELETE para eliminar cliente
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) { // recibo el id del fliente a eliminar
        clienteService.eliminarCliente(id);// lo elimino
        return ResponseEntity.ok().build(); // retorno repuesta exitosa sin contenido -> build -> construye la repuesta http final, s usa cuando no hay datos q deovolver, solo el estado del codigo
    }
}
