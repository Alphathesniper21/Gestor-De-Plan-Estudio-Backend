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

import co.edu.unbosque.sdgh.dto.MateriaDTO;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.MateriaService;

/**
 * Pruebas unitarias para el controlador REST {@link MateriaController}.
 * <p>
 * Verifica el comportamiento de los endpoints HTTP para la gestion de materias:
 * creacion con validacion de codigos de estado (201 CREATED, 400 BAD REQUEST),
 * consultas por id, semestre y total, actualizacion y eliminacion.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see MateriaController
 * @see MateriaService
 */
class MateriaControllerTest {

	/** Mock del servicio de materias. */
	@Mock
	private MateriaService materiaService;

	/** Instancia del controlador bajo prueba. */
	private MateriaController controller;

	/** DTO de referencia para pruebas. */
	private MateriaDTO dto;

	/**
	 * Prepara los mocks e inyecta las dependencias en el controlador antes de cada
	 * prueba.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		controller = new MateriaController();

		try {
			var f1 = MateriaController.class.getDeclaredField("materiaService");
			f1.setAccessible(true);
			f1.set(controller, materiaService);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		dto = new MateriaDTO();
		dto.setId(101L);
		dto.setNombre("Programacion I");
		dto.setSemestre(2);
		dto.setNcreditos(3);
		dto.setHorasDeLaMateria(4);
		dto.setCorrelativa(false);
	}

	// ─── CREAR ────────────────────────────────────────────────────────────

	/**
	 * Verifica que el endpoint de crear materia retorna HTTP 201 CREATED cuando
	 * los datos son validos.
	 */
	@Test
	void testCrear_Exitoso() {
		when(materiaService.create(any(MateriaDTO.class))).thenReturn(0);

		ResponseEntity<String> response = controller.crear("Programacion I", 2, 3, 4, false);

		assertNotNull(response);
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("Materia creada exitosamente", response.getBody());
		verify(materiaService, times(1)).create(any(MateriaDTO.class));
	}

	/**
	 * Verifica que el endpoint de crear retorna HTTP 400 BAD REQUEST cuando el
	 * servicio lanza {@link DatosInvalidosException}.
	 */
	@Test
	void testCrear_RetornaBadRequestPorDatosInvalidos() {
		when(materiaService.create(any(MateriaDTO.class)))
				.thenThrow(new DatosInvalidosException("Semestre fuera de rango"));

		ResponseEntity<String> response = controller.crear("Invalida", 15, 3, 4, false);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Semestre fuera de rango"));
	}

	/**
	 * Verifica que el endpoint de crear retorna HTTP 400 BAD REQUEST cuando el
	 * servicio lanza {@link RecursoDuplicadoException}.
	 */
	@Test
	void testCrear_RetornaBadRequestPorNombreDuplicado() {
		when(materiaService.create(any(MateriaDTO.class)))
				.thenThrow(new RecursoDuplicadoException("Nombre ya existe"));

		ResponseEntity<String> response = controller.crear("Programacion I", 2, 3, 4, false);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("Nombre ya existe"));
	}

	// ─── CONSULTAS ────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code obtenerTodo} retorna HTTP 202 ACCEPTED con la lista de
	 * materias cuando hay registros.
	 */
	@Test
	void testObtenerTodo_ExitosoConDatos() {
		when(materiaService.getAll()).thenReturn(List.of(dto));

		ResponseEntity<List<MateriaDTO>> response = controller.obtenerTodo();

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1, response.getBody().size());
	}

	/**
	 * Verifica que {@code obtenerTodo} retorna HTTP 204 NO CONTENT cuando no hay
	 * materias registradas.
	 */
	@Test
	void testObtenerTodo_RetornaNoContent() {
		when(materiaService.getAll()).thenReturn(Collections.emptyList());

		ResponseEntity<List<MateriaDTO>> response = controller.obtenerTodo();

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	/**
	 * Verifica que {@code obtenerPorId} retorna HTTP 202 ACCEPTED con la materia
	 * cuando existe.
	 */
	@Test
	void testObtenerPorId_Exitoso() {
		when(materiaService.getById(101L)).thenReturn(dto);

		ResponseEntity<MateriaDTO> response = controller.obtenerPorId(101L);

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Programacion I", response.getBody().getNombre());
	}

	/**
	 * Verifica que {@code obtenerPorId} retorna HTTP 404 NOT FOUND cuando la
	 * materia no existe.
	 */
	@Test
	void testObtenerPorId_RetornaNotFound() {
		when(materiaService.getById(999L)).thenThrow(new RecursoNoEncontradoException("No existe"));

		ResponseEntity<MateriaDTO> response = controller.obtenerPorId(999L);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	// ─── ACTUALIZAR Y ELIMINAR ────────────────────────────────────────────

	/**
	 * Verifica que {@code actualizar} retorna HTTP 202 ACCEPTED cuando la
	 * actualizacion es exitosa.
	 */
	@Test
	void testActualizar_Exitoso() {
		when(materiaService.existById(101L)).thenReturn(true);
		when(materiaService.updateById(eq(101L), any(MateriaDTO.class))).thenReturn(0);

		ResponseEntity<String> response = controller.actualizar(101L, "Programacion I", 2, 3, 4, false);

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertEquals("Materia actualizada exitosamente", response.getBody());
	}

	/**
	 * Verifica que {@code eliminarPorCodigo} retorna HTTP 202 ACCEPTED cuando la
	 * eliminacion es exitosa.
	 */
	@Test
	void testEliminarPorCodigo_Exitoso() {
		when(materiaService.deleteById(101L)).thenReturn(0);

		ResponseEntity<String> response = controller.eliminarPorCodigo(101L);

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertEquals("Materia eliminada exitosamente", response.getBody());
	}

	/**
	 * Verifica que {@code contar} retorna HTTP 202 ACCEPTED con la cantidad total
	 * de materias.
	 */
	@Test
	void testContar_Exitoso() {
		when(materiaService.count()).thenReturn(6L);

		ResponseEntity<Long> response = controller.contar();

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertEquals(6L, response.getBody());
	}
}
