package co.edu.unbosque.sdgh.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.sdgh.dto.GrupoDTO;
import co.edu.unbosque.sdgh.dto.SesionDTO;
import co.edu.unbosque.sdgh.entity.Grupo;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.entity.Sesion;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.repository.GrupoRepository;
import co.edu.unbosque.sdgh.repository.MateriaRepository;
import co.edu.unbosque.sdgh.repository.SesionRepository;

/**
 * Pruebas unitarias para la clase {@link HorarioService}.
 * <p>
 * Verifica la gestión global del horario (CU08): la consulta de grupos con
 * sesiones pendientes antes de publicar (RF-11) y la publicación del horario
 * (RF-12) validando que no existan pendientes, que haya sesiones registradas y
 * que todas cumplan la franja y pausa vigentes.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see HorarioService
 */
class HorarioServiceTest {

	/**
	 * Mock del repositorio de grupos.
	 */
	@Mock
	private GrupoRepository grupoRepo;

	/**
	 * Mock del repositorio de materias.
	 */
	@Mock
	private MateriaRepository materiaRepo;

	/**
	 * Mock del repositorio de sesiones.
	 */
	@Mock
	private SesionRepository sesionRepo;

	/**
	 * Mock del servicio de parámetros configurables.
	 */
	@Mock
	private ParametroService parametroServ;

	/**
	 * Mapper para la conversión entre entidades y DTOs.
	 */
	private ModelMapper mapper;

	/**
	 * Instancia del servicio bajo prueba.
	 */
	private HorarioService service;

	/**
	 * Entidades de referencia para las pruebas.
	 */
	private Materia materia;
	private Grupo grupo;
	private Sesion s1;
	private Sesion s2;

	/**
	 * Inicializa los mocks e inyecta las dependencias en el servicio por reflexión
	 * antes de cada caso.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mapper = new ModelMapper();
		service = new HorarioService();
		ReflectionTestUtils.setField(service, "grupoRepo", grupoRepo);
		ReflectionTestUtils.setField(service, "materiaRepo", materiaRepo);
		ReflectionTestUtils.setField(service, "sesionRepo", sesionRepo);
		ReflectionTestUtils.setField(service, "parametroServ", parametroServ);
		ReflectionTestUtils.setField(service, "mapper", mapper);

		when(parametroServ.obtenerMapa()).thenReturn(new HashMap<>(ParametroService.VALORES_POR_DEFECTO));

		materia = new Materia(1001L, "Estructuras de Datos", 4, 3, 4, false); // 2 sesiones
		materia.setId(101L);

		grupo = new Grupo(101L, 1, 40);
		grupo.setId(1L);

		s1 = new Sesion(1L, 1, LocalTime.of(7, 0), LocalTime.of(9, 0));
		s1.setId(10L);
		s2 = new Sesion(1L, 3, LocalTime.of(7, 0), LocalTime.of(9, 0));
		s2.setId(11L);
	}

	// ─── GRUPOS PENDIENTES (RF-11) ────────────────────────────────────────────

	/**
	 * Verifica el criterio de aceptación de RF-11: un grupo con 1 de 2 sesiones
	 * asignadas aparece en la lista de grupos pendientes.
	 */
	@Test
	void testGetPendientes_GrupoConUnaDeDosSesionesAparecePendiente() {
		when(grupoRepo.findAll()).thenReturn(List.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(s1)); // 1 de 2 sesiones

		List<GrupoDTO> pendientes = service.getPendientes();

		assertEquals(1, pendientes.size());
		assertEquals(1L, pendientes.get(0).getId());
	}

	// ─── PUBLICACIÓN DEL HORARIO (RF-12) ──────────────────────────────────────

	/**
	 * Verifica el criterio de aceptación de RF-12: cuando todos los grupos están
	 * completos y dentro de la franja vigente, {@code publicar} retorna 0 y marca
	 * el horario como publicado.
	 */
	@Test
	void testPublicar_ExitosoCuandoTodosLosGruposEstanCompletos() {
		when(grupoRepo.findAll()).thenReturn(List.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(s1, s2)); // 2 de 2 sesiones
		when(sesionRepo.count()).thenReturn(2L);
		when(sesionRepo.findAll()).thenReturn(List.of(s1, s2));

		int resultado = service.publicar();

		assertEquals(0, resultado);
		verify(parametroServ, times(1)).marcarPublicado(true);
	}

	/**
	 * Verifica que {@code publicar} lanza {@link DatosInvalidosException} si hay
	 * grupos con sesiones pendientes, si no hay sesiones registradas o si alguna
	 * sesión quedó fuera del horario configurado.
	 */
	@Test
	void testPublicar_LanzaExcepcionCuandoHayPendientesOSinSesionesOFueraDeHorario() {
		// 1. Con grupos pendientes
		when(grupoRepo.findAll()).thenReturn(List.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(s1));
		assertThrows(DatosInvalidosException.class, () -> service.publicar());

		// 2. Sin sesiones registradas
		when(grupoRepo.findAll()).thenReturn(Collections.emptyList());
		when(sesionRepo.count()).thenReturn(0L);
		assertThrows(DatosInvalidosException.class, () -> service.publicar());

		// 3. Con una sesión que cruza la pausa vigente (11:00 - 13:00)
		Sesion sEnPausa = new Sesion(1L, 1, LocalTime.of(11, 0), LocalTime.of(13, 0));
		when(sesionRepo.count()).thenReturn(1L);
		when(sesionRepo.findAll()).thenReturn(List.of(sEnPausa));
		assertThrows(DatosInvalidosException.class, () -> service.publicar());
	}

	/**
	 * Verifica que {@code getSesionesPublicadas} retorna las sesiones cuando el
	 * horario está publicado o lanza {@link DatosInvalidosException} cuando no lo
	 * está.
	 */
	@Test
	void testGetSesionesPublicadasYEstado() {
		when(parametroServ.estaPublicado()).thenReturn(true);
		when(sesionRepo.findAll()).thenReturn(List.of(s1, s2));

		assertTrue(service.estaPublicado());
		List<SesionDTO> publicadas = service.getSesionesPublicadas();
		assertEquals(2, publicadas.size());

		when(parametroServ.estaPublicado()).thenReturn(false);
		assertThrows(DatosInvalidosException.class, () -> service.getSesionesPublicadas());
	}
}
