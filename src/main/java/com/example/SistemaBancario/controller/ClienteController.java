package com.example.SistemaBancario.controller;

import com.example.SistemaBancario.model.Cliente;
import com.example.SistemaBancario.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    // LO PROBE EN EL POSTMANN!!!!!!!
    @PostMapping // enponid POST para crear los clientes
    public ResponseEntity<Cliente> crearCliente(@RequestBody @Valid Cliente cliente) { // recibo y valido datos del cliente
        Cliente nuevoCliente = clienteService.crearCliente(cliente);
        if (nuevoCliente == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 404, errror
        }
        return new ResponseEntity<>(nuevoCliente, HttpStatus.CREATED); // 201, creadoo
    }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @GetMapping("/{id}") // endpoin GET para obtener el cliente x id
    public ResponseEntity<Cliente> obtenerCliente(@PathVariable Long id) { // recibo el id cm variable d ruta
        Cliente cliente = clienteService.obtenerCliente(id);
        if (cliente == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404, no encontrado
        }
        return new ResponseEntity<>(cliente, HttpStatus.OK);// 200,exitoo
    }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @GetMapping("/dni/{dni}") // endpoin GET para obtener el cliente x dni
    public ResponseEntity<Cliente> obtenerClientePorDni(@PathVariable String dni) {
        Cliente cliente = clienteService.obtenerClientePorDni(dni);
        if (cliente == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404, no encontrado
        }
        return new ResponseEntity<>(cliente, HttpStatus.OK);// 200,exitoo    
    }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @GetMapping // endpoin GET para listar a tds los clientes
    public ResponseEntity<List<Cliente>> listarClientes() {
        List<Cliente> clientes = clienteService.listarClientes();
        if (clientes == null || clientes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404, no encontrado
        }
        return new ResponseEntity<>(clientes, HttpStatus.OK);// 200,exito
    }

    @PutMapping("/{id}") // endpoint PUT para actualizar cliente
    public ResponseEntity<Cliente> actualizarCliente(
            @PathVariable Long id,
            @RequestBody @Valid Cliente cliente) { // nuevos datos del cliente, el requestBody ->convierte el jsson q llega en la peticion a un obj
                Cliente clienteActualizado = clienteService.actualizarCliente(id,cliente);
                if (clienteActualizado == null){
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 404, no encontrado
                }
                return new ResponseEntity<>(cliente, HttpStatus.OK);// 200,exitoo
            }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @DeleteMapping("/{id}") // endpoin DELETE para eliminar cliente
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) { // recibo el id del fliente a eliminar
        try{
            clienteService.eliminarCliente(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204, sin contenido
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);// 404, no encontrado

        }
    }
}
