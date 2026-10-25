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

import co.edu.unbosque.sdgh.dto.EstudianteDTO;
import co.edu.unbosque.sdgh.service.EstudianteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST para gestionar los estudiantes del programa (CU08 - RF14).
 * Rutas bajo /estudiante. Todos los endpoints requieren rol ADMINISTRADOR porque
 * el estudiante almacena datos personales (documento, EPS, resultado Saber 11, vacunacion).
 */
@RestController
@RequestMapping("/estudiante")
@CrossOrigin(origins = { "http://localhost:8081", "*" })
@Transactional
@Tag(name = "Estudiantes", description = "Endpoints para la gestion de estudiantes")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class EstudianteController {

	/**
	 * Servicio que contiene la logica de negocio de los estudiantes.
	 */
	@Autowired
	private EstudianteService estudianteServ;

	/**
	 * Crea un nuevo estudiante. Retorna 201 si fue exitoso, 406 si hubo error.
	 */
	@PostMapping("/crear")
	@Operation(summary = "Crear estudiante", description = "Registra un nuevo estudiante. Los datos se envian en el cuerpo JSON.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Estudiante creado exitosamente"),
		@ApiResponse(responseCode = "406", description = "Error al crear estudiante (datos invalidos o codigo/correo/documento duplicado)")
	})
	public ResponseEntity<String> crear(@RequestBody EstudianteDTO nuevo) {
		int status = estudianteServ.create(nuevo);
		if (status == 0) {
			return new ResponseEntity<>("Estudiante creado exitosamente", HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>("Error al crear estudiante", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	/**
	 * Retorna todos los estudiantes. 202 si hay datos, 204 si la lista esta vacia.
	 */
	@GetMapping("/mostrartodo")
	@Operation(summary = "Obtener todos los estudiantes", description = "Retorna la lista de todos los estudiantes registrados.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay estudiantes registrados")
	})
	public ResponseEntity<List<EstudianteDTO>> obtenerTodo() {
		List<EstudianteDTO> lista = estudianteServ.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Busca un estudiante por codigo. 202 si existe, 404 si no se encuentra.
	 */
	@GetMapping("/mostrar/{codigo}")
	@Operation(summary = "Obtener estudiante por codigo", description = "Retorna un estudiante segun su codigo.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Estudiante encontrado"),
		@ApiResponse(responseCode = "404", description = "Estudiante no encontrado")
	})
	public ResponseEntity<EstudianteDTO> obtenerPorCodigo(@PathVariable Integer codigo) {
		EstudianteDTO dto = estudianteServ.getById(codigo);
		if (dto != null) {
			return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Busca un estudiante por correo. 202 si existe, 404 si no se encuentra.
	 */
	@GetMapping("/mostrarporcorreo")
	@Operation(summary = "Obtener estudiante por correo", description = "Retorna un estudiante segun su correo institucional.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Estudiante encontrado"),
		@ApiResponse(responseCode = "404", description = "Estudiante no encontrado")
	})
	public ResponseEntity<EstudianteDTO> obtenerPorCorreo(@RequestParam String correo) {
		EstudianteDTO dto = estudianteServ.getByCorreo(correo);
		if (dto != null) {
			return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Retorna los estudiantes de una facultad. 202 si hay datos, 204 si no hay ninguno.
	 */
	@GetMapping("/mostrarporfacultad")
	@Operation(summary = "Obtener estudiantes por facultad", description = "Retorna los estudiantes que pertenecen a una facultad.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay estudiantes en esa facultad")
	})
	public ResponseEntity<List<EstudianteDTO>> obtenerPorFacultad(@RequestParam String facultad) {
		List<EstudianteDTO> lista = estudianteServ.getByFacultad(facultad);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna los estudiantes de un programa academico. 202 si hay datos, 204 si no hay ninguno.
	 */
	@GetMapping("/mostrarporprograma")
	@Operation(summary = "Obtener estudiantes por programa academico", description = "Retorna los estudiantes inscritos en un programa academico.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay estudiantes en ese programa")
	})
	public ResponseEntity<List<EstudianteDTO>> obtenerPorPrograma(@RequestParam String programaAcademico) {
		List<EstudianteDTO> lista = estudianteServ.getByProgramaAcademico(programaAcademico);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna los estudiantes de un semestre. 202 si hay datos, 204 si no hay ninguno.
	 */
	@GetMapping("/mostrarporsemestre/{semestre}")
	@Operation(summary = "Obtener estudiantes por semestre", description = "Retorna los estudiantes que cursan un semestre (1 a 9).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay estudiantes en ese semestre")
	})
	public ResponseEntity<List<EstudianteDTO>> obtenerPorSemestre(@PathVariable int semestre) {
		List<EstudianteDTO> lista = estudianteServ.getBySemestreActual(semestre);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna los estudiantes segun su estado (activo o inactivo). 202 si hay datos, 204 si no hay ninguno.
	 */
	@GetMapping("/mostrarporestado/{estado}")
	@Operation(summary = "Obtener estudiantes por estado", description = "Retorna los estudiantes activos (true) o inactivos (false).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay estudiantes con ese estado")
	})
	public ResponseEntity<List<EstudianteDTO>> obtenerPorEstado(@PathVariable boolean estado) {
		List<EstudianteDTO> lista = estudianteServ.getByEstado(estado);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna el total de estudiantes. 202 si hay registros, 204 si no hay ninguno.
	 */
	@GetMapping("/contar")
	@Operation(summary = "Contar estudiantes", description = "Retorna el numero total de estudiantes registrados.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Conteo retornado exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay estudiantes registrados")
	})
	public ResponseEntity<Long> contar() {
		Long count = estudianteServ.count();
		if (count == 0) {
			return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Verifica si existe un estudiante por codigo. 202 si existe, 204 si no se encuentra.
	 */
	@GetMapping("/existe/{codigo}")
	@Operation(summary = "Verificar existencia de estudiante", description = "Verifica si existe un estudiante con el codigo proporcionado.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Estudiante encontrado"),
		@ApiResponse(responseCode = "204", description = "Estudiante no encontrado")
	})
	public ResponseEntity<Boolean> existe(@PathVariable Integer codigo) {
		boolean encontrado = estudianteServ.exist(codigo);
		if (encontrado) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * Actualiza un estudiante por codigo. 202 exitoso, 404 no encontrado, 400 error.
	 */
	@PutMapping("/actualizar/{codigo}")
	@Operation(summary = "Actualizar estudiante", description = "Actualiza los datos de un estudiante por su codigo. Los datos nuevos se envian en el cuerpo JSON.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Estudiante actualizado exitosamente"),
		@ApiResponse(responseCode = "404", description = "Estudiante no encontrado"),
		@ApiResponse(responseCode = "400", description = "Error al actualizar (datos invalidos o correo de otro estudiante)")
	})
	public ResponseEntity<String> actualizar(@PathVariable Integer codigo, @RequestBody EstudianteDTO nuevo) {
		if (!estudianteServ.exist(codigo)) {
			return new ResponseEntity<>("Estudiante no encontrado", HttpStatus.NOT_FOUND);
		}
		int status = estudianteServ.updateById(codigo, nuevo);
		if (status == 0) {
			return new ResponseEntity<>("Estudiante actualizado exitosamente", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Error al actualizar estudiante", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Elimina un estudiante por codigo. 202 si fue exitoso, 404 si no se encontro.
	 */
	@DeleteMapping("/eliminar/{codigo}")
	@Operation(summary = "Eliminar estudiante", description = "Elimina un estudiante por su codigo.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Estudiante eliminado exitosamente"),
		@ApiResponse(responseCode = "404", description = "Estudiante no encontrado")
	})
	public ResponseEntity<String> eliminarPorCodigo(@PathVariable Integer codigo) {
		int status = estudianteServ.deleteById(codigo);
		if (status == 0) {
			return new ResponseEntity<>("Estudiante eliminado exitosamente", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Error al eliminar", HttpStatus.NOT_FOUND);
		}
	}
}