package co.edu.unbosque.sdgh.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import co.edu.unbosque.sdgh.dto.UsuarioDTO;
import co.edu.unbosque.sdgh.entity.RolUsuario;
import co.edu.unbosque.sdgh.entity.Usuario;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.security.JwtUtil;
import co.edu.unbosque.sdgh.service.UsuarioService;

/**
 * Pruebas unitarias para el controlador REST {@link AuthController}.
 * <p>
 * Verifica los endpoints de autenticacion del sistema: inicio de sesion (login)
 * con emision de token JWT y rol (200 OK) o rechazo de credenciales invalidas
 * (401 UNAUTHORIZED), y registro de nuevos usuarios (201 CREATED, 409 CONFLICT,
 * 400 BAD REQUEST).
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see AuthController
 * @see JwtUtil
 * @see UsuarioService
 */
class AuthControllerTest {

	/** Mock del AuthenticationManager de Spring Security. */
	@Mock
	private AuthenticationManager authenticationManager;

	/** Mock de la utilidad de tokens JWT. */
	@Mock
	private JwtUtil jwtUtil;

	/** Mock del servicio de usuarios. */
	@Mock
	private UsuarioService usuarioServ;

	/** Instancia del controlador bajo prueba. */
	private AuthController controller;

	/**
	 * Prepara los mocks e inyecta las dependencias en el controlador antes de cada
	 * prueba.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		controller = new AuthController();

		try {
			var f1 = AuthController.class.getDeclaredField("authenticationManager");
			f1.setAccessible(true);
			f1.set(controller, authenticationManager);

			var f2 = AuthController.class.getDeclaredField("jwtUtil");
			f2.setAccessible(true);
			f2.set(controller, jwtUtil);

			var f3 = AuthController.class.getDeclaredField("usuarioServ");
			f3.setAccessible(true);
			f3.set(controller, usuarioServ);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ─── LOGIN ────────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code login} retorna HTTP 200 OK y la estructura de respuesta
	 * con el token JWT cuando las credenciales son correctas.
	 */
	@Test
	void testLogin_Exitoso() {
		Usuario usuario = new Usuario();
		usuario.setUsername("admin");
		usuario.setRol(RolUsuario.GESTOR);

		Authentication auth = new UsernamePasswordAuthenticationToken(usuario, "pass123", usuario.getAuthorities());

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(auth);
		when(jwtUtil.generateAccessToken(usuario)).thenReturn("mocked.jwt.token");

		ResponseEntity<Object> response = controller.login("admin", "pass123");

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
	}

	/**
	 * Verifica que {@code login} retorna HTTP 401 UNAUTHORIZED cuando las
	 * credenciales son incorrectas.
	 */
	@Test
	void testLogin_RechazaCredencialesIncorrectas() {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));

		ResponseEntity<Object> response = controller.login("admin", "claveIncorrecta");

		assertNotNull(response);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertEquals("Credenciales invalidas", response.getBody());
	}

	// ─── REGISTRO ─────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code registro} retorna HTTP 201 CREATED al registrar
	 * exitosamente un nuevo usuario.
	 */
	@Test
	void testRegistro_Exitoso() {
		when(usuarioServ.create(any(UsuarioDTO.class))).thenReturn(0);

		ResponseEntity<Object> response = controller.registro("Laura Gomez", "laurag", "laura@unbosque.edu.co",
				"ClaveSegura123");

		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("Usuario registrado exitosamente", response.getBody());
	}

	/**
	 * Verifica que {@code registro} retorna HTTP 409 CONFLICT cuando el usuario o
	 * correo ya existen.
	 */
	@Test
	void testRegistro_RetornaConflictPorDuplicado() {
		when(usuarioServ.create(any(UsuarioDTO.class)))
				.thenThrow(new RecursoDuplicadoException("El correo ya existe"));

		ResponseEntity<Object> response = controller.registro("Laura Gomez", "laurag", "laura@unbosque.edu.co",
				"ClaveSegura123");

		assertNotNull(response);
		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertEquals("El correo ya existe", response.getBody());
	}

	/**
	 * Verifica que {@code registro} retorna HTTP 400 BAD REQUEST cuando los datos
	 * ingresados son invalidos.
	 */
	@Test
	void testRegistro_RetornaBadRequestPorDatosInvalidos() {
		when(usuarioServ.create(any(UsuarioDTO.class)))
				.thenThrow(new DatosInvalidosException("Contrasena debil"));

		ResponseEntity<Object> response = controller.registro("Laura", "laurag", "correoInvalido", "123");

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Contrasena debil", response.getBody());
	}
}
