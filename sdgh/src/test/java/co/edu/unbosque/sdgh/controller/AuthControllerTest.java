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
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.sdgh.controller.AuthController.AuthResponse;
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
 * Verifica los endpoints de autenticación del sistema (CU01 / RS-8): inicio de
 * sesión ({@code /auth/login}) con emisión de token JWT y rol (200 OK) o
 * rechazo de credenciales inválidas (401 UNAUTHORIZED), y registro de nuevos
 * usuarios ({@code /auth/registro}) con respuestas 201 CREATED, 409 CONFLICT y
 * 400 BAD REQUEST.
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

	/**
	 * Mock del administrador de autenticación de Spring Security.
	 */
	@Mock
	private AuthenticationManager authenticationManager;

	/**
	 * Mock de la utilidad de generación y validación de tokens JWT.
	 */
	@Mock
	private JwtUtil jwtUtil;

	/**
	 * Mock del servicio de usuarios.
	 */
	@Mock
	private UsuarioService usuarioServ;

	/**
	 * Instancia del controlador bajo prueba.
	 */
	private AuthController controller;

	/**
	 * Inicializa los mocks e inyecta las dependencias en el controlador por
	 * reflexión antes de cada prueba.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		controller = new AuthController();
		ReflectionTestUtils.setField(controller, "authenticationManager", authenticationManager);
		ReflectionTestUtils.setField(controller, "jwtUtil", jwtUtil);
		ReflectionTestUtils.setField(controller, "usuarioServ", usuarioServ);
	}

	// ─── LOGIN (CU01) ─────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code login} retorna HTTP 200 OK junto con el token JWT y el
	 * rol del usuario cuando las credenciales son válidas.
	 */
	@Test
	void testLogin_Exitoso() {
		Usuario usuario = new Usuario();
		usuario.setUsername("gestor");
		usuario.setRol(RolUsuario.GESTOR);

		Authentication auth = new UsernamePasswordAuthenticationToken(usuario, "Admin123", usuario.getAuthorities());

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
		when(jwtUtil.generateToken(usuario)).thenReturn("mocked.jwt.token");

		ResponseEntity<Object> response = controller.login("gestor", "Admin123");

		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertInstanceOf(AuthResponse.class, response.getBody());
		AuthResponse body = (AuthResponse) response.getBody();
		assertEquals("mocked.jwt.token", body.getToken());
		assertEquals(RolUsuario.GESTOR, body.getRol());
	}

	/**
	 * Verifica que {@code login} retorna HTTP 401 UNAUTHORIZED cuando el
	 * {@link AuthenticationManager} lanza {@link BadCredentialsException}.
	 */
	@Test
	void testLogin_RechazaCredencialesIncorrectas() {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));

		ResponseEntity<Object> response = controller.login("gestor", "claveIncorrecta");

		assertNotNull(response);
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertEquals("Credenciales invalidas", response.getBody());
	}

	/**
	 * Verifica que {@code login} retorna HTTP 401 UNAUTHORIZED cuando el objeto
	 * {@link Authentication} retornado es nulo.
	 */
	@Test
	void testLogin_RechazaAutenticacionNula() {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);

		ResponseEntity<Object> response = controller.login("gestor", "Admin123");

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertEquals("Credenciales invalidas", response.getBody());
	}

	// ─── REGISTRO ─────────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code registro} retorna HTTP 201 CREATED cuando el nuevo
	 * usuario se registra exitosamente con rol {@link RolUsuario#CONSULTANTE}.
	 */
	@Test
	void testRegistro_Exitoso() {
		when(usuarioServ.create(any(UsuarioDTO.class))).thenReturn(0);

		ResponseEntity<Object> response = controller.registro("Laura Gomez", "laurag", "laura@unbosque.edu.co",
				"ClaveSegura123");

		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("Usuario registrado exitosamente", response.getBody());
		verify(usuarioServ, times(1)).create(any(UsuarioDTO.class));
	}

	/**
	 * Verifica que {@code registro} retorna HTTP 409 CONFLICT cuando el nombre de
	 * usuario o el correo electrónico ya existen.
	 */
	@Test
	void testRegistro_RetornaConflictPorDuplicado() {
		when(usuarioServ.create(any(UsuarioDTO.class)))
				.thenThrow(new RecursoDuplicadoException("Ya existe un usuario con el correo laura@unbosque.edu.co"));

		ResponseEntity<Object> response = controller.registro("Laura Gomez", "laurag", "laura@unbosque.edu.co",
				"ClaveSegura123");

		assertNotNull(response);
		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertEquals("Ya existe un usuario con el correo laura@unbosque.edu.co", response.getBody());
	}

	/**
	 * Verifica que {@code registro} retorna HTTP 400 BAD REQUEST cuando los datos
	 * proporcionados no superan las validaciones de formato o seguridad.
	 */
	@Test
	void testRegistro_RetornaBadRequestPorDatosInvalidos() {
		when(usuarioServ.create(any(UsuarioDTO.class)))
				.thenThrow(new DatosInvalidosException("La contrasena debe tener minimo 8 caracteres"));

		ResponseEntity<Object> response = controller.registro("Laura", "laurag", "correoInvalido", "123");

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("La contrasena debe tener minimo 8 caracteres", response.getBody());
	}
}
