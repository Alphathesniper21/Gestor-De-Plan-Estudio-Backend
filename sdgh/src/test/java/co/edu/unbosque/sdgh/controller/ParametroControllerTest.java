package co.edu.unbosque.sdgh.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.sdgh.dto.ParametroDTO;
import co.edu.unbosque.sdgh.entity.ClaveParametro;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.ParametroService;

/**
 * Pruebas unitarias para el controlador REST {@link ParametroController}.
 * <p>
 * Verifica los endpoints HTTP para consultar y actualizar los parámetros
 * configurables del sistema (CU06, RF-9, RNF-3).
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see ParametroController
 * @see ParametroService
 */
class ParametroControllerTest {

	/**
	 * Mock del servicio de parámetros.
	 */
	@Mock
	private ParametroService parametroServ;

	/**
	 * Instancia del controlador bajo prueba.
	 */
	private ParametroController controller;

	/**
	 * Inicializa los mocks e inyecta las dependencias en el controlador por
	 * reflexión antes de cada prueba.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		controller = new ParametroController();
		ReflectionTestUtils.setField(controller, "parametroServ", parametroServ);
	}

	/**
	 * Verifica que {@code obtenerTodo} retorna HTTP 202 ACCEPTED con la lista de
	 * parámetros editables.
	 */
	@Test
	void testObtenerTodo_Exitoso() {
		when(parametroServ.getAll()).thenReturn(List.of(new ParametroDTO(ClaveParametro.CUPO_BASE, "40")));

		ResponseEntity<List<ParametroDTO>> response = controller.obtenerTodo();

		assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
		assertEquals(1, response.getBody().size());
	}

	/**
	 * Verifica que {@code actualizar} retorna HTTP 202 ACCEPTED cuando el cambio
	 * es válido, 400 BAD REQUEST si el valor está fuera de rango y 404 NOT FOUND si
	 * la clave no existe.
	 */
	@Test
	void testActualizar_ExitosoYErrores() {
		when(parametroServ.updateByClave(ClaveParametro.CUPO_BASE, "30")).thenReturn(0);
		assertEquals(HttpStatus.ACCEPTED, controller.actualizar(ClaveParametro.CUPO_BASE, "30").getStatusCode());

		when(parametroServ.updateByClave(ClaveParametro.CUPO_BASE, "500"))
				.thenThrow(new DatosInvalidosException("Fuera de rango"));
		assertEquals(HttpStatus.BAD_REQUEST, controller.actualizar(ClaveParametro.CUPO_BASE, "500").getStatusCode());

		when(parametroServ.updateByClave("inexistente", "10"))
				.thenThrow(new RecursoNoEncontradoException("No existe"));
		assertEquals(HttpStatus.NOT_FOUND, controller.actualizar("inexistente", "10").getStatusCode());
	}
}
