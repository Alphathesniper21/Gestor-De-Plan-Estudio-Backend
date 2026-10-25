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

import co.edu.unbosque.sdgh.dto.UsuarioDTO;
import co.edu.unbosque.sdgh.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST para gestionar los usuarios del aplicativo (CU01, CU09 - RF11, RF22, RF24).
 * Rutas bajo /usuario. Todos los endpoints requieren rol ADMINISTRADOR.
 */
@RestController
@RequestMapping("/usuario")
@CrossOrigin(origins = { "http://localhost:8080"})
@Transactional
@Tag(name = "Usuarios", description = "Endpoints para la gestion de usuarios")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

	/**
	 * Servicio que contiene la logica de negocio de los usuarios.
	 */
	@Autowired
	private UsuarioService usuarioServ;

	/**
	 * Crea un nuevo usuario. Retorna 201 si fue exitoso, 406 si hubo error.
	 */
	@PostMapping("/crear")
	@Operation(summary = "Crear usuario", description = "Registra un nuevo usuario. Requiere rol ADMINISTRADOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
		@ApiResponse(responseCode = "406", description = "Error al crear usuario (datos invalidos o codigo/correo duplicado)")
	})
	public ResponseEntity<String> crear(@RequestParam String nombre, @RequestParam int codigo,
			@RequestParam String correo, @RequestParam String facultad) {
		UsuarioDTO nuevo = new UsuarioDTO(nombre, codigo, correo, facultad);
		int status = usuarioServ.create(nuevo);
		if (status == 0) {
			return new ResponseEntity<>("Usuario creado exitosamente", HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>("Error al crear usuario", HttpStatus.NOT_ACCEPTABLE);
		}
	}

	/**
	 * Retorna todos los usuarios. 202 si hay datos, 204 si la lista esta vacia.
	 */
	@GetMapping("/mostrartodo")
	@Operation(summary = "Obtener todos los usuarios", description = "Retorna la lista de todos los usuarios registrados.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay usuarios registrados")
	})
	public ResponseEntity<List<UsuarioDTO>> obtenerTodo() {
		List<UsuarioDTO> lista = usuarioServ.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Busca un usuario por codigo. 202 si existe, 404 si no se encuentra.
	 */
	@GetMapping("/mostrar/{codigo}")
	@Operation(summary = "Obtener usuario por codigo", description = "Retorna un usuario segun su codigo.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Usuario encontrado"),
		@ApiResponse(responseCode = "404", description = "Usuario no encontrado")
	})
	public ResponseEntity<UsuarioDTO> obtenerPorCodigo(@PathVariable Integer codigo) {
		UsuarioDTO dto = usuarioServ.getById(codigo);
		if (dto != null) {
			return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Busca un usuario por correo. 202 si existe, 404 si no se encuentra.
	 */
	@GetMapping("/mostrarporcorreo")
	@Operation(summary = "Obtener usuario por correo", description = "Retorna un usuario segun su correo institucional.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Usuario encontrado"),
		@ApiResponse(responseCode = "404", description = "Usuario no encontrado")
	})
	public ResponseEntity<UsuarioDTO> obtenerPorCorreo(@RequestParam String correo) {
		UsuarioDTO dto = usuarioServ.getByCorreo(correo);
		if (dto != null) {
			return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Busca un usuario por nombre de usuario. 202 si existe, 404 si no se encuentra.
	 */
	@GetMapping("/mostrarporusername")
	@Operation(summary = "Obtener usuario por username", description = "Retorna un usuario segun su nombre de usuario (login).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Usuario encontrado"),
		@ApiResponse(responseCode = "404", description = "Usuario no encontrado")
	})
	public ResponseEntity<UsuarioDTO> obtenerPorUsername(@RequestParam String username) {
		UsuarioDTO dto = usuarioServ.getByUsername(username);
		if (dto != null) {
			return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Retorna los usuarios de una facultad. 202 si hay datos, 204 si no hay ninguno.
	 */
	@GetMapping("/mostrarporfacultad")
	@Operation(summary = "Obtener usuarios por facultad", description = "Retorna los usuarios que pertenecen a una facultad.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Lista retornada exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay usuarios en esa facultad")
	})
	public ResponseEntity<List<UsuarioDTO>> obtenerPorFacultad(@RequestParam String facultad) {
		List<UsuarioDTO> lista = usuarioServ.getByFacultad(facultad);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Retorna el total de usuarios. 202 si hay registros, 204 si no hay ninguno.
	 */
	@GetMapping("/contar")
	@Operation(summary = "Contar usuarios", description = "Retorna el numero total de usuarios registrados.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Conteo retornado exitosamente"),
		@ApiResponse(responseCode = "204", description = "No hay usuarios registrados")
	})
	public ResponseEntity<Long> contar() {
		Long count = usuarioServ.count();
		if (count == 0) {
			return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
		}
	}

	/**
	 * Verifica si existe un usuario por codigo. 202 si existe, 204 si no se encuentra.
	 */
	@GetMapping("/existe/{codigo}")
	@Operation(summary = "Verificar existencia de usuario", description = "Verifica si existe un usuario con el codigo proporcionado.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Usuario encontrado"),
		@ApiResponse(responseCode = "204", description = "Usuario no encontrado")
	})
	public ResponseEntity<Boolean> existe(@PathVariable Integer codigo) {
		boolean encontrado = usuarioServ.exist(codigo);
		if (encontrado) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * Actualiza un usuario por codigo. 202 exitoso, 404 no encontrado, 400 error.
	 */
	@PutMapping("/actualizar")
	@Operation(summary = "Actualizar usuario", description = "Actualiza nombre, correo y facultad de un usuario por su codigo.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Usuario actualizado exitosamente"),
		@ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
		@ApiResponse(responseCode = "400", description = "Error al actualizar (datos invalidos o correo de otro usuario)")
	})
	public ResponseEntity<String> actualizar(@RequestParam Integer codigo, @RequestParam String nombre,
			@RequestParam String correo, @RequestParam String facultad) {
		if (!usuarioServ.exist(codigo)) {
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.NOT_FOUND);
		}
		UsuarioDTO nuevo = new UsuarioDTO(nombre, codigo, correo, facultad);
		int status = usuarioServ.updateById(codigo, nuevo);
		if (status == 0) {
			return new ResponseEntity<>("Usuario actualizado exitosamente", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Error al actualizar usuario", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Elimina un usuario por codigo. 202 si fue exitoso, 404 si no se encontro.
	 */
	@DeleteMapping("/eliminar/{codigo}")
	@Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su codigo.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Usuario eliminado exitosamente"),
		@ApiResponse(responseCode = "404", description = "Usuario no encontrado")
	})
	public ResponseEntity<String> eliminarPorCodigo(@PathVariable Integer codigo) {
		int status = usuarioServ.deleteById(codigo);
		if (status == 0) {
			return new ResponseEntity<>("Usuario eliminado exitosamente", HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>("Error al eliminar", HttpStatus.NOT_FOUND);
		}
	}
}