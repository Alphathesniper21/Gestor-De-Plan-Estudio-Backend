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
 * Controlador REST para ubicar, mover y quitar las sesiones de los grupos en la
 * grilla semanal (RF3, RF8), consultarlas por semestre (RF10) y listar los
 * grupos con sesiones pendientes (RF11). Rutas bajo /sesion. Las consultas
 * estan abiertas a cualquier usuario autenticado; crear, actualizar y eliminar
 * requieren rol GESTOR. Las excepciones del service (RF4 a RF7) se capturan
 * aqui y se traducen a codigos HTTP, siempre con el mensaje de la regla.
 */
@RestController
@RequestMapping("/sesion")
@CrossOrigin(origins = { "http://localhost:8080" })
@Transactional
@Tag(name = "Sesiones", description = "Endpoints para la gestion de sesiones en la grilla semanal")
@SecurityRequirement(name = "bearerAuth")
public class SesionController {

	/**
	 * Servicio que contiene la logica de negocio de las sesiones.
	 */
	@Autowired
	private SesionService sesionServ;

	/**
	 * Ubica una sesion en la grilla. 201 si fue exitoso, o el codigo del error de
	 * validacion con el mensaje de la regla incumplida. Las horas se envian en
	 * formato HH:mm (por ejemplo 07:00).
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@PostMapping("/crear")
	@Operation(summary = "Ubicar sesion", description = "Ubica una sesion de dos horas validando franja, pausa, maximo de sesiones y cruces. Requiere rol GESTOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Sesion creada exitosamente"),
		@ApiResponse(responseCode = "400", description = "Regla incumplida o datos invalidos"),
		@ApiResponse(responseCode = "404", description = "El grupo o su curso no existen")
	})
	public ResponseEntity<String> crear(@RequestParam long grupoId, @RequestParam int diaSemana,
			@RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaInicio,
			@RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaFin) {
		try {
			SesionDTO nueva = new SesionDTO();
			nueva.setGrupoId(grupoId);
			nueva.setDiaSemana(diaSemana);
			nueva.setHoraInicio(horaInicio);
			nueva.setHoraFin(horaFin);
			sesionServ.create(nueva);
			return new ResponseEntity<>("Sesion creada exitosamente", HttpStatus.CREATED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException | FranjaInstitucionalInvalidaException
				| CruceHorarioException | LimiteSesionesExcedidoException e) {
			return respuestaError("Error al crear sesion: ", e);
		}
	}

	/**
	 * Retorna todas las sesiones. 202 si hay datos, 204 si la lista esta vacia.
	 */
	@GetMapping("/mostrartodo")
	@Operation(summary = "Obtener todas las sesiones", description = "Retorna la lista de todas las sesiones registradas.")
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
	@Operation(summary = "Obtener sesiones por grupo", description = "Retorna las sesiones asociadas a un grupo.")
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
	 * Retorna las sesiones de los cursos de un semestre (RF10). 202 si hay
	 * datos, 204 si no hay ninguna, 400 si el semestre esta fuera de 1 a 9.
	 */
	@GetMapping("/mostrarporsemestre/{semestre}")
	@Operation(summary = "Obtener sesiones por semestre", description = "Retorna las sesiones de los cursos de un semestre (1 a 9).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay sesiones en ese semestre"),
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
	 * Verifica si existe una sesion por id. 202 si existe, 204 si no se
	 * encuentra.
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
	 * Mueve una sesion a otro dia u hora (RF8). 202 si fue exitoso, o el codigo
	 * del error de validacion con el mensaje de la regla. Si alguna regla falla,
	 * la sesion conserva su posicion anterior.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@PutMapping("/actualizar")
	@Operation(summary = "Mover sesion", description = "Mueve una sesion aplicando de nuevo las validaciones. Requiere rol GESTOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Sesion actualizada exitosamente"),
		@ApiResponse(responseCode = "400", description = "Regla incumplida o datos invalidos"),
		@ApiResponse(responseCode = "404", description = "La sesion, su grupo o su curso no existen")
	})
	public ResponseEntity<String> actualizar(@RequestParam Long id, @RequestParam int diaSemana,
			@RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaInicio,
			@RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaFin) {
		try {
			SesionDTO nueva = new SesionDTO();
			nueva.setDiaSemana(diaSemana);
			nueva.setHoraInicio(horaInicio);
			nueva.setHoraFin(horaFin);
			sesionServ.updateById(id, nueva);
			return new ResponseEntity<>("Sesion actualizada exitosamente", HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException | FranjaInstitucionalInvalidaException
				| CruceHorarioException | LimiteSesionesExcedidoException e) {
			return respuestaError("Error al actualizar sesion: ", e);
		}
	}

	/**
	 * Quita una sesion de la grilla (RF8). 202 si fue exitoso, 404 si no se
	 * encontro. El grupo queda con una sesion pendiente.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@DeleteMapping("/eliminar/{id}")
	@Operation(summary = "Quitar sesion", description = "Elimina una sesion de la grilla. Requiere rol GESTOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Sesion eliminada exitosamente"),
		@ApiResponse(responseCode = "404", description = "Sesion no encontrada")
	})
	public ResponseEntity<String> eliminarPorId(@PathVariable Long id) {
		try {
			sesionServ.deleteById(id);
			return new ResponseEntity<>("Sesion eliminada exitosamente", HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException e) {
			return respuestaError("Error al eliminar sesion: ", e);
		}
	}

	/**
	 * Traduce la excepcion del service al codigo HTTP correspondiente: 404 si el
	 * recurso no existe y 400 para las demas reglas incumplidas. El cuerpo
	 * siempre lleva el mensaje de la regla.
	 * @param prefijo Texto que antecede al mensaje de la regla.
	 * @param e Excepcion capturada.
	 * @return Retorna la respuesta HTTP con el mensaje.
	 */
	private ResponseEntity<String> respuestaError(String prefijo, RuntimeException e) {
		HttpStatus estado = (e instanceof RecursoNoEncontradoException) ? HttpStatus.NOT_FOUND
				: HttpStatus.BAD_REQUEST;
		return new ResponseEntity<>(prefijo + e.getMessage(), estado);
	}

}