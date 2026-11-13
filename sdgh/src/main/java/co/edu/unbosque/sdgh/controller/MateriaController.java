package co.edu.unbosque.sdgh.controller;

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

import co.edu.unbosque.sdgh.dto.MateriaDTO;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.MateriaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST para gestionar los cursos del plan de estudios (RF1, CU02).
 * Rutas bajo /materia. Las consultas estan abiertas a cualquier usuario
 * autenticado; crear, actualizar y eliminar requieren rol GESTOR. Las
 * excepciones se traducen a codigos HTTP: 404 no existe, 409 duplicado y 400
 * los demas errores de validacion.
 */
@RestController
@RequestMapping("/materia")
@CrossOrigin(origins = { "http://localhost:8080"})
@Transactional
@Tag(name = "Materias", description = "Endpoints para la gestion de materias del plan de estudios")
@SecurityRequirement(name = "bearerAuth")
public class MateriaController {

	@Autowired
	private MateriaService materiaService;

	/**
	 * Crea una nueva materia. 201 si fue exitoso, 409 si el codigo o el nombre ya
	 * existen y 400 si los datos son invalidos.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@PostMapping("/crear")
	@Operation(summary = "Crear materia", description = "Crea una nueva materia. Requiere rol GESTOR.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Materia creada exitosamente"),
			@ApiResponse(responseCode = "400", description = "Datos invalidos o sesiones fuera de rango"),
			@ApiResponse(responseCode = "409", description = "Codigo o nombre duplicado")
	})
	public ResponseEntity<String> crear(@RequestParam long codigo, @RequestParam String nombre,
			@RequestParam int semestre, @RequestParam int ncreditos,
			@RequestParam int horasDeLaMateria, @RequestParam boolean correlativa) {
		try {
			MateriaDTO nueva = new MateriaDTO(codigo, nombre, semestre, ncreditos, horasDeLaMateria, correlativa);
			materiaService.create(nueva);
			return new ResponseEntity<>("Materia creada exitosamente", HttpStatus.CREATED);
		} catch (DatosInvalidosException e) {
			return new ResponseEntity<>("Error al crear materia: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (RecursoDuplicadoException e) {
			return new ResponseEntity<>("Error al crear materia: " + e.getMessage(), HttpStatus.CONFLICT);
		}
	}


	/**
	 * Retorna todas las materias. 202 si hay datos, 204 si la lista esta vacia.
	 */
	@GetMapping("/mostrartodo")
	@Operation(summary = "Obtener todas las materias", description = "Retorna la lista de todas las materias registradas.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
			@ApiResponse(responseCode = "204", description = "No hay materias registradas")
	})
	public ResponseEntity<List<MateriaDTO>> obtenerTodo() {
		List<MateriaDTO> lista = materiaService.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Busca una materia por id. 202 si existe, 404 si no se encuentra.
	 */
	@GetMapping("/mostrar/{id}")
	@Operation(summary = "Obtener materia por id", description = "Retorna una materia segun su id.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Materia encontrada"),
			@ApiResponse(responseCode = "404", description = "Materia no encontrada")
	})
	public ResponseEntity<MateriaDTO> obtenerPorId(@PathVariable Long id) {
		try {
			return new ResponseEntity<>(materiaService.getById(id), HttpStatus.ACCEPTED);
		} catch (RecursoNoEncontradoException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Retorna las materias de un semestre. 202 si hay datos, 204 si no hay ninguna.
	 */
	@GetMapping("/semestre/{semestre}")
	@Operation(summary = "Obtener materias por semestre", description = "Retorna las materias de un semestre (1 a 9).")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
			@ApiResponse(responseCode = "204", description = "No hay materias en ese semestre")
	})
	public ResponseEntity<List<MateriaDTO>> obtenerPorSemestre(@PathVariable int semestre) {
		List<MateriaDTO> lista = materiaService.getBySemestre(semestre);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna las materias segun si son correlativas. 202 si hay datos, 204 si no hay ninguna.
	 */
	@GetMapping("/correlativa/{correlativa}")
	@Operation(summary = "Obtener materias por correlatividad", description = "Retorna las materias que son (o no) correlativas.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
			@ApiResponse(responseCode = "204", description = "No hay materias con ese filtro")
	})
	public ResponseEntity<List<MateriaDTO>> obtenerPorCorrelativa(@PathVariable boolean correlativa) {
		List<MateriaDTO> lista = materiaService.getByCorrelativa(correlativa);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna el total de materias. 202 si hay registros, 204 si no hay ninguno.
	 */
	@GetMapping("/contar")
	@Operation(summary = "Contar materias", description = "Retorna el numero total de materias registradas.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Conteo retornado exitosamente"),
			@ApiResponse(responseCode = "204", description = "No hay materias registradas")
	})
	public ResponseEntity<Long> contar() {
		Long count = materiaService.count();
		if (count == 0) {
			return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Verifica si existe una materia por id. 202 si existe, 204 si no se encuentra.
	 */
	@GetMapping("/existe/{id}")
	@Operation(summary = "Verificar existencia de materia", description = "Verifica si existe una materia con el id proporcionado.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Materia encontrada"),
			@ApiResponse(responseCode = "204", description = "Materia no encontrada")
	})
	public ResponseEntity<Boolean> existe(@PathVariable Long id) {
		boolean encontrado = materiaService.existById(id);
		if (encontrado) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * Actualiza una materia por id. 202 exitoso, 404 no encontrada, 409 codigo o
	 * nombre duplicado y 400 datos invalidos.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@PutMapping("/actualizar")
	@Operation(summary = "Actualizar materia", description = "Actualiza una materia existente por su id. Requiere rol GESTOR.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Materia actualizada exitosamente"),
			@ApiResponse(responseCode = "404", description = "Materia no encontrada"),
			@ApiResponse(responseCode = "409", description = "Codigo o nombre duplicado"),
			@ApiResponse(responseCode = "400", description = "Datos invalidos o sesiones fuera de rango")
	})
	public ResponseEntity<String> actualizar(@RequestParam Long id, @RequestParam long codigo,
			@RequestParam String nombre, @RequestParam int semestre, @RequestParam int ncreditos,
			@RequestParam int horasDeLaMateria, @RequestParam boolean correlativa) {
		try {
			MateriaDTO nueva = new MateriaDTO(codigo, nombre, semestre, ncreditos, horasDeLaMateria, correlativa);
			materiaService.updateById(id, nueva);
			return new ResponseEntity<>("Materia actualizada exitosamente", HttpStatus.ACCEPTED);
		} catch (RecursoNoEncontradoException e) {
			return new ResponseEntity<>("Error al actualizar materia: " + e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (RecursoDuplicadoException e) {
			return new ResponseEntity<>("Error al actualizar materia: " + e.getMessage(), HttpStatus.CONFLICT);
		} catch (DatosInvalidosException e) {
			return new ResponseEntity<>("Error al actualizar materia: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Elimina una materia por id. 202 si fue exitoso, 404 si no se encontro.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@DeleteMapping("/eliminar/{id}")
	@Operation(summary = "Eliminar materia", description = "Elimina una materia por su id. Requiere rol GESTOR.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Materia eliminada exitosamente"),
			@ApiResponse(responseCode = "404", description = "Materia no encontrada")
	})
	public ResponseEntity<String> eliminarPorId(@PathVariable Long id) {
		try {
			materiaService.deleteById(id);
			return new ResponseEntity<>("Materia eliminada exitosamente", HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException e) {
			return new ResponseEntity<>("Error al eliminar: " + e.getMessage(), HttpStatus.NOT_FOUND);
		}
	}
}