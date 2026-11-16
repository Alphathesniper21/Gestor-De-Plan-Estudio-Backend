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

import co.edu.unbosque.sdgh.dto.MateriaDTO;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.LimiteSesionesExcedidoException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.MateriaService;

/**
 * Pruebas unitarias para el controlador REST {@link MateriaController}.
 * <p>
 * Verifica el comportamiento de los endpoints HTTP para la gestión de materias
 * (RF-1): creación con validación de códigos de estado (201 CREATED, 400 BAD
 * REQUEST, 409 CONFLICT), consultas por id, código, semestre y correlatividad,
 * actualización y eliminación por id y por código.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see MateriaController
 * @see MateriaService
 */
class MateriaControllerTest {

	/**
	 * Mock del servicio de materias.
	 */
	@Mock
	private MateriaService materiaService;

	/**
	 * Instancia del controlador bajo prueba.
	 */
	private MateriaController controller;

	/**
	 * DTO de referencia para las pruebas.
	 */
	private MateriaDTO dto;

	/**
	 * Inicializa los mocks e inyecta las dependencias en el controlador por
	 * reflexión antes de cada prueba.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		controller = new MateriaController();
		ReflectionTestUtils.setField(controller, "materiaService", materiaService);

		dto = new MateriaDTO();
		dto.setId(101L);
		dto.setCodigo(1001L);
		dto.setNombre("Programacion I");
		dto.setSemestre(2);
		dto.setNcreditos(3);
		dto.setHorasDeLaMateria(4);
		dto.setCorrelativa(false);
	}

	// ─── CREAR (RF-1) ─────────────────────────────────────────────────────────

	/**
	 * Verifica que el endpoint de crear materia retorna HTTP 201 CREATED cuando
	 * los datos son válidos.
	 */
	@Test
	void testCrear_Exitoso() {
		when(materiaService.create(any(MateriaDTO.class))).thenReturn(0);

		ResponseEntity<String> response = controller.crear(1001L, "Programacion I", 2, 3, 4, false);

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
				.thenThrow(new DatosInvalidosException("El semestre debe estar entre 1 y 9"));

		ResponseEntity<String> response = controller.crear(1001L, "Invalida", 15, 3, 4, false);

		assertNotNull(response);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertTrue(response.getBody().contains("El semestre debe estar entre 1 y 9"));
	}

	/**
	 * Verifica que el endpoint de crear retorna HTTP 409 CONFLICT cuando el
	 * servicio lanza {@link RecursoDuplicadoException}.
	 */
	@Test
	void testCrear_RetornaConflictPorDuplicado() {
		when(materiaService.create(any(MateriaDTO.class)))
				.thenThrow(new RecursoDuplicadoException("Ya existe una materia con el codigo 1001"));

		ResponseEntity<String> response = controller.crear(1001L, "Programacion I", 2, 3, 4, false);

		assertNotNull(response);
		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertTrue(response.getBody().contains("Ya existe una materia con el codigo 1001"));
	}

	// ─── CONSULTAS ────────────────────────────────────────────────────────────

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
	 * Verifica que {@code obtenerPorId} y {@code obtenerPorCodigo} retornan HTTP
	 * 202 ACCEPTED cuando la materia existe, y 404 NOT FOUND cuando no existe.
	 */
	@Test
	void testObtenerPorIdYPorCodigo_ExitosoYNotFound() {
		when(materiaService.getById(101L)).thenReturn(dto);
		when(materiaService.getByCodigo(1001L)).thenReturn(dto);

		ResponseEntity<MateriaDTO> porId = controller.obtenerPorId(101L);
		assertEquals(HttpStatus.ACCEPTED, porId.getStatusCode());
		assertEquals("Programacion I", porId.getBody().getNombre());

		ResponseEntity<MateriaDTO> porCodigo = controller.obtenerPorCodigo(1001L);
		assertEquals(HttpStatus.ACCEPTED, porCodigo.getStatusCode());
		assertEquals(1001L, porCodigo.getBody().getCodigo());

		when(materiaService.getById(999L)).thenThrow(new RecursoNoEncontradoException("No existe"));
		assertEquals(HttpStatus.NOT_FOUND, controller.obtenerPorId(999L).getStatusCode());
	}

	/**
	 * Verifica que {@code obtenerPorSemestre} retorna HTTP 202 ACCEPTED con las
	 * materias del semestre o 400 BAD REQUEST si el semestre está fuera de rango.
	 */
	@Test
	void testObtenerPorSemestre_ExitosoYBadRequest() {
		when(materiaService.getBySemestre(2)).thenReturn(List.of(dto));
		assertEquals(HttpStatus.ACCEPTED, controller.obtenerPorSemestre(2).getStatusCode());

		when(materiaService.getBySemestre(10)).thenThrow(new DatosInvalidosException("Fuera de rango"));
		assertEquals(HttpStatus.BAD_REQUEST, controller.obtenerPorSemestre(10).getStatusCode());
	}

	// ─── ACTUALIZAR Y ELIMINAR ────────────────────────────────────────────────

	/**
	 * Verifica que {@code actualizar} retorna HTTP 202 ACCEPTED cuando la
	 * actualización es exitosa y 409 CONFLICT cuando excede el límite de sesiones.
	 */
	@Test
	void testActualizar_ExitosoYConflictoPorSesiones() {
		when(materiaService.existById(101L)).thenReturn(true);
		when(materiaService.updateById(eq(101L), any(MateriaDTO.class))).thenReturn(0);

		ResponseEntity<String> response = controller.actualizar(101L, 1001L, "Programacion I", 2, 3, 4, false);

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertEquals("Materia actualizada exitosamente", response.getBody());

		when(materiaService.updateById(eq(101L), any(MateriaDTO.class)))
				.thenThrow(new LimiteSesionesExcedidoException("Tiene mas sesiones asignadas"));
		ResponseEntity<String> conflicto = controller.actualizar(101L, 1001L, "Programacion I", 2, 3, 2, false);
		assertEquals(HttpStatus.CONFLICT, conflicto.getStatusCode());
	}

	/**
	 * Verifica que {@code eliminarPorCodigo} y {@code eliminarPorId} retornan HTTP
	 * 202 ACCEPTED cuando la eliminación es exitosa.
	 */
	@Test
	void testEliminarPorCodigoYPorId_Exitoso() {
		when(materiaService.deleteByCodigo(1001L)).thenReturn(0);
		when(materiaService.deleteById(101L)).thenReturn(0);

		ResponseEntity<String> respCodigo = controller.eliminarPorCodigo(1001L);
		assertEquals(HttpStatus.ACCEPTED, respCodigo.getStatusCode());
		assertEquals("Materia eliminada exitosamente", respCodigo.getBody());

		ResponseEntity<String> respId = controller.eliminarPorId(101L);
		assertEquals(HttpStatus.ACCEPTED, respId.getStatusCode());
		assertEquals("Materia eliminada exitosamente", respId.getBody());
	}

	/**
	 * Verifica que {@code contar} y {@code existe} retornan HTTP 202 ACCEPTED
	 * cuando existen registros.
	 */
	@Test
	void testContarYExiste_Exitoso() {
		when(materiaService.count()).thenReturn(6L);
		when(materiaService.existById(101L)).thenReturn(true);

		ResponseEntity<Long> response = controller.contar();
		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertEquals(6L, response.getBody());

		assertEquals(HttpStatus.ACCEPTED, controller.existe(101L).getStatusCode());
	}
}
