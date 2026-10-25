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

import co.edu.unbosque.sdgh.dto.MateriaDTO;
import co.edu.unbosque.sdgh.dto.PlanDeEstudiosDTO;
import co.edu.unbosque.sdgh.service.PlanDeEstudiosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST para gestionar el plan de estudios del programa (CU02 - RF01, RF02).
 * Rutas bajo /plan. Las consultas estan abiertas a cualquier usuario autenticado;
 * crear, actualizar, eliminar y asociar materias requieren rol ADMINISTRADOR.
 */
@RestController
@RequestMapping("/plan")
@CrossOrigin(origins = { "http://localhost:8080"})
@Transactional
@Tag(name = "Plan de estudios", description = "Endpoints para la gestion del plan de estudios")
@SecurityRequirement(name = "bearerAuth")
public class PlanDeEstudiosController {

	/**
	 * Servicio que contiene la logica de negocio del plan de estudios.
	 */
	@Autowired
	private PlanDeEstudiosService planServ;

	/**
	 * Crea un nuevo plan de estudios. Retorna 201 si fue exitoso, 406 si hubo error.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/crear")
	@Operation(summary = "Crear plan de estudios", description = "Registra un plan de estudios con sus etapas, espacios de conocimiento y materias. Los datos se envian en el cuerpo JSON. Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Plan de estudios creado exitosamente"),
		@ApiResponse(responseCode = "406", description = "Error al crear plan (datos invalidos, id o nombre de programa duplicado)")
	})
	public ResponseEntity<String> crear(@RequestBody PlanDeEstudiosDTO nuevo) {
		int status = planServ.create(nuevo);
		if (status == 0) {
			return new ResponseEntity<>("Plan de estudios creado exitosamente", HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>("Error al crear plan de estudios", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	/**
	 * Retorna todos los planes de estudio. 202 si hay datos, 204 si la lista esta vacia.
	 */
	@GetMapping("/mostrartodo")
	@Operation(summary = "Obtener todos los planes de estudio", description = "Retorna la lista de todos los planes de estudio registrados.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay planes de estudio registrados")
	})
	public ResponseEntity<List<PlanDeEstudiosDTO>> obtenerTodo() {
		List<PlanDeEstudiosDTO> lista = planServ.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Busca un plan de estudios por id. 202 si existe, 404 si no se encuentra.
	 */
	@GetMapping("/mostrar/{id}")
	@Operation(summary = "Obtener plan de estudios por id", description = "Retorna un plan de estudios segun su identificador (ej. codigo SNIES).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Plan de estudios encontrado"),
		@ApiResponse(responseCode = "404", description = "Plan de estudios no encontrado")
	})
	public ResponseEntity<PlanDeEstudiosDTO> obtenerPorId(@PathVariable String id) {
		PlanDeEstudiosDTO dto = planServ.getById(id);
		if (dto != null) {
			return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Busca un plan de estudios por nombre de programa. 202 si existe, 404 si no se encuentra.
	 */
	@GetMapping("/mostrarpornombre")
	@Operation(summary = "Obtener plan de estudios por nombre de programa", description = "Retorna un plan de estudios segun el nombre del programa academico.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Plan de estudios encontrado"),
		@ApiResponse(responseCode = "404", description = "Plan de estudios no encontrado")
	})
	public ResponseEntity<PlanDeEstudiosDTO> obtenerPorNombre(@RequestParam String nombrePrograma) {
		PlanDeEstudiosDTO dto = planServ.getByNombrePrograma(nombrePrograma);
		if (dto != null) {
			return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Busca un plan de estudios por nombre de programa y version. 202 si existe, 404 si no se encuentra.
	 */
	@GetMapping("/mostrarpornombreyversion")
	@Operation(summary = "Obtener plan de estudios por nombre y version", description = "Retorna un plan de estudios segun el nombre del programa y su version (ej. 2025).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Plan de estudios encontrado"),
		@ApiResponse(responseCode = "404", description = "Plan de estudios no encontrado")
	})
	public ResponseEntity<PlanDeEstudiosDTO> obtenerPorNombreYVersion(@RequestParam String nombrePrograma,
			@RequestParam String version) {
		PlanDeEstudiosDTO dto = planServ.getByNombreProgramaAndVersion(nombrePrograma, version);
		if (dto != null) {
			return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Retorna el total de planes de estudio. 202 si hay registros, 204 si no hay ninguno.
	 */
	@GetMapping("/contar")
	@Operation(summary = "Contar planes de estudio", description = "Retorna el numero total de planes de estudio registrados.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Conteo retornado exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay planes de estudio registrados")
	})
	public ResponseEntity<Long> contar() {
		Long count = planServ.count();
		if (count == 0) {
			return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Verifica si existe un plan de estudios por id. 202 si existe, 204 si no se encuentra.
	 */
	@GetMapping("/existe/{id}")
	@Operation(summary = "Verificar existencia de plan de estudios", description = "Verifica si existe un plan de estudios con el id proporcionado.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Plan de estudios encontrado"),
		@ApiResponse(responseCode = "204", description = "Plan de estudios no encontrado")
	})
	public ResponseEntity<Boolean> existe(@PathVariable String id) {
		boolean encontrado = planServ.existByStringId(id);
		if (encontrado) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * Asocia una materia a un plan de estudios. 201 si se asocio, 404 si el plan no existe,
	 * 406 si la materia ya estaba asociada, 400 si los datos son invalidos.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/agregarmateria/{planId}")
	@Operation(summary = "Agregar materia a un plan", description = "Asocia una materia a un plan de estudios existente. Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Materia asociada exitosamente"),
		@ApiResponse(responseCode = "404", description = "Plan de estudios no encontrado"),
		@ApiResponse(responseCode = "406", description = "La materia ya esta asociada al plan"),
		@ApiResponse(responseCode = "400", description = "Datos de la materia invalidos")
	})
	public ResponseEntity<String> agregarMateria(@PathVariable String planId, @RequestParam long codigo,
			@RequestParam String nombre, @RequestParam int semestre, @RequestParam int ncreditos,
			@RequestParam int horasDeLaMateria, @RequestParam boolean correlativa) {
		if (!planServ.existByStringId(planId)) {
			return new ResponseEntity<>("Plan de estudios no encontrado", HttpStatus.NOT_FOUND);
		}
		MateriaDTO materia = new MateriaDTO(codigo, nombre, semestre, ncreditos, horasDeLaMateria, correlativa);
		int status = planServ.agregarMateriaAPlan(planId, materia);
		if (status == 0) {
			return new ResponseEntity<>("Materia asociada exitosamente", HttpStatus.CREATED);
		} else if (status == 1) {
			return new ResponseEntity<>("La materia ya esta asociada al plan", HttpStatus.NOT_ACCEPTABLE);
		} else {
			return new ResponseEntity<>("Datos de la materia invalidos", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Actualiza un plan de estudios por id. 202 exitoso, 404 no encontrado, 400 error.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/actualizar/{id}")
	@Operation(summary = "Actualizar plan de estudios", description = "Actualiza un plan de estudios por su id. Los datos nuevos se envian en el cuerpo JSON. Reemplaza etapas, espacios y materias. Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Plan de estudios actualizado exitosamente"),
		@ApiResponse(responseCode = "404", description = "Plan de estudios no encontrado"),
		@ApiResponse(responseCode = "400", description = "Error al actualizar (datos invalidos o nombre de programa de otro plan)")
	})
	public ResponseEntity<String> actualizar(@PathVariable String id, @RequestBody PlanDeEstudiosDTO nuevo) {
		if (!planServ.existByStringId(id)) {
			return new ResponseEntity<>("Plan de estudios no encontrado", HttpStatus.NOT_FOUND);
		}
		int status = planServ.updateByStringId(id, nuevo);
		if (status == 0) {
			return new ResponseEntity<>("Plan de estudios actualizado exitosamente", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Error al actualizar plan de estudios", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Elimina un plan de estudios por id. 202 si fue exitoso, 404 si no se encontro.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/eliminar/{id}")
	@Operation(summary = "Eliminar plan de estudios", description = "Elimina un plan de estudios por su id. Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Plan de estudios eliminado exitosamente"),
		@ApiResponse(responseCode = "404", description = "Plan de estudios no encontrado")
	})
	public ResponseEntity<String> eliminarPorId(@PathVariable String id) {
		int status = planServ.deleteByStringId(id);
		if (status == 0) {
			return new ResponseEntity<>("Plan de estudios eliminado exitosamente", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Error al eliminar", HttpStatus.NOT_FOUND);
		}
	}
}