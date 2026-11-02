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

import co.edu.unbosque.sdgh.dto.GrupoDTO;
import co.edu.unbosque.sdgh.exception.CupoExcedidoException;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.GrupoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST para gestionar los grupos de un curso (RF2). Rutas bajo
 * /grupo. Las consultas estan abiertas a cualquier usuario autenticado; crear,
 * actualizar y eliminar requieren rol ADMIN. Las excepciones del service se
 * capturan aqui y se traducen a codigos HTTP.
 */
@RestController
@RequestMapping("/grupo")
@CrossOrigin(origins = { "http://localhost:8080" })
@Transactional
@Tag(name = "Grupos", description = "Endpoints para la gestion de grupos")
@SecurityRequirement(name = "bearerAuth")
public class GrupoController {

	/**
	 * Servicio que contiene la logica de negocio de los grupos.
	 */
	@Autowired
	private GrupoService grupoServ;

	/**
	 * Crea un nuevo grupo. 201 si fue exitoso, o el codigo del error de
	 * validacion con su mensaje.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/crear")
	@Operation(summary = "Crear grupo", description = "Crea un grupo de una materia validando cupo y numero de grupo. Requiere rol ADMIN.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Grupo creado exitosamente"),
		@ApiResponse(responseCode = "400", description = "Datos invalidos"),
		@ApiResponse(responseCode = "404", description = "La materia no existe"),
		@ApiResponse(responseCode = "406", description = "Cupo por encima del limite institucional"),
		@ApiResponse(responseCode = "409", description = "La materia ya tiene ese numero de grupo")
	})
	public ResponseEntity<String> crear(@RequestParam long materiaId, @RequestParam int numeroGrupo,
			@RequestParam int cupoPlaneado) {
		try {
			GrupoDTO nuevo = new GrupoDTO();
			nuevo.setMateriaId(materiaId);
			nuevo.setNumeroGrupo(numeroGrupo);
			nuevo.setCupoPlaneado(cupoPlaneado);
			grupoServ.create(nuevo);
			return new ResponseEntity<>("Grupo creado exitosamente", HttpStatus.CREATED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException | RecursoDuplicadoException
				| CupoExcedidoException e) {
			return new ResponseEntity<>("Error al crear grupo: " + e.getMessage(), estado(e));
		}
	}

	/**
	 * Retorna todos los grupos. 202 si hay datos, 204 si la lista esta vacia.
	 */
	@GetMapping("/mostrartodo")
	@Operation(summary = "Obtener todos los grupos", description = "Retorna la lista de todos los grupos registrados.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay grupos registrados")
	})
	public ResponseEntity<List<GrupoDTO>> obtenerTodo() {
		List<GrupoDTO> lista = grupoServ.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
	}

	/**
	 * Busca un grupo por id. 202 si existe, 404 si no se encuentra.
	 */
	@GetMapping("/mostrar/{id}")
	@Operation(summary = "Obtener grupo por id", description = "Retorna un grupo segun su identificador.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Grupo encontrado"),
		@ApiResponse(responseCode = "404", description = "Grupo no encontrado")
	})
	public ResponseEntity<GrupoDTO> obtenerPorId(@PathVariable Long id) {
		try {
			return new ResponseEntity<>(grupoServ.getById(id), HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException e) {
			return new ResponseEntity<>(estado(e));
		}
	}

	/**
	 * Retorna los grupos de una materia. 202 si hay datos, 204 si no hay ninguno.
	 */
	@GetMapping("/mostrarpormateria/{materiaId}")
	@Operation(summary = "Obtener grupos por materia", description = "Retorna los grupos asociados a una materia.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "La materia no tiene grupos"),
		@ApiResponse(responseCode = "400", description = "Id de materia invalido")
	})
	public ResponseEntity<List<GrupoDTO>> obtenerPorMateria(@PathVariable long materiaId) {
		try {
			List<GrupoDTO> lista = grupoServ.getByMateriaId(materiaId);
			if (lista.isEmpty()) {
				return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException e) {
			return new ResponseEntity<>(estado(e));
		}
	}

	/**
	 * Retorna el total de grupos. 202 si hay registros, 204 si no hay ninguno.
	 */
	@GetMapping("/contar")
	@Operation(summary = "Contar grupos", description = "Retorna el numero total de grupos registrados.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Conteo retornado exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay grupos registrados")
	})
	public ResponseEntity<Long> contar() {
		long count = grupoServ.count();
		if (count == 0) {
			return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
	}

	/**
	 * Verifica si existe un grupo por id. 202 si existe, 204 si no se encuentra.
	 */
	@GetMapping("/existe/{id}")
	@Operation(summary = "Verificar existencia de grupo", description = "Verifica si existe un grupo con el id proporcionado.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Grupo encontrado"),
		@ApiResponse(responseCode = "204", description = "Grupo no encontrado")
	})
	public ResponseEntity<Boolean> existe(@PathVariable Long id) {
		if (grupoServ.exist(id)) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		}
		return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
	}

	/**
	 * Actualiza el numero de grupo y el cupo planeado. 202 si fue exitoso, o el
	 * codigo del error de validacion con su mensaje.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/actualizar")
	@Operation(summary = "Actualizar grupo", description = "Actualiza el numero de grupo y el cupo planeado. Requiere rol ADMIN.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Grupo actualizado exitosamente"),
		@ApiResponse(responseCode = "400", description = "Datos invalidos"),
		@ApiResponse(responseCode = "404", description = "Grupo no encontrado"),
		@ApiResponse(responseCode = "406", description = "Cupo por encima del limite institucional"),
		@ApiResponse(responseCode = "409", description = "La materia ya tiene ese numero de grupo")
	})
	public ResponseEntity<String> actualizar(@RequestParam Long id, @RequestParam int numeroGrupo,
			@RequestParam int cupoPlaneado) {
		try {
			GrupoDTO nuevo = new GrupoDTO();
			nuevo.setNumeroGrupo(numeroGrupo);
			nuevo.setCupoPlaneado(cupoPlaneado);
			grupoServ.updateById(id, nuevo);
			return new ResponseEntity<>("Grupo actualizado exitosamente", HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException | RecursoDuplicadoException
				| CupoExcedidoException e) {
			return new ResponseEntity<>("Error al actualizar grupo: " + e.getMessage(), estado(e));
		}
	}

	/**
	 * Elimina un grupo y sus sesiones. 202 si fue exitoso, 404 si no se encontro.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/eliminar/{id}")
	@Operation(summary = "Eliminar grupo", description = "Elimina un grupo y sus sesiones. Requiere rol ADMIN.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Grupo eliminado exitosamente"),
		@ApiResponse(responseCode = "404", description = "Grupo no encontrado")
	})
	public ResponseEntity<String> eliminarPorId(@PathVariable Long id) {
		try {
			grupoServ.deleteById(id);
			return new ResponseEntity<>("Grupo eliminado exitosamente", HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException e) {
			return new ResponseEntity<>("Error al eliminar: " + e.getMessage(), estado(e));
		}
	}

	/**
	 * Traduce la excepcion del service al codigo HTTP correspondiente.
	 * @param e Excepcion capturada.
	 * @return Retorna el codigo HTTP.
	 */
	private HttpStatus estado(RuntimeException e) {
		if (e instanceof RecursoNoEncontradoException) {
			return HttpStatus.NOT_FOUND;
		}
		if (e instanceof RecursoDuplicadoException) {
			return HttpStatus.CONFLICT;
		}
		if (e instanceof CupoExcedidoException) {
			return HttpStatus.NOT_ACCEPTABLE;
		}
		return HttpStatus.BAD_REQUEST;
	}
}