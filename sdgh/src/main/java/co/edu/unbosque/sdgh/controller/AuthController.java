package co.edu.unbosque.sdgh.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
 * Controlador REST publico para login y registro. Rutas bajo /auth.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = { "http://localhost:8080" })
@Tag(name = "Autenticacion", description = "Endpoints para login y registro de usuarios")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UsuarioService usuarioService;

	public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
			UsuarioService usuarioService) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.usuarioService = usuarioService;
	}

	@PostMapping("/login")
	@Operation(summary = "Iniciar sesion", description = "Autentica al usuario y retorna un token JWT junto con su rol.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Login exitoso, retorna token JWT y rol"),
		@ApiResponse(responseCode = "401", description = "Credenciales invalidas")
	})
	public ResponseEntity<?> login(@RequestBody UsuarioDTO loginRequest) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			String jwt = jwtUtil.generateToken(userDetails);

			String role = null;
			if (userDetails instanceof Usuario) {
				Usuario usuario = (Usuario) userDetails;
				role = usuario.getRol();
			}
			return ResponseEntity.ok(new AuthResponse(jwt, role));
		} catch (AuthenticationException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("Nombre de usuario o contrasena invalidos o usuario no encontrado");
		}
	}

	@PostMapping("/register")
	@Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario con rol CONSULTA.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
		@ApiResponse(responseCode = "400", description = "Datos invalidos"),
		@ApiResponse(responseCode = "409", description = "El correo o el nombre de usuario ya existen")
	})
	public ResponseEntity<?> register(@RequestBody UsuarioDTO registerRequest) {
		try {
			// El registro publico nunca decide el id ni el rol
			registerRequest.setId(0);
			registerRequest.setRol("CONSULTA");
			usuarioService.create(registerRequest);
			return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente");
		} catch (RecursoDuplicadoException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
		} catch (DatosInvalidosException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	private static class AuthResponse {

		private final String token;
		private final String role;

		public AuthResponse(String token, String role) {
			this.token = token;
			this.role = role;
		}

		public String getToken() {
			return token;
		}

		public String getRole() {
			return role;
		}
	}
}