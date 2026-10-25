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

import co.edu.unbosque.sdgh.dto.GrupoDTO;
import co.edu.unbosque.sdgh.service.GrupoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST para gestionar los grupos y la asignacion de horarios
 * (CU05, CU07, CU08, CU10 - RF05 a RF09, RF13 a RF21, RF25). Rutas bajo /grupo.
 * Las consultas estan abiertas a cualquier usuario autenticado; crear, actualizar,
 * eliminar y las herramientas de diagnostico/publicacion requieren rol ADMINISTRADOR.
 */
@RestController
@RequestMapping("/grupo")
@CrossOrigin(origins = { "http://localhost:8080"})
@Transactional
@Tag(name = "Grupos", description = "Endpoints para la gestion de grupos y horarios")
@SecurityRequirement(name = "bearerAuth")
public class GrupoController {

	/**
	 * Servicio que contiene la logica de negocio de los grupos.
	 */
	@Autowired
	private GrupoService grupoServ;

	/**
	 * Crea un nuevo grupo. Retorna 201 si fue exitoso, 406 si hubo error
	 * (con el detalle de las reglas incumplidas).
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/crear")
	@Operation(summary = "Crear grupo", description = "Crea un grupo validando cruces de horario, cupo, franja institucional y sesiones semanales. Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Grupo creado exitosamente"),
		@ApiResponse(responseCode = "406", description = "Error al crear grupo (datos invalidos, id duplicado o regla incumplida)")
	})
	public ResponseEntity<String> crear(@RequestParam int idGrupo, @RequestParam long materiaId,
			@RequestParam long docenteId, @RequestParam long aulaId, @RequestParam int cupoMaximo,
			@RequestParam int estudiantesInscritos, @RequestParam String diaSemana,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin) {
		GrupoDTO nuevo = new GrupoDTO(idGrupo, materiaId, docenteId, aulaId, cupoMaximo, estudiantesInscritos,
				diaSemana, horaInicio, horaFin);
		int status = grupoServ.create(nuevo);
		if (status == 0) {
			return new ResponseEntity<>("Grupo creado exitosamente", HttpStatus.CREATED);
		}
		String detalle = status == 1 && grupoServ.exist(idGrupo)
				? "El id del grupo ya existe."
				: String.join(" | ", grupoServ.diagnosticarReglasGrupo(nuevo, null));
		return new ResponseEntity<>("Error al crear grupo: " + detalle, HttpStatus.NOT_ACCEPTABLE);
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
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
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
	public ResponseEntity<GrupoDTO> obtenerPorId(@PathVariable Integer id) {
		GrupoDTO dto = grupoServ.getById(id);
		if (dto != null) {
			return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Retorna los grupos de una materia. 202 si hay datos, 204 si no hay ninguno.
	 */
	@GetMapping("/mostrarpormateria/{materiaId}")
	@Operation(summary = "Obtener grupos por materia", description = "Retorna los grupos asociados a una materia.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "La materia no tiene grupos")
	})
	public ResponseEntity<List<GrupoDTO>> obtenerPorMateria(@PathVariable long materiaId) {
		List<GrupoDTO> lista = grupoServ.getByMateriaId(materiaId);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna los grupos de un docente. 202 si hay datos, 204 si no hay ninguno.
	 */
	@GetMapping("/mostrarpordocente/{docenteId}")
	@Operation(summary = "Obtener grupos por docente", description = "Retorna los grupos asignados a un docente.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "El docente no tiene grupos")
	})
	public ResponseEntity<List<GrupoDTO>> obtenerPorDocente(@PathVariable long docenteId) {
		List<GrupoDTO> lista = grupoServ.getByDocenteId(docenteId);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna los grupos de un aula. 202 si hay datos, 204 si no hay ninguno.
	 */
	@GetMapping("/mostrarporaula/{aulaId}")
	@Operation(summary = "Obtener grupos por aula", description = "Retorna los grupos programados en un aula.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "El aula no tiene grupos")
	})
	public ResponseEntity<List<GrupoDTO>> obtenerPorAula(@PathVariable long aulaId) {
		List<GrupoDTO> lista = grupoServ.getByAulaId(aulaId);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna los grupos con pocos inscritos (RF19). 202 si hay datos, 204 si no hay ninguno.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/paracierre")
	@Operation(summary = "Obtener grupos para cierre", description = "Retorna los grupos con menos estudiantes que el minimo (10 por defecto). Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "Ningun grupo esta por debajo del minimo")
	})
	public ResponseEntity<List<GrupoDTO>> obtenerParaCierre(@RequestParam(required = false) Integer minimo) {
		List<GrupoDTO> lista = minimo != null ? grupoServ.getGruposParaCierre(minimo)
				: grupoServ.getGruposParaCierre();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna las horas semanales de un docente (RF18). 202 si tiene horas, 204 si no tiene.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/horassemanales/{docenteId}")
	@Operation(summary = "Calcular horas semanales de un docente", description = "Retorna el total de horas semanales programadas para el docente. Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Horas calculadas exitosamente"),
		@ApiResponse(responseCode = "204", description = "El docente no tiene horas programadas")
	})
	public ResponseEntity<Long> horasSemanales(@PathVariable long docenteId) {
		long horas = grupoServ.calcularHorasSemanalesDocente(docenteId);
		if (horas == 0) {
			return new ResponseEntity<>(horas, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(horas, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Diagnostica las reglas duras y blandas de un grupo sin guardarlo (CU05 / RF16, RF20).
	 * 202 si hay hallazgos, 204 si el grupo no presenta conflictos ni advertencias.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/diagnosticar")
	@Operation(summary = "Diagnosticar un grupo", description = "Evalua un grupo y lista las reglas incumplidas y advertencias, sin guardarlo. Si se envia idExcluir, ese grupo se ignora (util al actualizar). Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Se detectaron conflictos o advertencias"),
		@ApiResponse(responseCode = "204", description = "El grupo no presenta conflictos")
	})
	public ResponseEntity<List<String>> diagnosticar(@RequestParam int idGrupo, @RequestParam long materiaId,
			@RequestParam long docenteId, @RequestParam long aulaId, @RequestParam int cupoMaximo,
			@RequestParam int estudiantesInscritos, @RequestParam String diaSemana,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin,
			@RequestParam(required = false) Integer idExcluir) {
		GrupoDTO dto = new GrupoDTO(idGrupo, materiaId, docenteId, aulaId, cupoMaximo, estudiantesInscritos,
				diaSemana, horaInicio, horaFin);
		List<String> hallazgos = grupoServ.diagnosticarReglasGrupo(dto, idExcluir);
		if (hallazgos.isEmpty()) {
			return new ResponseEntity<>(hallazgos, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(hallazgos, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Genera el reporte de conflictos del horario actual (CU05, CU10 / RF20, RF21, RF25).
	 * 202 si hay hallazgos, 204 si no hay ninguno.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/reporteconflictos")
	@Operation(summary = "Reporte de conflictos", description = "Lista los conflictos y advertencias de todos los grupos registrados. Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Reporte con hallazgos"),
		@ApiResponse(responseCode = "204", description = "El horario no tiene conflictos")
	})
	public ResponseEntity<List<String>> reporteConflictos() {
		List<String> reporte = grupoServ.generarReporteConflictos();
		if (reporte.isEmpty()) {
			return new ResponseEntity<>(reporte, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(reporte, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Verifica si el horario se puede publicar (CU10 / RF25). 202 si se puede, 204 si no.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/puedepublicar")
	@Operation(summary = "Verificar si el horario se puede publicar", description = "Indica si no hay violaciones de restricciones duras. Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "El horario se puede publicar"),
		@ApiResponse(responseCode = "204", description = "Hay restricciones duras incumplidas")
	})
	public ResponseEntity<Boolean> puedePublicar() {
		boolean puede = grupoServ.puedePublicarHorario();
		if (puede) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
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
		Long count = grupoServ.count();
		if (count == 0) {
			return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
		}
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
	public ResponseEntity<Boolean> existe(@PathVariable Integer id) {
		boolean encontrado = grupoServ.exist(id);
		if (encontrado) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * Actualiza un grupo por id. 202 exitoso, 404 no encontrado, 400 error
	 * (con el detalle de las reglas incumplidas).
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/actualizar")
	@Operation(summary = "Actualizar grupo", description = "Actualiza un grupo existente revalidando las restricciones duras. Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Grupo actualizado exitosamente"),
		@ApiResponse(responseCode = "404", description = "Grupo no encontrado"),
		@ApiResponse(responseCode = "400", description = "Error al actualizar (datos invalidos o regla incumplida)")
	})
	public ResponseEntity<String> actualizar(@RequestParam Integer id, @RequestParam long materiaId,
			@RequestParam long docenteId, @RequestParam long aulaId, @RequestParam int cupoMaximo,
			@RequestParam int estudiantesInscritos, @RequestParam String diaSemana,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin) {
		if (!grupoServ.exist(id)) {
			return new ResponseEntity<>("Grupo no encontrado", HttpStatus.NOT_FOUND);
		}
		GrupoDTO nuevo = new GrupoDTO(id, materiaId, docenteId, aulaId, cupoMaximo, estudiantesInscritos,
				diaSemana, horaInicio, horaFin);
		int status = grupoServ.updateById(id, nuevo);
		if (status == 0) {
			return new ResponseEntity<>("Grupo actualizado exitosamente", HttpStatus.ACCEPTED);
		}
		String detalle = String.join(" | ", grupoServ.diagnosticarReglasGrupo(nuevo, id));
		return new ResponseEntity<>("Error al actualizar grupo: " + detalle, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Elimina un grupo por id. 202 si fue exitoso, 404 si no se encontro.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/eliminar/{id}")
	@Operation(summary = "Eliminar grupo", description = "Elimina un grupo por su id. Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Grupo eliminado exitosamente"),
		@ApiResponse(responseCode = "404", description = "Grupo no encontrado")
	})
	public ResponseEntity<String> eliminarPorId(@PathVariable Integer id) {
		int status = grupoServ.deleteById(id);
		if (status == 0) {
			return new ResponseEntity<>("Grupo eliminado exitosamente", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Error al eliminar", HttpStatus.NOT_FOUND);
		}
	}
}