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
import co.edu.unbosque.sdgh.exception.CruceHorarioException;
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
 * actualizar y eliminar requieren rol GESTOR. Las excepciones del service se
 * capturan aqui y se traducen a codigos HTTP.
 *
 * @author Martín Peña
 * @version 1.0
 * @since 1.0
 * @see co.edu.unbosque.sdgh.service.GrupoService
 * @see co.edu.unbosque.sdgh.dto.GrupoDTO
 */
@RestController
@RequestMapping("/grupo")
@CrossOrigin(origins = {"http://localhost:8080"})
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
	 * Crea un nuevo grupo. Retorna 201 si fue exitoso, o 400 con el mensaje de la
	 * regla incumplida (datos invalidos, materia inexistente, numero de grupo
	 * duplicado o cupo por encima del limite institucional).
	 * @param materiaId Identificador de la materia a la que pertenece el grupo.
	 * @param numeroGrupo Numero del grupo dentro de la materia.
	 * @param cupoPlaneado Cupo planeado para el grupo.
	 * @return Retorna la respuesta HTTP con el mensaje del resultado.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@PostMapping("/crear")
	@Operation(summary = "Crear grupo", description = "Crea un grupo de una materia validando cupo y numero de grupo. Requiere rol GESTOR.")
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
			return new ResponseEntity<>("Error al crear grupo: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		} 
	}

	/**
	 * Retorna todos los grupos. 202 si hay datos, 204 si la lista esta vacia.
	 * @return Retorna la lista de grupos con el codigo HTTP correspondiente.
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
	 * Busca un grupo por id. 202 si existe, 404 si no se encuentra o el id es
	 * invalido.
	 * @param id Identificador del grupo.
	 * @return Retorna el grupo encontrado con el codigo HTTP correspondiente.
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
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Retorna los grupos de una materia. 202 si hay datos, 204 si no hay ninguno,
	 * 400 si el id de la materia es invalido.
	 * @param materiaId Identificador de la materia.
	 * @return Retorna la lista de grupos con el codigo HTTP correspondiente.
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
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Retorna el total de grupos. 202 si hay registros, 204 si no hay ninguno.
	 * @return Retorna el numero de grupos con el codigo HTTP correspondiente.
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
	 * @param id Identificador del grupo.
	 * @return Retorna true o false segun la existencia, con el codigo HTTP correspondiente.
	 */
	@GetMapping("/existe/{id}")
	@Operation(summary = "Verificar existencia de grupo", description = "Verifica si existe un grupo con el id proporcionado.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Grupo encontrado"),
		@ApiResponse(responseCode = "204", description = "Grupo no encontrado")
	})
	public ResponseEntity<Boolean> existe(@PathVariable Long id) {
		if (grupoServ.existById(id)) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		}
		return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
	}

	/**
	 * Actualiza el numero de grupo y el cupo planeado. 202 si fue exitoso; 400 si
	 * los datos son invalidos o la actualizacion genera un cruce de horario; 404
	 * si el grupo no existe; 409 si la materia ya tiene ese numero de grupo; 406
	 * si el cupo supera el limite institucional.
	 * @param id Identificador del grupo a actualizar.
	 * @param numeroGrupo Nuevo numero de grupo.
	 * @param cupoPlaneado Nuevo cupo planeado.
	 * @return Retorna la respuesta HTTP con el mensaje del resultado.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@PutMapping("/actualizar")
	@Operation(summary = "Actualizar grupo", description = "Actualiza el numero de grupo y el cupo planeado. Requiere rol GESTOR.")
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
		} catch (DatosInvalidosException | CruceHorarioException e) {
			return new ResponseEntity<>("Error al actualizar grupo: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (RecursoNoEncontradoException e) {
			return new ResponseEntity<>("Error al actualizar grupo: " + e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (RecursoDuplicadoException e) {
			return new ResponseEntity<>("Error al actualizar grupo: " + e.getMessage(), HttpStatus.CONFLICT);
		} catch (CupoExcedidoException e) {
			return new ResponseEntity<>("Error al actualizar grupo: " + e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
		}
	}

	/**
	 * Elimina un grupo y sus sesiones. 202 si fue exitoso, 400 si el id es
	 * invalido o el grupo no se encuentra.
	 * @param id Identificador del grupo a eliminar.
	 * @return Retorna la respuesta HTTP con el mensaje del resultado.
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@DeleteMapping("/eliminar/{id}")
	@Operation(summary = "Eliminar grupo", description = "Elimina un grupo y sus sesiones. Requiere rol GESTOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Grupo eliminado exitosamente"),
		@ApiResponse(responseCode = "404", description = "Grupo no encontrado")
	})
	public ResponseEntity<String> eliminarPorId(@PathVariable Long id) {
		try {
			grupoServ.deleteById(id);
			return new ResponseEntity<>("Grupo eliminado exitosamente", HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException e) {
			return new ResponseEntity<>("Error al eliminar: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

}