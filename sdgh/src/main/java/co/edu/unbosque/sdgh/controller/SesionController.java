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
 * Controlador REST para ubicar, mover y quitar las sesiones de los grupos en
 * la grilla semanal (RF3 a RF8). Rutas bajo /sesion. Las consultas estan
 * abiertas a cualquier usuario autenticado; crear, actualizar y eliminar
 * requieren rol GESTOR. Las excepciones del service se capturan aqui y se
 * traducen a codigos HTTP. Al crear o mover una sesion, la respuesta incluye
 * las advertencias por dias consecutivos (RF8), que no rechazan la sesion.
 */
@RestController
@RequestMapping("/sesion")
@CrossOrigin(origins = { "http://localhost:8080" })
@Transactional
@Tag(name = "Sesiones", description = "Endpoints para la gestion de sesiones de la grilla semanal")
@SecurityRequirement(name = "bearerAuth")
public class SesionController {

	/**
	 * Servicio que contiene la logica de negocio de las sesiones.
	 */
	@Autowired
	private SesionService sesionServ;

	/**
	 * Ubica una sesion en la grilla. 201 si fue exitoso (con advertencias si las
	 * hay), o el codigo del error de validacion con su mensaje.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@PostMapping("/crear")
	@Operation(summary = "Crear sesion", description = "Ubica una sesion de 2 horas validando franja, pausa, cruce y limite de sesiones. Requiere rol GESTOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Sesion creada exitosamente"),
		@ApiResponse(responseCode = "400", description = "Datos invalidos"),
		@ApiResponse(responseCode = "404", description = "El grupo o su materia no existen"),
		@ApiResponse(responseCode = "406", description = "Fuera de la franja permitida, en la pausa o de duracion incorrecta"),
		@ApiResponse(responseCode = "409", description = "Cruce de horario o limite de sesiones excedido")
	})
	public ResponseEntity<String> crear(@RequestParam int grupoId, @RequestParam int diaSemana,
			@RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaInicio,
			@RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaFin) {
		try {
			SesionDTO nueva = new SesionDTO();
			nueva.setGrupoId(grupoId);
			nueva.setDiaSemana(diaSemana);
			nueva.setHoraInicio(horaInicio);
			nueva.setHoraFin(horaFin);
			sesionServ.create(nueva);

			String mensaje = "Sesion creada exitosamente";
			List<String> advertencias = sesionServ.obtenerAdvertencias(grupoId);
			for (String advertencia : advertencias) {
				mensaje = mensaje + ". " + advertencia;
			}
			return new ResponseEntity<>(mensaje, HttpStatus.CREATED);
		} catch (DatosInvalidosException e) {
			return new ResponseEntity<>("Error al crear sesion: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (RecursoNoEncontradoException e) {
			return new ResponseEntity<>("Error al crear sesion: " + e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (FranjaInstitucionalInvalidaException e) {
			return new ResponseEntity<>("Error al crear sesion: " + e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
		} catch (CruceHorarioException | LimiteSesionesExcedidoException e) {
			return new ResponseEntity<>("Error al crear sesion: " + e.getMessage(), HttpStatus.CONFLICT);
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
	 * Retorna las sesiones de un grupo. 202 si hay datos, 204 si no hay ninguna,
	 * 400 si el id del grupo es invalido.
	 */
	@GetMapping("/mostrarporgrupo/{grupoId}")
	@Operation(summary = "Obtener sesiones por grupo", description = "Retorna las sesiones de un grupo.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "El grupo no tiene sesiones"),
		@ApiResponse(responseCode = "400", description = "Id de grupo invalido")
	})
	public ResponseEntity<List<SesionDTO>> obtenerPorGrupo(@PathVariable int grupoId) {
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
	 * Retorna las advertencias de un grupo por dias consecutivos (RF8). 202 si hay
	 * advertencias, 204 si no hay ninguna, 400 si el id del grupo es invalido.
	 */
	@GetMapping("/advertencias/{grupoId}")
	@Operation(summary = "Obtener advertencias de un grupo", description = "Retorna las advertencias por sesiones en dias consecutivos. No rechazan la sesion (RF8).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Advertencias retornadas"),
		@ApiResponse(responseCode = "204", description = "El grupo no tiene advertencias"),
		@ApiResponse(responseCode = "400", description = "Id de grupo invalido")
	})
	public ResponseEntity<List<String>> obtenerAdvertencias(@PathVariable int grupoId) {
		try {
			List<String> lista = sesionServ.obtenerAdvertencias(grupoId);
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
	 * Mueve una sesion a otro dia u hora (RF3). 202 si fue exitoso (con
	 * advertencias si las hay), o el codigo del error de validacion con su
	 * mensaje.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@PutMapping("/actualizar")
	@Operation(summary = "Mover sesion", description = "Cambia el dia y las horas de una sesion validando de nuevo franja, pausa y cruce. Requiere rol GESTOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Sesion actualizada exitosamente"),
		@ApiResponse(responseCode = "400", description = "Datos invalidos"),
		@ApiResponse(responseCode = "404", description = "Sesion, grupo o materia no encontrados"),
		@ApiResponse(responseCode = "406", description = "Fuera de la franja permitida, en la pausa o de duracion incorrecta"),
		@ApiResponse(responseCode = "409", description = "Cruce de horario")
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

			String mensaje = "Sesion actualizada exitosamente";
			int grupoId = sesionServ.getById(id).getGrupoId();
			List<String> advertencias = sesionServ.obtenerAdvertencias(grupoId);
			for (String advertencia : advertencias) {
				mensaje = mensaje + ". " + advertencia;
			}
			return new ResponseEntity<>(mensaje, HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException e) {
			return new ResponseEntity<>("Error al actualizar sesion: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (RecursoNoEncontradoException e) {
			return new ResponseEntity<>("Error al actualizar sesion: " + e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (FranjaInstitucionalInvalidaException e) {
			return new ResponseEntity<>("Error al actualizar sesion: " + e.getMessage(),
					HttpStatus.NOT_ACCEPTABLE);
		} catch (CruceHorarioException e) {
			return new ResponseEntity<>("Error al actualizar sesion: " + e.getMessage(), HttpStatus.CONFLICT);
		}
	}

	/**
	 * Quita una sesion de la grilla; el grupo queda con una sesion pendiente
	 * (RF11). 202 si fue exitoso, 404 si no se encontro.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@DeleteMapping("/eliminar/{id}")
	@Operation(summary = "Eliminar sesion", description = "Quita una sesion de la grilla. Requiere rol GESTOR.")
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