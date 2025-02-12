package com.example.SistemaBancario.controller;

import com.example.SistemaBancario.dto.TransferenciaRequestDTO;
import com.example.SistemaBancario.dto.TransferenciaResponseDTO;
import com.example.SistemaBancario.service.TransferenciaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api") // ruta para lps endpoints
@CrossOrigin(origins = "*")
public class TranferenciaController {
    
    private TransferenciaService transferenciaService;
    private static final String EXITOSA = "EXITOSA";
    private static final String FALLIDA = "FALLIDA";

    //@Autowired // inyeccion automatica
    public TranferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    // LO PROBE EN EL POSTMANN!!!!!!!
    @PostMapping("/transfer") // endpoint POST para realizar transferencia
    // responceentity es un wrapper, envoltorio, me lo da el spring frameqork, maneja respuestas htto en controllers
    public ResponseEntity<TransferenciaResponseDTO> realizarTransferencia(
            @RequestBody @Valid TransferenciaRequestDTO request) { //recibo y valido dattos
        try {
            TransferenciaResponseDTO response = transferenciaService.realizarTransferencia(request); // llamo al servicio d transferencia y guardo el resultado en response
            if (response == null){
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400, error de soli
            }
            return new ResponseEntity<>(
                new TransferenciaResponseDTO(EXITOSA, "Transferencia realizada con exito"),
                HttpStatus.CREATED);
            } catch (Exception e) { // si ocurre algo inesperado
            return new ResponseEntity<>(
                new TransferenciaResponseDTO(FALLIDA, e.getMessage()),
                HttpStatus.BAD_REQUEST); // 400, erorr wn la tranferedia
        }
    }
}
