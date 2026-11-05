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
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

/**
 * Controlador REST para gestionar los usuarios del aplicativo. Rutas bajo
 * /usuario. Todos los endpoints requieren rol GESTOR. Las excepciones del
 * service se capturan aqui y se traducen a codigos HTTP.
 */
@RestController
@RequestMapping("/usuario")
@CrossOrigin(origins = { "http://localhost:8080" })
@Transactional
@Tag(name = "Usuarios", description = "Endpoints para la gestion de usuarios")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('GESTOR')")
public class UsuarioController {

	/**
	 * Servicio que contiene la logica de negocio de los usuarios.
	 */
	@Autowired
	private UsuarioService usuarioServ;

	@PostMapping("/crear")
	@Operation(summary = "Crear usuario", description = "Registra un nuevo usuario. Requiere rol GESTOR.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
		@ApiResponse(responseCode = "400", description = "Datos invalidos"),
		@ApiResponse(responseCode = "409", description = "Codigo, correo o username duplicado")
	})
	public ResponseEntity<String> crear(@RequestParam String nombre,
			@RequestParam String correo, @RequestParam String username, @RequestParam String password,
			@RequestParam String rol) {
		try {
			UsuarioDTO nuevo = new UsuarioDTO(nombre, correo, username, password, rol);
			usuarioServ.create(nuevo);
			return new ResponseEntity<>("Usuario creado exitosamente", HttpStatus.CREATED);
		} catch (DatosInvalidosException | RecursoDuplicadoException e) {
			return new ResponseEntity<>("Error al crear usuario: " + e.getMessage(), estado(e));
		}
	}

	@GetMapping("/mostrartodo")
	@Operation(summary = "Obtener todos los usuarios", description = "Retorna la lista de todos los usuarios registrados.")
	public ResponseEntity<List<UsuarioDTO>> obtenerTodo() {
		List<UsuarioDTO> lista = usuarioServ.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.ACCEPTED);
	}

	@GetMapping("/mostrar/{codigo}")
	@Operation(summary = "Obtener usuario por codigo", description = "Retorna un usuario segun su codigo.")
	public ResponseEntity<UsuarioDTO> obtenerPorCodigo(@PathVariable Long id) {
		try {
			return new ResponseEntity<>(usuarioServ.getById(id), HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException e) {
			return new ResponseEntity<>(estado(e));
		}
	}

	@GetMapping("/mostrarporcorreo")
	@Operation(summary = "Obtener usuario por correo", description = "Retorna un usuario segun su correo institucional.")
	public ResponseEntity<UsuarioDTO> obtenerPorCorreo(@RequestParam String correo) {
		try {
			return new ResponseEntity<>(usuarioServ.getByCorreo(correo), HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException e) {
			return new ResponseEntity<>(estado(e));
		}
	}

	@GetMapping("/mostrarporusername")
	@Operation(summary = "Obtener usuario por username", description = "Retorna un usuario segun su nombre de usuario (login).")
	public ResponseEntity<UsuarioDTO> obtenerPorUsername(@RequestParam String username) {
		try {
			return new ResponseEntity<>(usuarioServ.getByUsername(username), HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException e) {
			return new ResponseEntity<>(estado(e));
		}
	}

	@GetMapping("/contar")
	@Operation(summary = "Contar usuarios", description = "Retorna el numero total de usuarios registrados.")
	public ResponseEntity<Long> contar() {
		long count = usuarioServ.count();
		if (count == 0) {
			return new ResponseEntity<>(count, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(count, HttpStatus.ACCEPTED);
	}

	@GetMapping("/existe/{codigo}")
	@Operation(summary = "Verificar existencia de usuario", description = "Verifica si existe un usuario con el codigo proporcionado.")
	public ResponseEntity<Boolean> existe(@PathVariable Long id) {
		if (usuarioServ.existById(id)) {
			return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
		}
		return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);
	}

	@PutMapping("/actualizar")
	@Operation(summary = "Actualizar usuario", description = "Actualiza nombre, correo y rol de un usuario por su codigo. La contrasena es opcional.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "202", description = "Usuario actualizado exitosamente"),
		@ApiResponse(responseCode = "400", description = "Datos invalidos"),
		@ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
		@ApiResponse(responseCode = "409", description = "Correo de otro usuario")
	})
	public ResponseEntity<String> actualizar(@RequestParam Long id, @RequestParam String nombre,
			@RequestParam String correo, @RequestParam String rol,
			@RequestParam(required = false) String password) {
		try {
			UsuarioDTO nuevo = new UsuarioDTO();
			nuevo.setId(id);
			nuevo.setNombre(nombre);
			nuevo.setCorreo(correo);
			nuevo.setRol(rol);
			nuevo.setPassword(password);
			usuarioServ.updateById(id, nuevo);
			return new ResponseEntity<>("Usuario actualizado exitosamente", HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException | RecursoDuplicadoException e) {
			return new ResponseEntity<>("Error al actualizar usuario: " + e.getMessage(), estado(e));
		}
	}

	@DeleteMapping("/eliminar/{codigo}")
	@Operation(summary = "Eliminar usuario", description = "Elimina un usuario por su codigo.")
	public ResponseEntity<String> eliminarPorCodigo(@PathVariable Long id) {
		try {
			usuarioServ.deleteById(id);
			return new ResponseEntity<>("Usuario eliminado exitosamente", HttpStatus.ACCEPTED);
		} catch (DatosInvalidosException | RecursoNoEncontradoException e) {
			return new ResponseEntity<>("Error al eliminar: " + e.getMessage(), estado(e));
		}
	}

	/**
	 * Traduce la excepcion del service al codigo HTTP correspondiente.
	 * @param e Excepcion capturada.
	 * @return Retorna el codigo HTTP.
	 */
	public HttpStatus estado(RuntimeException e) {
		if (e instanceof RecursoNoEncontradoException) {
			return HttpStatus.NOT_FOUND;
		}
		if (e instanceof RecursoDuplicadoException) {
			return HttpStatus.CONFLICT;
		}
		return HttpStatus.BAD_REQUEST;
	}
}