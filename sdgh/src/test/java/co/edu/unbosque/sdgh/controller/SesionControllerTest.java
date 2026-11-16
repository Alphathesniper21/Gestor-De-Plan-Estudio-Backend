package co.edu.unbosque.sdgh.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.sdgh.dto.SesionDTO;
import co.edu.unbosque.sdgh.exception.CruceHorarioException;
import co.edu.unbosque.sdgh.exception.FranjaInstitucionalInvalidaException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.SesionService;

/**
 * Pruebas unitarias para el controlador REST {@link SesionController}.
 * <p>
 * Verifica los endpoints HTTP para la gestión de sesiones en la grilla semanal
 * (RF-3, RF-8, RF-10): creación (201 CREATED, 400 BAD REQUEST, 404 NOT FOUND),
 * consultas generales, por grupo y por semestre, actualización y eliminación.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see SesionController
 * @see SesionService
 */
class SesionControllerTest {

	/**
	 * Mock del servicio de sesiones.
	 */
	@Mock
	private SesionService sesionServ;

	/**
	 * Instancia del controlador bajo prueba.
	 */
	private SesionController controller;

	/**
	 * DTO de referencia utilizado en las pruebas.
	 */
	private SesionDTO dto;

	/**
	 * Inicializa los mocks e inyecta las dependencias en el controlador por
	 * reflexión antes de cada prueba.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		controller = new SesionController();
		ReflectionTestUtils.setField(controller, "sesionServ", sesionServ);

		dto = new SesionDTO(1L, 1, LocalTime.of(7, 0), LocalTime.of(9, 0));
		dto.setId(10L);
	}

	// ─── CREAR (RF-3) ─────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code crear} retorna HTTP 201 CREATED cuando la sesión cumple
	 * todas las reglas institucionales.
	 */
	@Test
	void testCrear_Exitoso() {
		when(sesionServ.create(any(SesionDTO.class))).thenReturn(0);

		ResponseEntity<String> response = controller.crear(1L, 1, LocalTime.of(7, 0), LocalTime.of(9, 0));

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("Sesion creada exitosamente", response.getBody());
	}

	/**
	 * Verifica que {@code crear} retorna HTTP 400 BAD REQUEST ante una violación
	 * de franja o cruce, y 404 NOT FOUND si el grupo no existe.
	 */
	@Test
	void testCrear_RetornaBadRequestYNotFound() {
		when(sesionServ.create(any(SesionDTO.class)))
				.thenThrow(new FranjaInstitucionalInvalidaException("Fuera de franja"));
		assertEquals(HttpStatus.BAD_REQUEST,
				controller.crear(1L, 1, LocalTime.of(21, 0), LocalTime.of(23, 0)).getStatusCode());

		when(sesionServ.create(any(SesionDTO.class))).thenThrow(new RecursoNoEncontradoException("No existe grupo"));
		assertEquals(HttpStatus.NOT_FOUND,
				controller.crear(99L, 1, LocalTime.of(7, 0), LocalTime.of(9, 0)).getStatusCode());
	}

	// ─── CONSULTAS (RF-10) ────────────────────────────────────────────────────

	/**
	 * Verifica las consultas de todas las sesiones, por id, por grupo y por
	 * semestre (RF-10).
	 */
	@Test
	void testConsultasGeneralesYPorSemestre() {
		when(sesionServ.getAll()).thenReturn(List.of(dto));
		when(sesionServ.getById(10L)).thenReturn(dto);
		when(sesionServ.getByGrupoId(1L)).thenReturn(List.of(dto));
		when(sesionServ.getBySemestre(4)).thenReturn(List.of(dto));

		assertEquals(HttpStatus.ACCEPTED, controller.obtenerTodo().getStatusCode());
		assertEquals(HttpStatus.ACCEPTED, controller.obtenerPorId(10L).getStatusCode());
		assertEquals(HttpStatus.ACCEPTED, controller.obtenerPorGrupo(1L).getStatusCode());
		assertEquals(HttpStatus.ACCEPTED, controller.obtenerPorSemestre(4).getStatusCode());
	}

	// ─── ACTUALIZAR Y ELIMINAR (RF-8) ─────────────────────────────────────────

	/**
	 * Verifica que {@code actualizar} y {@code eliminarPorId} retornan HTTP 202
	 * ACCEPTED cuando son exitosos, o 400 BAD REQUEST ante un cruce de horario.
	 */
	@Test
	void testActualizarYEliminar_ExitosoYError() {
		when(sesionServ.updateById(eq(10L), any(SesionDTO.class))).thenReturn(0);
		assertEquals(HttpStatus.ACCEPTED,
				controller.actualizar(10L, 2, LocalTime.of(9, 0), LocalTime.of(11, 0)).getStatusCode());

		when(sesionServ.updateById(eq(10L), any(SesionDTO.class))).thenThrow(new CruceHorarioException("Cruce"));
		assertEquals(HttpStatus.BAD_REQUEST,
				controller.actualizar(10L, 2, LocalTime.of(9, 0), LocalTime.of(11, 0)).getStatusCode());

		when(sesionServ.deleteById(10L)).thenReturn(0);
		assertEquals(HttpStatus.ACCEPTED, controller.eliminarPorId(10L).getStatusCode());
	}
}
