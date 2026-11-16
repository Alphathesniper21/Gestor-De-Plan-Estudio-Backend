package co.edu.unbosque.sdgh.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.sdgh.dto.ParametroDTO;
import co.edu.unbosque.sdgh.entity.ClaveParametro;
import co.edu.unbosque.sdgh.entity.Parametro;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.ParametroRepository;

/**
 * Pruebas unitarias para la clase {@link ParametroService}.
 * <p>
 * Verifica la gestión de los parámetros configurables del sistema (CU06, RF-9,
 * RNF-3): lectura con respaldo en valores predeterminados, cálculo dinámico del
 * cupo máximo (RF-2), actualización con validación de formatos y rangos para
 * franjas, pausa, cupo base, tolerancia y sesiones máximas, y control del
 * estado de publicación del horario (RF-12).
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see ParametroService
 * @see ClaveParametro
 */
class ParametroServiceTest {

	/**
	 * Mock del repositorio de parámetros.
	 */
	@Mock
	private ParametroRepository parametroRepo;

	/**
	 * Instancia del servicio bajo prueba.
	 */
	private ParametroService service;

	/**
	 * Inicializa los mocks e inyecta el repositorio en el servicio por reflexión
	 * antes de cada prueba.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		service = new ParametroService();
		ReflectionTestUtils.setField(service, "parametroRepo", parametroRepo);
	}

	// ─── LECTURA Y VALORES POR DEFECTO (RF-9, RNF-3) ──────────────────────────

	/**
	 * Verifica que {@code obtenerMapa} y {@code getAll} retornan los valores por
	 * defecto cuando la base de datos aún no tiene registros personalizados, y que
	 * {@code getAll} excluye el parámetro interno {@code horario.publicado}.
	 */
	@Test
	void testObtenerMapaYGetAll_ValoresPorDefecto() {
		when(parametroRepo.findAll()).thenReturn(Collections.emptyList());

		Map<String, String> mapa = service.obtenerMapa();
		List<ParametroDTO> editables = service.getAll();

		assertEquals("07:00", mapa.get(ClaveParametro.HORA_INICIO_JORNADA));
		assertEquals("40", mapa.get(ClaveParametro.CUPO_BASE));
		assertEquals(8, editables.size());
		assertEquals(LocalTime.of(7, 0), service.getHora(ClaveParametro.HORA_INICIO_JORNADA));
		assertEquals(4, service.getEntero(ClaveParametro.SESIONES_MAXIMAS));
		assertEquals(0.10, service.getDecimal(ClaveParametro.CUPO_TOLERANCIA), 1e-6);
	}

	/**
	 * Verifica el criterio de aceptación de RF-9 y RNF-3: al cambiar el cupo base
	 * a 30 con tolerancia de 10 %, {@code getCupoMaximo} calcula 33 sin necesidad
	 * de recompilar.
	 */
	@Test
	void testGetCupoMaximo_CambiarCupoBaseA30DejaMaximoEn33() {
		when(parametroRepo.findAll()).thenReturn(List.of(new Parametro(ClaveParametro.CUPO_BASE, "30")));

		assertEquals(33, service.getCupoMaximo());
	}

	// ─── ACTUALIZACIÓN Y VALIDACIÓN DE RANGOS (CU06, RF-9) ────────────────────

	/**
	 * Verifica que {@code updateByClave} actualiza correctamente un parámetro
	 * válido, lo persiste y marca el horario como no publicado.
	 */
	@Test
	void testUpdateByClave_ExitosoEnTodosLosTipos() {
		when(parametroRepo.findAll()).thenReturn(Collections.emptyList());
		when(parametroRepo.findById(anyString())).thenReturn(Optional.empty());

		assertEquals(0, service.updateByClave(ClaveParametro.CUPO_BASE, "30"));
		assertEquals(0, service.updateByClave(ClaveParametro.CUPO_TOLERANCIA, "0.15"));
		assertEquals(0, service.updateByClave(ClaveParametro.SESIONES_MAXIMAS, "5"));
		assertEquals(0, service.updateByClave(ClaveParametro.HORA_INICIO_JORNADA, "06:00"));

		verify(parametroRepo, atLeast(4)).save(any(Parametro.class));
	}

	/**
	 * Verifica que {@code updateByClave} rechaza valores fuera de rango o con
	 * formato inválido para horas, pausa, cupo base, tolerancia y sesiones
	 * máximas.
	 */
	@Test
	void testUpdateByClave_LanzaExcepcionValoresFueraDeRangoOFormatoInvalido() {
		when(parametroRepo.findAll()).thenReturn(Collections.emptyList());

		assertThrows(DatosInvalidosException.class, () -> service.updateByClave("", "40"));
		assertThrows(DatosInvalidosException.class, () -> service.updateByClave(ClaveParametro.CUPO_BASE, ""));
		assertThrows(DatosInvalidosException.class,
				() -> service.updateByClave(ClaveParametro.HORARIO_PUBLICADO, "true"));
		assertThrows(RecursoNoEncontradoException.class, () -> service.updateByClave("parametro.inexistente", "10"));

		// Horas inválidas o incoherentes
		assertThrows(DatosInvalidosException.class,
				() -> service.updateByClave(ClaveParametro.HORA_INICIO_JORNADA, "no-es-hora"));
		assertThrows(DatosInvalidosException.class,
				() -> service.updateByClave(ClaveParametro.HORA_INICIO_JORNADA, "14:00")); // Posterior a cierre sábado
		assertThrows(DatosInvalidosException.class,
				() -> service.updateByClave(ClaveParametro.PAUSA_INICIO, "14:00")); // Posterior a pausa.fin (13:00)

		// Cupo base fuera de 1..200
		assertThrows(DatosInvalidosException.class, () -> service.updateByClave(ClaveParametro.CUPO_BASE, "0"));
		assertThrows(DatosInvalidosException.class, () -> service.updateByClave(ClaveParametro.CUPO_BASE, "250"));
		assertThrows(DatosInvalidosException.class, () -> service.updateByClave(ClaveParametro.CUPO_BASE, "abc"));

		// Tolerancia fuera de 0..1
		assertThrows(DatosInvalidosException.class, () -> service.updateByClave(ClaveParametro.CUPO_TOLERANCIA, "-0.1"));
		assertThrows(DatosInvalidosException.class, () -> service.updateByClave(ClaveParametro.CUPO_TOLERANCIA, "1.5"));

		// Sesiones máximas fuera de 1..10
		assertThrows(DatosInvalidosException.class, () -> service.updateByClave(ClaveParametro.SESIONES_MAXIMAS, "0"));
		assertThrows(DatosInvalidosException.class, () -> service.updateByClave(ClaveParametro.SESIONES_MAXIMAS, "12"));
	}

	// ─── ESTADO DE PUBLICACIÓN (RF-12) ────────────────────────────────────────

	/**
	 * Verifica que {@code estaPublicado} y {@code marcarPublicado} gestionan el
	 * estado de publicación del horario en la tabla de parámetros.
	 */
	@Test
	void testEstaPublicadoYMarcarPublicado() {
		when(parametroRepo.findAll()).thenReturn(List.of(new Parametro(ClaveParametro.HORARIO_PUBLICADO, "true")));
		assertTrue(service.estaPublicado());

		when(parametroRepo.findById(ClaveParametro.HORARIO_PUBLICADO)).thenReturn(Optional.empty());
		service.marcarPublicado(true);
		verify(parametroRepo, times(1)).save(new Parametro(ClaveParametro.HORARIO_PUBLICADO, "true"));
	}
}
