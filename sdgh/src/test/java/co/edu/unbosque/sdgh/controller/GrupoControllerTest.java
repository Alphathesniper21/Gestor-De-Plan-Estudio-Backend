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

import co.edu.unbosque.sdgh.dto.GrupoDTO;
import co.edu.unbosque.sdgh.exception.CupoExcedidoException;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.GrupoService;

/**
 * Pruebas unitarias para el controlador REST {@link GrupoController}.
 * <p>
 * Verifica los endpoints HTTP para la gestion de grupos: creacion con
 * validacion de cupo maximo institucional (201 CREATED, 400 BAD REQUEST),
 * consultas por id y generales, actualizacion y eliminacion.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see GrupoController
 * @see GrupoService
 */
class GrupoControllerTest {

	/** Mock del servicio de grupos. */
	@Mock
	private GrupoService grupoServ;

	/** Instancia del controlador bajo prueba. */
	private GrupoController controller;

	/** DTO de referencia para pruebas. */
	private GrupoDTO dto;

	/**
	 * Prepara los mocks e inyecta las dependencias en el controlador antes de cada
	 * prueba.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		controller = new GrupoController();

		try {
			var f1 = GrupoController.class.getDeclaredField("grupoServ");
			f1.setAccessible(true);
			f1.set(controller, grupoServ);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		dto = new GrupoDTO();
		dto.setId(1L);
		dto.setMateriaId(101L);
		dto.setNumeroGrupo(1);
		dto.setCupoPlaneado(40);
	}

	// ─── CREAR ────────────────────────────────────────────────────────────

	/**
	 * Verifica que el endpoint de crear grupo retorna HTTP 201 CREATED cuando los
	 * datos son validos.
	 */
	@Test
	void testCrear_Exitoso() {
		when(grupoServ.create(any(GrupoDTO.class))).thenReturn(0);

		ResponseEntity<String> response = controller.crear(101L, 1, 40);

		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("Grupo creado exitosamente", response.getBody());
	}

	/**
	 * Verifica que el endpoint de crear grupo retorna HTTP 400 BAD REQUEST cuando
	 * el cupo supera el limite institucional permitido.
	 */
	@Test
	void testCrear_RetornaBadRequestPorCupoExcedido() {
		when(grupoServ.create(any(GrupoDTO.class)))
				.thenThrow(new CupoExcedidoException("Cupo supera limite"));

		ResponseEntity<String> response = controller.crear(101L, 1, 45);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Cupo supera limite"));
	}

	// ─── CONSULTAS ────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code obtenerTodo} retorna HTTP 202 ACCEPTED con los grupos
	 * cuando hay datos.
	 */
	@Test
	void testObtenerTodo_Exitoso() {
		when(grupoServ.getAll()).thenReturn(List.of(dto));

		ResponseEntity<List<GrupoDTO>> response = controller.obtenerTodo();

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1, response.getBody().size());
	}

	/**
	 * Verifica que {@code obtenerTodo} retorna HTTP 204 NO CONTENT cuando no hay
	 * grupos.
	 */
	@Test
	void testObtenerTodo_RetornaNoContent() {
		when(grupoServ.getAll()).thenReturn(Collections.emptyList());

		ResponseEntity<List<GrupoDTO>> response = controller.obtenerTodo();

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	/**
	 * Verifica que {@code obtenerPorId} retorna HTTP 202 ACCEPTED cuando el grupo
	 * existe.
	 */
	@Test
	void testObtenerPorId_Exitoso() {
		when(grupoServ.getById(1L)).thenReturn(dto);

		ResponseEntity<GrupoDTO> response = controller.obtenerPorId(1L);

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1, response.getBody().getNumeroGrupo());
	}

	/**
	 * Verifica que {@code obtenerPorId} retorna HTTP 404 NOT FOUND cuando el grupo
	 * no existe.
	 */
	@Test
	void testObtenerPorId_RetornaNotFound() {
		when(grupoServ.getById(999L)).thenThrow(new RecursoNoEncontradoException("No existe"));

		ResponseEntity<GrupoDTO> response = controller.obtenerPorId(999L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	// ─── ACTUALIZAR Y ELIMINAR ────────────────────────────────────────────

	/**
	 * Verifica que {@code actualizar} retorna HTTP 202 ACCEPTED cuando la
	 * actualizacion es exitosa.
	 */
	@Test
	void testActualizar_Exitoso() {
		when(grupoServ.updateById(eq(1L), any(GrupoDTO.class))).thenReturn(0);

		ResponseEntity<String> response = controller.actualizar(1L, 2, 35);

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertEquals("Grupo actualizado exitosamente", response.getBody());
	}

	/**
	 * Verifica que {@code eliminarPorId} retorna HTTP 202 ACCEPTED cuando el grupo
	 * es eliminado.
	 */
	@Test
	void testEliminarPorId_Exitoso() {
		when(grupoServ.deleteById(1L)).thenReturn(0);

		ResponseEntity<String> response = controller.eliminarPorId(1L);

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertEquals("Grupo eliminado exitosamente", response.getBody());
	}

	/**
	 * Verifica que {@code contar} y {@code existe} retornan respuestas exitosas
	 * HTTP 202 ACCEPTED.
	 */
	@Test
	void testContarYExiste_Exitoso() {
		when(grupoServ.count()).thenReturn(3L);
		when(grupoServ.existById(1L)).thenReturn(true);

		ResponseEntity<Long> responseConteo = controller.contar();
		assertEquals(HttpStatus.ACCEPTED, responseConteo.getStatusCode());
		assertEquals(3L, responseConteo.getBody());

		ResponseEntity<Boolean> responseExiste = controller.existe(1L);
		assertEquals(HttpStatus.ACCEPTED, responseExiste.getStatusCode());
		assertEquals(true, responseExiste.getBody());
	}
}
