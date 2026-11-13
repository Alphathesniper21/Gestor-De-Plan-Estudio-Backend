package co.edu.unbosque.sdgh.controller;

import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import co.edu.unbosque.sdgh.dto.SesionDTO;
import co.edu.unbosque.sdgh.exception.CruceHorarioException;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.FranjaInstitucionalInvalidaException;
import co.edu.unbosque.sdgh.exception.LimiteSesionesExcedidoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.SesionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST para ubicar, mover, quitar y consultar las sesiones de la
 * grilla semanal (RF3, RF8, RF10). Rutas bajo /sesion. Las consultas estan
 * abiertas a cualquier usuario autenticado; crear, mover y quitar requieren rol
 * GESTOR. Las horas se envian como HH:mm o HH:mm:ss.
 */
@RestController
@RequestMapping("/sesion")
@CrossOrigin(origins = { "http://localhost:8080" })
@Transactional
@Tag(name = "Sesiones", description = "Endpoints para armar el horario en la grilla semanal")
@SecurityRequirement(name = "bearerAuth")
public class SesionController {

	/**
	 * Servicio que contiene las reglas de las sesiones.
	 */
	@Autowired
	private SesionService sesionServ;

	/**
	 * Ubica una sesion en la grilla. 201 si cumple todas las reglas, 404 si el
	 * grupo no existe y 400 con el mensaje de la regla incumplida.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@PostMapping("/crear")
	@Operation(summary = "Ubicar sesion", description = "Ubica una sesion de 2 horas validando franja, pausa, maximo de sesiones y cruces (RF3 a RF7). Requiere rol GESTOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Sesion creada exitosamente"),
		@ApiResponse(responseCode = "400", description = "Regla incumplida: franja, pausa, maximo de sesiones o cruce"),
		@ApiResponse(responseCode = "404", description = "El grupo o su curso no existen")
	})
	public ResponseEntity<String> crear(@RequestParam long grupoId, @RequestParam int diaSemana,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin) {
		try {
			sesionServ.create(new SesionDTO(grupoId, diaSemana, horaInicio, horaFin));
			return new ResponseEntity<>("Sesion creada exitosamente", HttpStatus.CREATED);
		} catch (RecursoNoEncontradoException e) {
			return new ResponseEntity<>("Error al crear sesion: " + e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (DatosInvalidosException | FranjaInstitucionalInvalidaException | LimiteSesionesExcedidoException
				| CruceHorarioException e) {
			return new ResponseEntity<>("Error al crear sesion: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Retorna todas las sesiones. 202 si hay datos, 204 si la lista esta vacia.
	 */
	@GetMapping("/mostrartodo")
	@Operation(summary = "Obtener todas las sesiones", description = "Retorna todas las sesiones de la grilla.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay sesiones registradas")
	})
	public ResponseEntity<List<SesionDTO>> obtenerTodo() {
		List<SesionDTO> lista = sesionServ.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
	}

	/**
	 * Busca una sesion por id. 202 si existe, 404 si no se encuentra.
	 */
	@GetMapping("/mostrar/{id}")
	@Operation(summary = "Obtener sesion por id", description = "Retorna una sesion segun su identificador.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Sesion encontrada"),
		@ApiResponse(responseCode = "404", description = "Sesion no encontrada")
	})
	public ResponseEntity<SesionDTO> obtenerPorId(@PathVariable Long id) {
		try {
			return new ResponseEntity<>(sesionServ.getById(id), HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Retorna las sesiones de un grupo. 202 si hay datos, 204 si no hay ninguna.
	 */
	@GetMapping("/mostrarporgrupo/{grupoId}")
	@Operation(summary = "Obtener sesiones de un grupo", description = "Retorna las sesiones asignadas a un grupo.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "El grupo no tiene sesiones"),
		@ApiResponse(responseCode = "400", description = "Id de grupo invalido")
	})
	public ResponseEntity<List<SesionDTO>> obtenerPorGrupo(@PathVariable long grupoId) {
		try {
			List<SesionDTO> lista = sesionServ.getByGrupoId(grupoId);
			if (lista.isEmpty()) {
				return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Retorna el horario de un semestre (RF10). 202 si hay sesiones, 204 si no
	 * hay ninguna, 400 si el semestre esta fuera de 1 a 9.
	 */
	@GetMapping("/semestre/{semestre}")
	@Operation(summary = "Consultar horario por semestre", description = "Retorna las sesiones de los cursos de un semestre (RF10).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay sesiones registradas en ese semestre"),
		@ApiResponse(responseCode = "400", description = "Semestre fuera de rango")
	})
	public ResponseEntity<List<SesionDTO>> obtenerPorSemestre(@PathVariable int semestre) {
		try {
			List<SesionDTO> lista = sesionServ.getBySemestre(semestre);
			if (lista.isEmpty()) {
				return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Retorna el total de sesiones. 202 si hay registros, 204 si no hay ninguno.
	 */
	@GetMapping("/contar")
	@Operation(summary = "Contar sesiones", description = "Retorna el numero total de sesiones registradas.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Conteo retornado exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay sesiones registradas")
	})
	public ResponseEntity<Long> contar() {
		long count = sesionServ.count();
		if (count == 0) {
			return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
	}

	/**
	 * Verifica si existe una sesion por id. 202 si existe, 204 si no se encuentra.
	 */
	@GetMapping("/existe/{id}")
	@Operation(summary = "Verificar existencia de sesion", description = "Verifica si existe una sesion con el id proporcionado.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Sesion encontrada"),
		@ApiResponse(responseCode = "204", description = "Sesion no encontrada")
	})
	public ResponseEntity<Boolean> existe(@PathVariable Long id) {
		if (sesionServ.existById(id)) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		}
		return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
	}

	/**
	 * Mueve una sesion a otro dia u hora (RF8). 202 si cumple las reglas; si no,
	 * la sesion conserva su posicion y se responde 400 con la regla incumplida.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@PutMapping("/actualizar")
	@Operation(summary = "Mover sesion", description = "Mueve una sesion a otro dia u hora aplicando de nuevo las reglas (RF8). Requiere rol GESTOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Sesion movida exitosamente"),
		@ApiResponse(responseCode = "400", description = "Regla incumplida: la sesion conserva su posicion"),
		@ApiResponse(responseCode = "404", description = "La sesion no existe")
	})
	public ResponseEntity<String> actualizar(@RequestParam Long id, @RequestParam int diaSemana,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin) {
		try {
			SesionDTO nueva = new SesionDTO();
			nueva.setDiaSemana(diaSemana);
			nueva.setHoraInicio(horaInicio);
			nueva.setHoraFin(horaFin);
			sesionServ.updateById(id, nueva);
			return new ResponseEntity<>("Sesion movida exitosamente", HttpStatus.ACCEPTED);
		} catch (RecursoNoEncontradoException e) {
			return new ResponseEntity<>("Error al mover sesion: " + e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (DatosInvalidosException | FranjaInstitucionalInvalidaException | CruceHorarioException e) {
			return new ResponseEntity<>("Error al mover sesion: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Quita una sesion de la grilla (RF8). 202 si fue exitoso, 404 si no se
	 * encontro.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@DeleteMapping("/eliminar/{id}")
	@Operation(summary = "Quitar sesion", description = "Quita una sesion de la grilla (RF8). Requiere rol GESTOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Sesion eliminada exitosamente"),
		@ApiResponse(responseCode = "404", description = "Sesion no encontrada")
	})
	public ResponseEntity<String> eliminarPorId(@PathVariable Long id) {
		try {
			sesionServ.deleteById(id);
			return new ResponseEntity<>("Sesion eliminada exitosamente", HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException e) {
			return new ResponseEntity<>("Error al eliminar: " + e.getMessage(), HttpStatus.NOT_FOUND);
		}
	}
}
