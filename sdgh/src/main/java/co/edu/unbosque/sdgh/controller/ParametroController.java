package co.edu.unbosque.sdgh.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.sdgh.dto.ParametroDTO;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.ParametroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST para consultar y configurar los parametros del sistema (RF9,
 * CU06). Rutas bajo /parametro. Consultar esta abierto a cualquier usuario
 * autenticado; actualizar requiere rol GESTOR.
 */
@RestController
@RequestMapping("/parametro")
@CrossOrigin(origins = { "http://localhost:8080" })
@Transactional
@Tag(name = "Parametros", description = "Endpoints para configurar las reglas del horario")
@SecurityRequirement(name = "bearerAuth")
public class ParametroController {

	/**
	 * Servicio que contiene la logica de los parametros.
	 */
	@Autowired
	private ParametroService parametroServ;

	/**
	 * Retorna los parametros editables con su valor actual. 202 siempre, porque
	 * los parametros tienen valores por defecto.
	 */
	@GetMapping("/mostrartodo")
	@Operation(summary = "Obtener parametros", description = "Retorna las franjas, la pausa, el cupo, la tolerancia y el maximo de sesiones vigentes.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente")
	})
	public ResponseEntity<List<ParametroDTO>> obtenerTodo() {
		return new ResponseEntity<>(parametroServ.getAll(), HttpStatus.ACCEPTED);
	}

	/**
	 * Cambia el valor de un parametro. El cambio se aplica a la siguiente
	 * validacion sin recompilar (RNF3).
	 */
	@PreAuthorize("hasRole('GESTOR')")
	@PutMapping("/actualizar")
	@Operation(summary = "Actualizar parametro", description = "Cambia el valor de un parametro (CU06). Requiere rol GESTOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Parametro actualizado exitosamente"),
		@ApiResponse(responseCode = "400", description = "Valor con formato invalido o fuera de rango"),
		@ApiResponse(responseCode = "404", description = "El parametro no existe")
	})
	public ResponseEntity<String> actualizar(@RequestParam String clave, @RequestParam String valor) {
		try {
			parametroServ.updateByClave(clave, valor);
			return new ResponseEntity<>("Parametro actualizado exitosamente", HttpStatus.ACCEPTED);
		} catch (RecursoNoEncontradoException e) {
			return new ResponseEntity<>("Error al actualizar parametro: " + e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (DatosInvalidosException e) {
			return new ResponseEntity<>("Error al actualizar parametro: " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
