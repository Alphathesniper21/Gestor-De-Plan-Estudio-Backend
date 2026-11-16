package co.edu.unbosque.sdgh.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.sdgh.dto.UsuarioDTO;
import co.edu.unbosque.sdgh.entity.RolUsuario;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.UsuarioService;

/**
 * Pruebas unitarias para el controlador REST {@link UsuarioController}.
 * <p>
 * Verifica los endpoints HTTP para la administración de usuarios: creación con
 * HTTP 201 CREATED o 400 BAD REQUEST, consultas por código, correo y username,
 * actualización (incluyendo actualización de {@code username}), conteo,
 * verificación de existencia y eliminación.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see UsuarioController
 * @see UsuarioService
 */
class UsuarioControllerTest {

	/**
	 * Mock del servicio de usuarios.
	 */
	@Mock
	private UsuarioService usuarioServ;

	/**
	 * Instancia del controlador bajo prueba.
	 */
	private UsuarioController controller;

	/**
	 * DTO de referencia utilizado en las pruebas.
	 */
	private UsuarioDTO dto;

	/**
	 * Inicializa los mocks e inyecta las dependencias en el controlador por
	 * reflexión antes de cada prueba.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		controller = new UsuarioController();
		ReflectionTestUtils.setField(controller, "usuarioServ", usuarioServ);

		dto = new UsuarioDTO();
		dto.setId(1L);
		dto.setNombre("Martin Pena");
		dto.setCorreo("martin@unbosque.edu.co");
		dto.setUsername("martinp");
		dto.setRol(RolUsuario.GESTOR);
	}

	// ─── CREAR ────────────────────────────────────────────────────────────────

	/**
	 * Verifica que el endpoint de crear usuario retorna HTTP 201 CREATED cuando
	 * los datos son válidos.
	 */
	@Test
	void testCrear_Exitoso() {
		when(usuarioServ.create(any(UsuarioDTO.class))).thenReturn(0);

		ResponseEntity<String> response = controller.crear("Martin Pena", "martin@unbosque.edu.co", "martinp",
				"ClaveSegura123", RolUsuario.GESTOR);

		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("Usuario creado exitosamente", response.getBody());
	}

	/**
	 * Verifica que el endpoint de crear usuario retorna HTTP 400 BAD REQUEST cuando
	 * se envían datos inválidos.
	 */
	@Test
	void testCrear_RetornaBadRequestPorDatosInvalidos() {
		when(usuarioServ.create(any(UsuarioDTO.class)))
				.thenThrow(new DatosInvalidosException("Formato de correo invalido"));

		ResponseEntity<String> response = controller.crear("Martin", "correoInvalido", "martinp", "123",
				RolUsuario.GESTOR);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Formato de correo invalido"));
	}

	// ─── CONSULTAS ────────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code obtenerTodo} retorna HTTP 202 ACCEPTED con la lista de
	 * usuarios cuando existen registros.
	 */
	@Test
	void testObtenerTodo_Exitoso() {
		when(usuarioServ.getAll()).thenReturn(List.of(dto));

		ResponseEntity<List<UsuarioDTO>> response = controller.obtenerTodo();

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1, response.getBody().size());
	}

	/**
	 * Verifica que {@code obtenerTodo} retorna HTTP 204 NO CONTENT cuando la lista
	 * está vacía.
	 */
	@Test
	void testObtenerTodo_RetornaNoContent() {
		when(usuarioServ.getAll()).thenReturn(Collections.emptyList());

		ResponseEntity<List<UsuarioDTO>> response = controller.obtenerTodo();

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	/**
	 * Verifica que {@code obtenerPorCodigo} retorna HTTP 202 ACCEPTED cuando el
	 * usuario existe, o 404 NOT FOUND en caso contrario.
	 */
	@Test
	void testObtenerPorCodigo_ExitosoYNoEncontrado() {
		when(usuarioServ.getById(1L)).thenReturn(dto);
		ResponseEntity<UsuarioDTO> ok = controller.obtenerPorCodigo(1L);
		assertEquals(HttpStatus.ACCEPTED, ok.getStatusCode());
		assertEquals("martinp", ok.getBody().getUsername());

		when(usuarioServ.getById(999L)).thenThrow(new RecursoNoEncontradoException("No existe"));
		ResponseEntity<UsuarioDTO> notFound = controller.obtenerPorCodigo(999L);
		assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
	}

	/**
	 * Verifica que {@code obtenerPorCorreo} retorna HTTP 202 ACCEPTED cuando el
	 * usuario existe.
	 */
	@Test
	void testObtenerPorCorreo_Exitoso() {
		when(usuarioServ.getByCorreo("martin@unbosque.edu.co")).thenReturn(dto);

		ResponseEntity<UsuarioDTO> response = controller.obtenerPorCorreo("martin@unbosque.edu.co");

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Martin Pena", response.getBody().getNombre());
	}

	/**
	 * Verifica que {@code obtenerPorUsername} retorna HTTP 202 ACCEPTED cuando el
	 * usuario existe.
	 */
	@Test
	void testObtenerPorUsername_Exitoso() {
		when(usuarioServ.getByUsername("martinp")).thenReturn(dto);

		ResponseEntity<UsuarioDTO> response = controller.obtenerPorUsername("martinp");

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("martinp", response.getBody().getUsername());
	}

	// ─── ACTUALIZAR Y ELIMINAR ────────────────────────────────────────────────

	/**
	 * Verifica que {@code actualizar} (incluyendo el parámetro {@code username})
	 * retorna HTTP 202 ACCEPTED cuando la actualización es exitosa.
	 */
	@Test
	void testActualizar_Exitoso() {
		when(usuarioServ.updateById(eq(1L), any(UsuarioDTO.class))).thenReturn(0);

		ResponseEntity<String> response = controller.actualizar(1L, "Martin Modificado", "martin@unbosque.edu.co",
				RolUsuario.GESTOR, "martin_nuevo", null);

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertEquals("Usuario actualizado exitosamente", response.getBody());
	}

	/**
	 * Verifica que {@code eliminarPorCodigo} retorna HTTP 202 ACCEPTED cuando el
	 * usuario es eliminado correctamente.
	 */
	@Test
	void testEliminarPorCodigo_Exitoso() {
		when(usuarioServ.deleteById(1L)).thenReturn(0);

		ResponseEntity<String> response = controller.eliminarPorCodigo(1L);

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertEquals("Usuario eliminado exitosamente", response.getBody());
	}

	/**
	 * Verifica que {@code contar} y {@code existe} retornan HTTP 202 ACCEPTED
	 * cuando hay usuarios registrados.
	 */
	@Test
	void testContarYExiste_Exitoso() {
		when(usuarioServ.count()).thenReturn(5L);
		when(usuarioServ.existById(1L)).thenReturn(true);

		ResponseEntity<Long> response = controller.contar();
		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertEquals(5L, response.getBody());

		ResponseEntity<Boolean> existeResp = controller.existe(1L);
		assertEquals(HttpStatus.ACCEPTED, existeResp.getStatusCode());
		assertTrue(existeResp.getBody());
	}
}
