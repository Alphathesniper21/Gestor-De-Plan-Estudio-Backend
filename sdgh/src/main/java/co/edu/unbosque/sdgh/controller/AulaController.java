package co.edu.unbosque.sdgh.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.sdgh.dto.AulaDTO;
import co.edu.unbosque.sdgh.service.AulaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST para gestionar las aulas fisicas (CU04 - RF04, RF07, RF08).
 * Rutas bajo /aula. Las consultas estan abiertas a cualquier usuario autenticado;
 * crear, actualizar y eliminar requieren rol ADMINISTRADOR.
 */
@RestController
@RequestMapping("/aula")
@CrossOrigin(origins = { "http://localhost:8080"})
@Transactional
@Tag(name = "Aulas", description = "Endpoints para la gestion de aulas")
@SecurityRequirement(name = "bearerAuth")
public class AulaController {

	/**
	 * Servicio que contiene la logica de negocio de las aulas.
	 */
	@Autowired
	private AulaService aulaServ;

	/**
	 * Crea una nueva aula. Retorna 201 si fue exitoso, 406 si hubo error.
	 * El id se genera automaticamente.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/crear")
	@Operation(summary = "Crear aula", description = "Registra una nueva aula. La capacidad debe ser mayor a cero. Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Aula creada exitosamente"),
		@ApiResponse(responseCode = "406", description = "Error al crear aula (capacidad invalida)")
	})
	public ResponseEntity<String> crear(@RequestParam int capacidad, @RequestParam boolean tieneSillasMovibles,
			@RequestParam boolean tieneComputadores) {
		AulaDTO nueva = new AulaDTO(null, capacidad, tieneSillasMovibles, tieneComputadores);
		int status = aulaServ.create(nueva);
		if (status == 0) {
			return new ResponseEntity<>("Aula creada exitosamente", HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>("Error al crear aula", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	/**
	 * Retorna todas las aulas. 202 si hay datos, 204 si la lista esta vacia.
	 */
	@GetMapping("/mostrartodo")
	@Operation(summary = "Obtener todas las aulas", description = "Retorna la lista de todas las aulas registradas.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay aulas registradas")
	})
	public ResponseEntity<List<AulaDTO>> obtenerTodo() {
		List<AulaDTO> lista = aulaServ.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Busca un aula por id. 202 si existe, 404 si no se encuentra.
	 */
	@GetMapping("/mostrar/{id}")
	@Operation(summary = "Obtener aula por id", description = "Retorna un aula segun su identificador.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Aula encontrada"),
		@ApiResponse(responseCode = "404", description = "Aula no encontrada")
	})
	public ResponseEntity<AulaDTO> obtenerPorId(@PathVariable Integer id) {
		AulaDTO dto = aulaServ.getById(id);
		if (dto != null) {
			return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Retorna las aulas con capacidad minima (RF07). 202 si hay datos, 204 si no hay ninguna.
	 */
	@GetMapping("/capacidad/{capacidad}")
	@Operation(summary = "Obtener aulas por capacidad minima", description = "Retorna las aulas con capacidad igual o superior a la indicada.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "Ninguna aula cumple la capacidad")
	})
	public ResponseEntity<List<AulaDTO>> obtenerPorCapacidad(@PathVariable int capacidad) {
		List<AulaDTO> lista = aulaServ.getByCapacidadMinima(capacidad);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna las aulas segun tengan computadores o no (RF08). 202 si hay datos, 204 si no hay ninguna.
	 */
	@GetMapping("/computadores/{tieneComputadores}")
	@Operation(summary = "Obtener aulas por computadores", description = "Retorna las aulas que tienen (true) o no tienen (false) computadores.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "Ninguna aula cumple el criterio")
	})
	public ResponseEntity<List<AulaDTO>> obtenerPorComputadores(@PathVariable boolean tieneComputadores) {
		List<AulaDTO> lista = aulaServ.getByTieneComputadores(tieneComputadores);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna las aulas segun tengan sillas movibles o no (RF08). 202 si hay datos, 204 si no hay ninguna.
	 */
	@GetMapping("/sillasmovibles/{tieneSillasMovibles}")
	@Operation(summary = "Obtener aulas por sillas movibles", description = "Retorna las aulas que tienen (true) o no tienen (false) sillas movibles.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "Ninguna aula cumple el criterio")
	})
	public ResponseEntity<List<AulaDTO>> obtenerPorSillasMovibles(@PathVariable boolean tieneSillasMovibles) {
		List<AulaDTO> lista = aulaServ.getByTieneSillasMovibles(tieneSillasMovibles);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna las aulas con capacidad minima y condicion de computadores (RF07, RF08).
	 * 202 si hay datos, 204 si no hay ninguna.
	 */
	@GetMapping("/filtrar")
	@Operation(summary = "Filtrar aulas por capacidad y computadores", description = "Retorna las aulas con capacidad igual o superior a la indicada y que cumplan la condicion de computadores.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "Ninguna aula cumple ambos criterios")
	})
	public ResponseEntity<List<AulaDTO>> filtrar(@RequestParam int capacidad,
			@RequestParam boolean tieneComputadores) {
		List<AulaDTO> lista = aulaServ.getByCapacidadMinimaAndComputadores(capacidad, tieneComputadores);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna el total de aulas. 202 si hay registros, 204 si no hay ninguno.
	 */
	@GetMapping("/contar")
	@Operation(summary = "Contar aulas", description = "Retorna el numero total de aulas registradas.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Conteo retornado exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay aulas registradas")
	})
	public ResponseEntity<Long> contar() {
		Long count = aulaServ.count();
		if (count == 0) {
			return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Verifica si existe un aula por id. 202 si existe, 204 si no se encuentra.
	 */
	@GetMapping("/existe/{id}")
	@Operation(summary = "Verificar existencia de aula", description = "Verifica si existe un aula con el id proporcionado.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Aula encontrada"),
		@ApiResponse(responseCode = "204", description = "Aula no encontrada")
	})
	public ResponseEntity<Boolean> existe(@PathVariable Integer id) {
		boolean encontrado = aulaServ.exist(id);
		if (encontrado) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * Actualiza un aula por id. 202 exitoso, 404 no encontrada, 400 error.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/actualizar")
	@Operation(summary = "Actualizar aula", description = "Actualiza capacidad y caracteristicas de un aula por su id. Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Aula actualizada exitosamente"),
		@ApiResponse(responseCode = "404", description = "Aula no encontrada"),
		@ApiResponse(responseCode = "400", description = "Error al actualizar (capacidad invalida)")
	})
	public ResponseEntity<String> actualizar(@RequestParam Integer id, @RequestParam int capacidad,
			@RequestParam boolean tieneSillasMovibles, @RequestParam boolean tieneComputadores) {
		AulaDTO nueva = new AulaDTO(id, capacidad, tieneSillasMovibles, tieneComputadores);
		int status = aulaServ.updateById(id, nueva);
		if (status == 0) {
			return new ResponseEntity<>("Aula actualizada exitosamente", HttpStatus.ACCEPTED);
		} else if (status == 1) {
			return new ResponseEntity<>("Aula no encontrada", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>("Error al actualizar aula", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Elimina un aula por id. 202 si fue exitoso, 404 si no se encontro.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/eliminar/{id}")
	@Operation(summary = "Eliminar aula", description = "Elimina un aula por su id. Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Aula eliminada exitosamente"),
		@ApiResponse(responseCode = "404", description = "Aula no encontrada")
	})
	public ResponseEntity<String> eliminarPorId(@PathVariable Integer id) {
		int status = aulaServ.deleteById(id);
		if (status == 0) {
			return new ResponseEntity<>("Aula eliminada exitosamente", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Error al eliminar", HttpStatus.NOT_FOUND);
		}
	}
}