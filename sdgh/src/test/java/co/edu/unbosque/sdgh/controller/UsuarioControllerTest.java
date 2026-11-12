package co.edu.unbosque.sdgh.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unbosque.sdgh.dto.UsuarioDTO;
import co.edu.unbosque.sdgh.entity.RolUsuario;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

	private MockMvc mockMvc;

	@Mock
	private UsuarioService usuarioServ;

	@InjectMocks
	private UsuarioController usuarioController;

	private UsuarioDTO usuarioDTO;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();

		usuarioDTO = new UsuarioDTO();
		usuarioDTO.setId(1L);
		usuarioDTO.setNombre("Martin Gonzalez");
		usuarioDTO.setCorreo("martin@unbosque.edu.co");
		usuarioDTO.setUsername("marting");
		usuarioDTO.setRol(RolUsuario.GESTOR);
	}

	@Test
	@DisplayName("POST /usuario/crear: Debe responder 201 CREATED al crear usuario")
	void debeCrearUsuarioExitosamente() throws Exception {
		when(usuarioServ.create(any(UsuarioDTO.class))).thenReturn(0);

		mockMvc.perform(post("/usuario/crear")
				.param("nombre", "Martin Gonzalez")
				.param("correo", "martin@unbosque.edu.co")
				.param("username", "marting")
				.param("password", "ClaveSegura123")
				.param("rol", "GESTOR"))
				.andExpect(status().isCreated())
				.andExpect(content().string("Usuario creado exitosamente"));
	}

	@Test
	@DisplayName("POST /usuario/crear: Debe responder 400 BAD_REQUEST ante datos invalidos")
	void debeResponder400AlCrearConDatosInvalidos() throws Exception {
		when(usuarioServ.create(any(UsuarioDTO.class)))
				.thenThrow(new DatosInvalidosException("Formato invalido"));

		mockMvc.perform(post("/usuario/crear")
				.param("nombre", "Martin")
				.param("correo", "correoInvalido")
				.param("username", "mg")
				.param("password", "123")
				.param("rol", "GESTOR"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("GET /usuario/mostrartodo: Debe responder 202 ACCEPTED cuando hay usuarios")
	void debeRetornarUsuarios() throws Exception {
		when(usuarioServ.getAll()).thenReturn(List.of(usuarioDTO));

		mockMvc.perform(get("/usuario/mostrartodo"))
				.andExpect(status().isAccepted());
	}

	@Test
	@DisplayName("GET /usuario/mostrartodo: Debe responder 204 NO_CONTENT cuando no hay usuarios")
	void debeRetornar204CuandoNoHayUsuarios() throws Exception {
		when(usuarioServ.getAll()).thenReturn(new ArrayList<>());

		mockMvc.perform(get("/usuario/mostrartodo"))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("GET /usuario/mostrarporcorreo: Debe responder 202 ACCEPTED al encontrar por correo")
	void debeBuscarPorCorreo() throws Exception {
		when(usuarioServ.getByCorreo("martin@unbosque.edu.co")).thenReturn(usuarioDTO);

		mockMvc.perform(get("/usuario/mostrarporcorreo")
				.param("correo", "martin@unbosque.edu.co"))
				.andExpect(status().isAccepted());
	}

	@Test
	@DisplayName("GET /usuario/mostrarporusername: Debe responder 202 ACCEPTED al encontrar por username")
	void debeBuscarPorUsername() throws Exception {
		when(usuarioServ.getByUsername("marting")).thenReturn(usuarioDTO);

		mockMvc.perform(get("/usuario/mostrarporusername")
				.param("username", "marting"))
				.andExpect(status().isAccepted());
	}

	@Test
	@DisplayName("PUT /usuario/actualizar: Debe responder 202 ACCEPTED al actualizar correctamente")
	void debeActualizarUsuario() throws Exception {
		when(usuarioServ.updateById(eq(1L), any(UsuarioDTO.class))).thenReturn(0);

		mockMvc.perform(put("/usuario/actualizar")
				.param("id", "1")
				.param("nombre", "Martin Modificado")
				.param("correo", "martin@unbosque.edu.co")
				.param("rol", "GESTOR"))
				.andExpect(status().isAccepted())
				.andExpect(content().string("Usuario actualizado exitosamente"));
	}

	@Test
	@DisplayName("GET /usuario/contar: Debe responder 202 ACCEPTED con el conteo de usuarios")
	void debeContarUsuarios() throws Exception {
		when(usuarioServ.count()).thenReturn(5L);

		mockMvc.perform(get("/usuario/contar"))
				.andExpect(status().isAccepted())
				.andExpect(content().string("5"));
	}
}
