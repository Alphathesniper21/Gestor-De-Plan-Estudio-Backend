/**
 * Paquete que contiene los controladores gestores de los endpoints del aplicativo.
 */
package co.edu.unbosque.sdgh.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.sdgh.dto.UsuarioDTO;
import co.edu.unbosque.sdgh.entity.Usuario;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.security.JwtUtil;
import co.edu.unbosque.sdgh.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para la autenticacion de usuarios.
 * Maneja los endpoints de inicio de sesion y registro de usuarios.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = { "http://localhost:8080" })
@Tag(name = "Autenticacion", description = "Endpoints para login y registro de usuarios")
public class AuthController {

	/**
	 * Gestor de autenticacion para validar las credenciales de usuarios.
	 */
	@Autowired
	private AuthenticationManager authenticationManager;

	/**
	 * Utilidad para la generacion y validacion de tokens JWT.
	 */
	@Autowired
	private JwtUtil jwtUtil;

	/**
	 * Servicio para operaciones relacionadas con usuarios.
	 */
	@Autowired
	private UsuarioService usuarioServ;

	/**
	 * Constructor vacio de la clase AuthController.
	 */
	public AuthController() {
	}

	/**
	 * Metodo endpoint para el inicio de sesion de usuarios.
	 * Permite autenticar usuarios y genera un token JWT si las credenciales son validas.
	 * @param nombreUsuario Nombre de usuario.
	 * @param contrasenia Contrasenia del usuario.
	 * @return ResponseEntity con el token JWT y el rol del usuario si la autenticacion es exitosa,
	 * o un mensaje de error si las credenciales son invalidas.
	 */
	@PostMapping("/login")
	@Operation(summary = "Iniciar sesion", description = "Autentica al usuario y retorna un token JWT junto con su rol.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Login exitoso, retorna token JWT y rol"),
		@ApiResponse(responseCode = "401", description = "Credenciales invalidas")
	})
	public ResponseEntity<Object> login(@RequestParam String nombreUsuario, @RequestParam String contrasenia) {

		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(nombreUsuario, contrasenia));

			if (authentication == null || !authentication.isAuthenticated()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales invalidas");
			}

			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			String jwt = jwtUtil.generateAccessToken(userDetails);

			String rol = null;
			if (userDetails instanceof Usuario) {
				Usuario usuario = (Usuario) userDetails;
				rol = usuario.getRol();
			}

			return ResponseEntity.ok(new AuthResponse(jwt, rol));
		} catch (AuthenticationException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales invalidas");
		}
	}

	/**
	 * Metodo endpoint para el registro de nuevos usuarios.
	 * Crea un nuevo usuario con rol CONSULTA si los datos son validos y no existen duplicados.
	 * @param id Codigo del usuario.
	 * @param nombre Nombre completo del usuario.
	 * @param nombreUsuario Nombre de usuario deseado.
	 * @param correoElectronico Correo electronico del usuario.
	 * @param contrasenia Contrasenia del usuario.
	 * @return ResponseEntity con mensaje de exito si el registro es exitoso, o un mensaje
	 * de error si falla el registro.
	 */
	@PostMapping("/registro")
	@Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario con rol CONSULTA por defecto.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
		@ApiResponse(responseCode = "409", description = "El codigo, el correo o el nombre de usuario ya existen"),
		@ApiResponse(responseCode = "400", description = "Error al registrar usuario")
	})
	public ResponseEntity<Object> registro(@RequestParam String nombre,
			@RequestParam String nombreUsuario, @RequestParam String correoElectronico,
			@RequestParam String contrasenia) {

		UsuarioDTO nuevo = new UsuarioDTO();
		nuevo.setNombre(nombre);
		nuevo.setUsername(nombreUsuario);
		nuevo.setCorreo(correoElectronico);
		nuevo.setPassword(contrasenia);
		nuevo.setRol("GESTOR");

		try {
			usuarioServ.create(nuevo);
			return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente");
		} catch (RecursoDuplicadoException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (DatosInvalidosException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	/**
	 * Clase interna que representa la respuesta de autenticacion: token JWT y
	 * rol del usuario autenticado.
	 */
	public static class AuthResponse {

		/**
		 * Token JWT generado para el usuario autenticado.
		 */
		private final String token;

		/**
		 * Rol del usuario autenticado (GESTOR o CONSULTA).
		 */
		private final String rol;

		/**
		 * Constructor con token y rol.
		 * @param token Token JWT generado.
		 * @param rol Rol del usuario.
		 */
		public AuthResponse(String token, String rol) {
			this.token = token;
			this.rol = rol;
		}

		/**
		 * Obtiene el token JWT.
		 * @return Token JWT.
		 */
		public String getToken() {
			return token;
		}

		/**
		 * Obtiene el rol del usuario.
		 * @return Rol del usuario.
		 */
		public String getRol() {
			return rol;
		}
	}
}