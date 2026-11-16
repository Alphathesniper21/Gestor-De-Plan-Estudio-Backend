package co.edu.unbosque.sdgh.controller;

import static org.junit.jupiter.api.Assertions.*;
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

import co.edu.unbosque.sdgh.dto.GrupoDTO;
import co.edu.unbosque.sdgh.dto.SesionDTO;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.service.HorarioService;

/**
 * Pruebas unitarias para el controlador REST {@link HorarioController}.
 * <p>
 * Verifica los endpoints HTTP para revisar grupos con sesiones pendientes
 * (RF-11), publicar el horario (RF-12) y consultar las sesiones del horario
 * publicado o su estado actual (CU08).
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see HorarioController
 * @see HorarioService
 */
class HorarioControllerTest {

	/**
	 * Mock del servicio de horario.
	 */
	@Mock
	private HorarioService horarioServ;

	/**
	 * Instancia del controlador bajo prueba.
	 */
	private HorarioController controller;

	/**
	 * Inicializa los mocks e inyecta las dependencias en el controlador por
	 * reflexión antes de cada prueba.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		controller = new HorarioController();
		ReflectionTestUtils.setField(controller, "horarioServ", horarioServ);
	}

	/**
	 * Verifica que {@code obtenerPendientes} retorna HTTP 202 ACCEPTED cuando hay
	 * grupos incompletos y 204 NO CONTENT cuando todos están completos (RF-11).
	 */
	@Test
	void testObtenerPendientes_ConDatosYSinDatos() {
		GrupoDTO g = new GrupoDTO(101L, "Estructuras", 1, 40);
		when(horarioServ.getPendientes()).thenReturn(List.of(g));
		assertEquals(HttpStatus.ACCEPTED, controller.obtenerPendientes().getStatusCode());

		when(horarioServ.getPendientes()).thenReturn(Collections.emptyList());
		assertEquals(HttpStatus.NO_CONTENT, controller.obtenerPendientes().getStatusCode());
	}

	/**
	 * Verifica que {@code publicar}, {@code obtenerPublicado} y {@code estado}
	 * retornan las respuestas HTTP esperadas para RF-12.
	 */
	@Test
	void testPublicarObtenerPublicadoYEstado() {
		when(horarioServ.publicar()).thenReturn(0);
		assertEquals(HttpStatus.ACCEPTED, controller.publicar().getStatusCode());

		when(horarioServ.publicar()).thenThrow(new DatosInvalidosException("Hay pendientes"));
		assertEquals(HttpStatus.BAD_REQUEST, controller.publicar().getStatusCode());

		SesionDTO s = new SesionDTO(1L, 1, LocalTime.of(7, 0), LocalTime.of(9, 0));
		when(horarioServ.getSesionesPublicadas()).thenReturn(List.of(s));
		assertEquals(HttpStatus.ACCEPTED, controller.obtenerPublicado().getStatusCode());

		when(horarioServ.getSesionesPublicadas()).thenThrow(new DatosInvalidosException("No publicado"));
		assertEquals(HttpStatus.NOT_FOUND, controller.obtenerPublicado().getStatusCode());

		when(horarioServ.estaPublicado()).thenReturn(true);
		ResponseEntity<Boolean> estado = controller.estado();
		assertEquals(HttpStatus.ACCEPTED, estado.getStatusCode());
		assertTrue(estado.getBody());
	}
}
