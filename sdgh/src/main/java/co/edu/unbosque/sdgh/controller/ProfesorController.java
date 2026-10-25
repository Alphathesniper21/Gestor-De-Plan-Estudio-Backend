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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.sdgh.dto.ProfesorDTO;
import co.edu.unbosque.sdgh.service.ProfesorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST para gestionar los docentes del aplicativo (CU03, RF03, RF17).
 * Rutas bajo /profesor. Todos los endpoints requieren rol ADMINISTRADOR porque
 * el docente almacena datos personales (documento, RUT, EPS, fondo de pensiones).
 */
@RestController
@RequestMapping("/profesor")
@CrossOrigin(origins = { "http://localhost:8080"})
@Transactional
@Tag(name = "Profesores", description = "Endpoints para la gestion de docentes")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class ProfesorController {

	/**
	 * Servicio que contiene la logica de negocio de los docentes.
	 */
	@Autowired
	private ProfesorService profesorServ;

	/**
	 * Crea un nuevo docente. Retorna 201 si fue exitoso, 406 si hubo error.
	 */
	@PostMapping("/crear")
	@Operation(summary = "Crear profesor", description = "Registra un nuevo docente. Los datos se envian en el cuerpo JSON.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Profesor creado exitosamente"),
			@ApiResponse(responseCode = "406", description = "Error al crear profesor (datos invalidos o codigo/correo duplicado)")
	})
	public ResponseEntity<String> crear(@RequestBody ProfesorDTO nuevo) {
		int status = profesorServ.create(nuevo);
		if (status == 0) {
			return new ResponseEntity<>("Profesor creado exitosamente", HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>("Error al crear profesor", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	/**
	 * Retorna todos los docentes. 202 si hay datos, 204 si la lista esta vacia.
	 */
	@GetMapping("/mostrartodo")
	@Operation(summary = "Obtener todos los profesores", description = "Retorna la lista de todos los docentes registrados.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
			@ApiResponse(responseCode = "204", description = "No hay profesores registrados")
	})
	public ResponseEntity<List<ProfesorDTO>> obtenerTodo() {
		return respuestaLista(profesorServ.getAll());
	}

	/**
	 * Busca un docente por codigo. 202 si existe, 404 si no se encuentra.
	 */
	@GetMapping("/mostrar/{codigo}")
	@Operation(summary = "Obtener profesor por codigo", description = "Retorna un docente segun su codigo.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Profesor encontrado"),
			@ApiResponse(responseCode = "404", description = "Profesor no encontrado")
	})
	public ResponseEntity<ProfesorDTO> obtenerPorCodigo(@PathVariable Integer codigo) {
		return respuestaUno(profesorServ.getById(codigo));
	}

	/**
	 * Busca un docente por correo. 202 si existe, 404 si no se encuentra.
	 */
	@GetMapping("/mostrarporcorreo")
	@Operation(summary = "Obtener profesor por correo", description = "Retorna un docente segun su correo institucional.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Profesor encontrado"),
			@ApiResponse(responseCode = "404", description = "Profesor no encontrado")
	})
	public ResponseEntity<ProfesorDTO> obtenerPorCorreo(@RequestParam String correo) {
		return respuestaUno(profesorServ.getByCorreo(correo));
	}

	/**
	 * Retorna los docentes de una facultad. 202 si hay datos, 204 si no hay ninguno.
	 */
	@GetMapping("/mostrarporfacultad")
	@Operation(summary = "Obtener profesores por facultad", description = "Retorna los docentes adscritos a una facultad.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
			@ApiResponse(responseCode = "204", description = "No hay profesores en esa facultad")
	})
	public ResponseEntity<List<ProfesorDTO>> obtenerPorFacultad(@RequestParam String facultad) {
		return respuestaLista(profesorServ.getByFacultad(facultad));
	}

	/**
	 * Retorna los docentes con experiencia minima. 202 si hay datos, 204 si no hay ninguno.
	 */
	@GetMapping("/experiencia/{anios}")
	@Operation(summary = "Obtener profesores por experiencia minima", description = "Retorna los docentes con al menos los anios de experiencia indicados.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
			@ApiResponse(responseCode = "204", description = "Ningun profesor cumple el criterio")
	})
	public ResponseEntity<List<ProfesorDTO>> obtenerPorExperiencia(@PathVariable int anios) {
		return respuestaLista(profesorServ.getByAniosExperienciaMinima(anios));
	}

	/**
	 * Retorna los docentes por nivel de ingles. 202 si hay datos, 204 si no hay ninguno.
	 */
	@GetMapping("/nivelingles")
	@Operation(summary = "Obtener profesores por nivel de ingles", description = "Retorna los docentes con el nivel de ingles indicado (A2, B1, B2, C1...).")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
			@ApiResponse(responseCode = "204", description = "Ningun profesor tiene ese nivel")
	})
	public ResponseEntity<List<ProfesorDTO>> obtenerPorNivelIngles(@RequestParam String nivel) {
		return respuestaLista(profesorServ.getByNivelIngles(nivel));
	}

	/**
	 * Retorna los docentes segun el estado de su hoja de vida. 202 si hay datos, 204 si no hay ninguno.
	 */
	@GetMapping("/hojavida/{actualizada}")
	@Operation(summary = "Obtener profesores por estado de hoja de vida", description = "Retorna los docentes con la hoja de vida actualizada (true) o desactualizada (false).")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
			@ApiResponse(responseCode = "204", description = "Ningun profesor cumple el criterio")
	})
	public ResponseEntity<List<ProfesorDTO>> obtenerPorHojaVida(@PathVariable boolean actualizada) {
		return respuestaLista(profesorServ.getByHojaVidaActualizada(actualizada));
	}

	/**
	 * Retorna los docentes con una especialidad. 202 si hay datos, 204 si no hay ninguno (RF17).
	 */
	@GetMapping("/especialidad")
	@Operation(summary = "Obtener profesores por especialidad", description = "Retorna los docentes que tienen la especialidad indicada (busqueda parcial, sin distinguir mayusculas).")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
			@ApiResponse(responseCode = "204", description = "Ningun profesor tiene esa especialidad")
	})
	public ResponseEntity<List<ProfesorDTO>> obtenerPorEspecialidad(@RequestParam String especialidad) {
		return respuestaLista(profesorServ.getByEspecialidad(especialidad));
	}

	/**
	 * Verifica si un docente puede orientar una especialidad. 202 si puede, 204 si no (RF17).
	 */
	@GetMapping("/puedeorientar/{codigo}")
	@Operation(summary = "Verificar si el profesor puede orientar una especialidad", description = "Indica si el docente tiene la especialidad o materia consultada.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "El profesor puede orientar la especialidad"),
			@ApiResponse(responseCode = "204", description = "El profesor no puede orientar la especialidad")
	})
	public ResponseEntity<Boolean> puedeOrientar(@PathVariable Integer codigo, @RequestParam String especialidad) {
		boolean puede = profesorServ.puedeOrientarEspecialidad(codigo, especialidad);
		if (puede) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * Retorna el total de docentes. 202 si hay registros, 204 si no hay ninguno.
	 */
	@GetMapping("/contar")
	@Operation(summary = "Contar profesores", description = "Retorna el numero total de docentes registrados.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Conteo retornado exitosamente"),
			@ApiResponse(responseCode = "204", description = "No hay profesores registrados")
	})
	public ResponseEntity<Long> contar() {
		Long count = profesorServ.count();
		if (count == 0) {
			return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Verifica si existe un docente por codigo. 202 si existe, 204 si no se encuentra.
	 */
	@GetMapping("/existe/{codigo}")
	@Operation(summary = "Verificar existencia de profesor", description = "Verifica si existe un docente con el codigo proporcionado.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Profesor encontrado"),
			@ApiResponse(responseCode = "204", description = "Profesor no encontrado")
	})
	public ResponseEntity<Boolean> existe(@PathVariable Integer codigo) {
		boolean encontrado = profesorServ.exist(codigo);
		if (encontrado) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * Actualiza un docente por codigo. 202 exitoso, 404 no encontrado, 400 error.
	 */
	@PutMapping("/actualizar/{codigo}")
	@Operation(summary = "Actualizar profesor", description = "Actualiza los datos de un docente por su codigo. Los datos nuevos se envian en el cuerpo JSON.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Profesor actualizado exitosamente"),
			@ApiResponse(responseCode = "404", description = "Profesor no encontrado"),
			@ApiResponse(responseCode = "400", description = "Error al actualizar (datos invalidos o correo de otro docente)")
	})
	public ResponseEntity<String> actualizar(@PathVariable Integer codigo, @RequestBody ProfesorDTO nuevo) {
		if (!profesorServ.exist(codigo)) {
			return new ResponseEntity<>("Profesor no encontrado", HttpStatus.NOT_FOUND);
		}
		int status = profesorServ.updateById(codigo, nuevo);
		if (status == 0) {
			return new ResponseEntity<>("Profesor actualizado exitosamente", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Error al actualizar profesor", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Elimina un docente por codigo. 202 si fue exitoso, 404 si no se encontro.
	 */
	@DeleteMapping("/eliminar/{codigo}")
	@Operation(summary = "Eliminar profesor", description = "Elimina un docente por su codigo.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Profesor eliminado exitosamente"),
			@ApiResponse(responseCode = "404", description = "Profesor no encontrado")
	})
	public ResponseEntity<String> eliminarPorCodigo(@PathVariable Integer codigo) {
		int status = profesorServ.deleteById(codigo);
		if (status == 0) {
			return new ResponseEntity<>("Profesor eliminado exitosamente", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Error al eliminar", HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Convierte una lista en respuesta: 202 si tiene datos, 204 si esta vacia.
	 */
	private ResponseEntity<List<ProfesorDTO>> respuestaLista(List<ProfesorDTO> lista) {
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Convierte un resultado unico en respuesta: 202 si existe, 404 si es null.
	 */
	private ResponseEntity<ProfesorDTO> respuestaUno(ProfesorDTO dto) {
		if (dto != null) {
			return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}