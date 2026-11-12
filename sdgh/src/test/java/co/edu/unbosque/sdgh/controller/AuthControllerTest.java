package co.edu.unbosque.sdgh.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unbosque.sdgh.dto.UsuarioDTO;
import co.edu.unbosque.sdgh.entity.RolUsuario;
import co.edu.unbosque.sdgh.entity.Usuario;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.security.JwtUtil;
import co.edu.unbosque.sdgh.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

	private MockMvc mockMvc;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private UsuarioService usuarioServ;

	@InjectMocks
	private AuthController authController;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
	}

	@Test
	@DisplayName("POST /auth/login: Debe retornar 200 OK con JWT al autenticar exitosamente")
	void debeIniciarSesionExitosamente() throws Exception {
		Usuario usuario = new Usuario();
		usuario.setUsername("admin");
		usuario.setRol(RolUsuario.GESTOR);

		Authentication auth = new UsernamePasswordAuthenticationToken(usuario, "pass123", usuario.getAuthorities());

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(auth);
		when(jwtUtil.generateAccessToken(usuario)).thenReturn("mocked.jwt.token");

		mockMvc.perform(post("/auth/login")
				.param("nombreUsuario", "admin")
				.param("contrasenia", "pass123"))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("POST /auth/login: Debe retornar 401 UNAUTHORIZED con credenciales incorrectas")
	void debeRechazarCredencialesIncorrectas() throws Exception {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));

		mockMvc.perform(post("/auth/login")
				.param("nombreUsuario", "admin")
				.param("contrasenia", "claveEquivocada"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string("Credenciales invalidas"));
	}

	@Test
	@DisplayName("POST /auth/registro: Debe retornar 201 CREATED al registrar usuario exitosamente")
	void debeRegistrarUsuarioExitosamente() throws Exception {
		when(usuarioServ.create(any(UsuarioDTO.class))).thenReturn(0);

		mockMvc.perform(post("/auth/registro")
				.param("nombre", "Laura Gomez")
				.param("nombreUsuario", "laurag")
				.param("correoElectronico", "laura@unbosque.edu.co")
				.param("contrasenia", "ClaveSegura123"))
				.andExpect(status().isCreated())
				.andExpect(content().string("Usuario registrado exitosamente"));
	}

	@Test
	@DisplayName("POST /auth/registro: Debe retornar 409 CONFLICT si el usuario ya existe")
	void debeRetornar409CuandoUsuarioExiste() throws Exception {
		when(usuarioServ.create(any(UsuarioDTO.class)))
				.thenThrow(new RecursoDuplicadoException("Correo ya existe"));

		mockMvc.perform(post("/auth/registro")
				.param("nombre", "Laura Gomez")
				.param("nombreUsuario", "laurag")
				.param("correoElectronico", "laura@unbosque.edu.co")
				.param("contrasenia", "ClaveSegura123"))
				.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("POST /auth/registro: Debe retornar 400 BAD_REQUEST si los datos son invalidos")
	void debeRetornar400ConDatosInvalidos() throws Exception {
		when(usuarioServ.create(any(UsuarioDTO.class)))
				.thenThrow(new DatosInvalidosException("Contrasena debil"));

		mockMvc.perform(post("/auth/registro")
				.param("nombre", "Laura")
				.param("nombreUsuario", "laurag")
				.param("correoElectronico", "correoInvalido")
				.param("contrasenia", "123"))
				.andExpect(status().isBadRequest());
	}
}
