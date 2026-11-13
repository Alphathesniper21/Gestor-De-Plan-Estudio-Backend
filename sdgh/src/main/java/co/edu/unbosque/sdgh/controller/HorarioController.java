package co.edu.unbosque.sdgh.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.sdgh.dto.GrupoDTO;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.service.HorarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST para revisar los pendientes y publicar el horario (CU08,
 * RF11, RF12). Rutas bajo /horario. Las consultas estan abiertas a cualquier
 * usuario autenticado; publicar requiere rol GESTOR.
 */
@RestController
@RequestMapping("/horario")
@CrossOrigin(origins = { "http://localhost:8080" })
@Transactional
@Tag(name = "Horario", description = "Endpoints para revisar pendientes y publicar el horario")
@SecurityRequirement(name = "bearerAuth")
public class HorarioController {

	/**
	 * Servicio que contiene la logica del horario.
	 */
	@Autowired
	private HorarioService horarioServ;

	/**
	 * Retorna los grupos con sesiones pendientes (RF11). 202 si hay pendientes,
	 * 204 si todos los grupos estan completos.
	 */
	@GetMapping("/pendientes")
	@Operation(summary = "Revisar pendientes", description = "Retorna los grupos que aun no tienen todas las sesiones de su curso (RF11).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Hay grupos pendientes"),
		@ApiResponse(responseCode = "204", description = "Todos los grupos tienen sus sesiones")
	})
	public ResponseEntity<List<GrupoDTO>> obtenerPendientes() {
		List<GrupoDTO> lista = horarioServ.getPendientes();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
	}

	/**
	 * Publica el horario (RF12). 202 si se publico, 400 si hay grupos pendientes.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@PostMapping("/publicar")
	@Operation(summary = "Publicar horario", description = "Marca el horario como publicado. No es posible mientras haya grupos pendientes (RF12). Requiere rol GESTOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Horario publicado exitosamente"),
		@ApiResponse(responseCode = "400", description = "Hay grupos con sesiones pendientes")
	})
	public ResponseEntity<String> publicar() {
		try {
			horarioServ.publicar();
			return new ResponseEntity<>("Horario publicado exitosamente", HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException e) {
			return new ResponseEntity<>("Error al publicar: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Indica si el horario esta publicado (RF12).
	 */
	@GetMapping("/estado")
	@Operation(summary = "Consultar estado del horario", description = "Retorna true si el horario esta publicado.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Estado retornado exitosamente")
	})
	public ResponseEntity<Boolean> estado() {
		return new ResponseEntity<>(horarioServ.estaPublicado(), HttpStatus.ACCEPTED);
	}
}
