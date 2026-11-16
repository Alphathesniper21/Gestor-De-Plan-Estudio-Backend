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
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.sdgh.dto.SesionDTO;
import co.edu.unbosque.sdgh.entity.Grupo;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.entity.Sesion;
import co.edu.unbosque.sdgh.exception.CruceHorarioException;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.FranjaInstitucionalInvalidaException;
import co.edu.unbosque.sdgh.exception.LimiteSesionesExcedidoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.GrupoRepository;
import co.edu.unbosque.sdgh.repository.MateriaRepository;
import co.edu.unbosque.sdgh.repository.SesionRepository;

/**
 * Pruebas unitarias para la clase {@link SesionService}.
 * <p>
 * Verifica el núcleo de restricciones duras del prototipo de horarios (CU04,
 * CU05, CU07 y RNF-1): ubicación de sesiones de 2 horas (RF-3), franja
 * institucional permitida (RF-4), rechazo de la pausa del mediodía de 12:00 a
 * 13:00 (RF-5), regla de no cruce entre materias del mismo semestre en el mismo
 * bloque y dentro del mismo grupo (RF-6), límite de sesiones semanales
 * derivadas de las horas del curso (RF-7), movimiento y eliminación de sesiones
 * (RF-8), y consulta por semestre (RF-10).
 * </p>
 * <p>
 * Incluye más de 15 casos de prueba de rechazo sobre RF-4 a RF-7 para verificar
 * el cumplimiento del atributo de calidad de corrección (RNF-1).
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see SesionService
 * @see CRUDOperation
 */
class SesionServiceTest {

	/**
	 * Mock del repositorio de sesiones.
	 */
	@Mock
	private SesionRepository sesionRepo;

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
	private SesionService service;

	/**
	 * Entidad de grupo utilizada en los tests.
	 */
	private Grupo grupo;

	/**
	 * Entidad de materia utilizada en los tests.
	 */
	private Materia materia;

	/**
	 * DTO de sesión utilizado en los tests.
	 */
	private SesionDTO dto;

	/**
	 * Entidad de sesión utilizada en los tests.
	 */
	private Sesion sesion;

	/**
	 * Inicializa los mocks, inyecta las dependencias en el servicio por reflexión
	 * y prepara los objetos de prueba antes de cada caso.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mapper = new ModelMapper();
		service = new SesionService();
		ReflectionTestUtils.setField(service, "sesionRepo", sesionRepo);
		ReflectionTestUtils.setField(service, "grupoRepo", grupoRepo);
		ReflectionTestUtils.setField(service, "materiaRepo", materiaRepo);
		ReflectionTestUtils.setField(service, "parametroServ", parametroServ);
		ReflectionTestUtils.setField(service, "mapper", mapper);

		when(parametroServ.obtenerMapa()).thenReturn(new HashMap<>(ParametroService.VALORES_POR_DEFECTO));

		materia = new Materia();
		materia.setId(101L);
		materia.setCodigo(4001L);
		materia.setNombre("Estructuras de Datos");
		materia.setSemestre(4);
		materia.setHorasDeLaMateria(4); // 4 horas -> 2 sesiones de 2 horas

		grupo = new Grupo();
		grupo.setId(1L);
		grupo.setMateriaId(101L);
		grupo.setNumeroGrupo(1);
		grupo.setCupoPlaneado(40);

		dto = new SesionDTO();
		dto.setId(1L);
		dto.setGrupoId(1L);
		dto.setDiaSemana(1); // Lunes
		dto.setHoraInicio(LocalTime.of(7, 0));
		dto.setHoraFin(LocalTime.of(9, 0));

		sesion = new Sesion();
		sesion.setId(10L);
		sesion.setGrupoId(1L);
		sesion.setDiaSemana(1);
		sesion.setHoraInicio(LocalTime.of(7, 0));
		sesion.setHoraFin(LocalTime.of(9, 0));

		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
	}

	// ─── CASOS POSITIVOS DE CREACIÓN (RF-3, RF-4, RF-6) ───────────────────────

	/**
	 * Verifica el criterio de aceptación de RF-3 y RF-4: una sesión válida en
	 * franja permitida (lunes 07:00–09:00) es aceptada y guardada retornando 0.
	 */
	@Test
	void testCreate_ExitosoFranjaValidaLunes7a9() {
		when(sesionRepo.findByGrupoId(1L)).thenReturn(Collections.emptyList());
		when(grupoRepo.findByNumeroGrupo(1)).thenReturn(List.of(grupo));
		when(sesionRepo.save(any(Sesion.class))).thenReturn(sesion);

		int resultado = service.create(dto);

		assertEquals(0, resultado);
		verify(sesionRepo, times(1)).save(any(Sesion.class));
		verify(parametroServ, times(1)).marcarPublicado(false);
	}

	/**
	 * Verifica el criterio de aceptación de RF-6: dos cursos de 4.º semestre en
	 * bloques distintos (grupo 01 y grupo 02) el lunes 07:00–09:00 se aceptan sin
	 * conflicto.
	 */
	@Test
	void testCreate_AceptaMismoSemestreEnDiferenteBloqueGrupo02() {
		grupo.setNumeroGrupo(2); // Grupo 02
		when(sesionRepo.findByGrupoId(1L)).thenReturn(Collections.emptyList());
		when(grupoRepo.findByNumeroGrupo(2)).thenReturn(List.of(grupo));
		when(sesionRepo.save(any(Sesion.class))).thenReturn(sesion);

		assertEquals(0, service.create(dto));
	}

	// ─── RNF-1: 16 CASOS DE PRUEBA QUE VIOLAN RF-4 A RF-7 (100 % RECHAZADOS) ──

	/**
	 * Caso RNF-1 #1 (RF-4): Lunes 21:00–23:00 se rechaza por superar el cierre de
	 * las 22:00 entre semana.
	 */
	@Test
	void testRechazo01_RF4_Lunes21a23SuperaCierreSemana() {
		dto.setDiaSemana(1);
		dto.setHoraInicio(LocalTime.of(21, 0));
		dto.setHoraFin(LocalTime.of(23, 0));

		FranjaInstitucionalInvalidaException ex = assertThrows(FranjaInstitucionalInvalidaException.class,
				() -> service.create(dto));
		assertTrue(ex.getMessage().contains("entre las 07:00 y las 22:00"));
	}

	/**
	 * Caso RNF-1 #2 (RF-4): Sábado 12:00–14:00 se rechaza por superar el cierre
	 * sabatino de las 13:00.
	 */
	@Test
	void testRechazo02_RF4_Sabado12a14SuperaCierreSabado() {
		dto.setDiaSemana(6);
		dto.setHoraInicio(LocalTime.of(12, 0));
		dto.setHoraFin(LocalTime.of(14, 0));

		FranjaInstitucionalInvalidaException ex = assertThrows(FranjaInstitucionalInvalidaException.class,
				() -> service.create(dto));
		assertTrue(ex.getMessage().contains("entre las 07:00 y las 13:00"));
	}

	/**
	 * Caso RNF-1 #3 (RF-4): Sesión antes de las 07:00 (05:00–07:00) se rechaza por
	 * iniciar antes de la apertura de la jornada.
	 */
	@Test
	void testRechazo03_RF4_AntesDeAperturaJornada05a07() {
		dto.setDiaSemana(2);
		dto.setHoraInicio(LocalTime.of(5, 0));
		dto.setHoraFin(LocalTime.of(7, 0));

		assertThrows(FranjaInstitucionalInvalidaException.class, () -> service.create(dto));
	}

	/**
	 * Caso RNF-1 #4 (RF-4): Sesión antes de las 07:00 (06:00–08:00) se rechaza.
	 */
	@Test
	void testRechazo04_RF4_AntesDeAperturaJornada06a08() {
		dto.setDiaSemana(3);
		dto.setHoraInicio(LocalTime.of(6, 0));
		dto.setHoraFin(LocalTime.of(8, 0));

		assertThrows(FranjaInstitucionalInvalidaException.class, () -> service.create(dto));
	}

	/**
	 * Caso RNF-1 #5 (RF-4): Sábado en la tarde (14:00–16:00) se rechaza.
	 */
	@Test
	void testRechazo05_RF4_SabadoTarde14a16() {
		dto.setDiaSemana(6);
		dto.setHoraInicio(LocalTime.of(14, 0));
		dto.setHoraFin(LocalTime.of(16, 0));

		assertThrows(FranjaInstitucionalInvalidaException.class, () -> service.create(dto));
	}

	/**
	 * Caso RNF-1 #6 (RF-4): Día 7 (domingo) se rechaza por estar fuera de lunes a
	 * sábado.
	 */
	@Test
	void testRechazo06_RF4_DiaDomingoInvalido() {
		dto.setDiaSemana(7);
		assertThrows(FranjaInstitucionalInvalidaException.class, () -> service.create(dto));
	}

	/**
	 * Caso RNF-1 #7 (RF-4): Día 0 se rechaza por estar fuera de lunes a sábado.
	 */
	@Test
	void testRechazo07_RF4_DiaCeroInvalido() {
		dto.setDiaSemana(0);
		assertThrows(FranjaInstitucionalInvalidaException.class, () -> service.create(dto));
	}

	/**
	 * Caso RNF-1 #8 (RF-3 / RF-4): Sesión de 1 hora (07:00–08:00) se rechaza por
	 * no durar exactamente 2 horas.
	 */
	@Test
	void testRechazo08_RF4_DuracionMenorADosHoras() {
		dto.setHoraInicio(LocalTime.of(7, 0));
		dto.setHoraFin(LocalTime.of(8, 0));

		assertThrows(FranjaInstitucionalInvalidaException.class, () -> service.create(dto));
	}

	/**
	 * Caso RNF-1 #9 (RF-3 / RF-4): Sesión de 3 horas (07:00–10:00) se rechaza por
	 * exceder las 2 horas.
	 */
	@Test
	void testRechazo09_RF4_DuracionMayorADosHoras() {
		dto.setHoraInicio(LocalTime.of(7, 0));
		dto.setHoraFin(LocalTime.of(10, 0));

		assertThrows(FranjaInstitucionalInvalidaException.class, () -> service.create(dto));
	}

	/**
	 * Caso RNF-1 #10 (RF-4): Hora de inicio posterior a la hora de fin
	 * (10:00–08:00) se rechaza.
	 */
	@Test
	void testRechazo10_RF4_HoraInicioPosteriorAHoraFin() {
		dto.setHoraInicio(LocalTime.of(10, 0));
		dto.setHoraFin(LocalTime.of(8, 0));

		assertThrows(FranjaInstitucionalInvalidaException.class, () -> service.create(dto));
	}

	/**
	 * Caso RNF-1 #11 (RF-5): Criterio de aceptación de RF-5 — una sesión de 11:00
	 * a 13:00 se rechaza porque invade la pausa del mediodía (12:00–13:00).
	 */
	@Test
	void testRechazo11_RF5_CruzaPausaMediodia11a13() {
		dto.setHoraInicio(LocalTime.of(11, 0));
		dto.setHoraFin(LocalTime.of(13, 0));

		FranjaInstitucionalInvalidaException ex = assertThrows(FranjaInstitucionalInvalidaException.class,
				() -> service.create(dto));
		assertTrue(ex.getMessage().contains("entre las 12:00 y las 13:00"));
	}

	/**
	 * Caso RNF-1 #12 (RF-5): Una sesión de 12:00 a 14:00 se rechaza porque inicia
	 * dentro de la pausa del mediodía (12:00–13:00).
	 */
	@Test
	void testRechazo12_RF5_CruzaPausaMediodia12a14() {
		dto.setHoraInicio(LocalTime.of(12, 0));
		dto.setHoraFin(LocalTime.of(14, 0));

		assertThrows(FranjaInstitucionalInvalidaException.class, () -> service.create(dto));
	}

	/**
	 * Caso RNF-1 #13 (RF-6): Criterio de aceptación de RF-6 — dos cursos de 4.º
	 * semestre, grupo 01, el lunes 07:00–09:00: el segundo se rechaza con
	 * {@link CruceHorarioException}.
	 */
	@Test
	void testRechazo13_RF6_CruceExactoMismoSemestreYBloque() {
		when(sesionRepo.findByGrupoId(1L)).thenReturn(Collections.emptyList());

		Grupo otroGrupo = new Grupo(202L, 1, 40);
		otroGrupo.setId(2L);
		Materia otraMateria = new Materia(4002L, "Bases de Datos", 4, 3, 4, true);
		otraMateria.setId(202L);
		Sesion sesionExistente = new Sesion(2L, 1, LocalTime.of(7, 0), LocalTime.of(9, 0));

		when(grupoRepo.findByNumeroGrupo(1)).thenReturn(List.of(grupo, otroGrupo));
		when(materiaRepo.findById(202L)).thenReturn(Optional.of(otraMateria));
		when(sesionRepo.findByGrupoId(2L)).thenReturn(List.of(sesionExistente));

		CruceHorarioException ex = assertThrows(CruceHorarioException.class, () -> service.create(dto));
		assertTrue(ex.getMessage().contains("La sesion se cruza con el curso Bases de Datos"));
	}

	/**
	 * Caso RNF-1 #14 (RF-6): Cruce parcial de 1 hora (08:00–10:00 contra
	 * 07:00–09:00) entre dos cursos del mismo semestre y bloque se rechaza.
	 */
	@Test
	void testRechazo14_RF6_CruceParcialMismoSemestreYBloque() {
		when(sesionRepo.findByGrupoId(1L)).thenReturn(Collections.emptyList());

		Grupo otroGrupo = new Grupo(202L, 1, 40);
		otroGrupo.setId(2L);
		Materia otraMateria = new Materia(4002L, "Bases de Datos", 4, 3, 4, true);
		otraMateria.setId(202L);
		Sesion sesionExistente = new Sesion(2L, 1, LocalTime.of(7, 0), LocalTime.of(9, 0));

		when(grupoRepo.findByNumeroGrupo(1)).thenReturn(List.of(grupo, otroGrupo));
		when(materiaRepo.findById(202L)).thenReturn(Optional.of(otraMateria));
		when(sesionRepo.findByGrupoId(2L)).thenReturn(List.of(sesionExistente));

		dto.setHoraInicio(LocalTime.of(8, 0));
		dto.setHoraFin(LocalTime.of(10, 0));

		assertThrows(CruceHorarioException.class, () -> service.create(dto));
	}

	/**
	 * Caso RNF-1 #15 (RF-6): Dos sesiones del mismo grupo en el mismo día y franja
	 * superpuesta se rechazan con {@link CruceHorarioException}.
	 */
	@Test
	void testRechazo15_RF6_CruceDentroDelMismoGrupo() {
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(sesion));

		assertThrows(CruceHorarioException.class, () -> service.create(dto));
	}

	/**
	 * Caso RNF-1 #16 (RF-7): Criterio de aceptación de RF-7 — un curso de 4 horas
	 * (2 sesiones) que ya tiene 2 sesiones registradas rechaza la tercera con
	 * {@link LimiteSesionesExcedidoException}.
	 */
	@Test
	void testRechazo16_RF7_ExcedeLimiteSesionesDelCurso() {
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(new Sesion(), new Sesion()));

		LimiteSesionesExcedidoException ex = assertThrows(LimiteSesionesExcedidoException.class,
				() -> service.create(dto));
		assertTrue(ex.getMessage().contains("ya tiene las 2 sesiones semanales"));
	}

	// ─── UPDATE BY ID Y DELETE BY ID (RF-8) ───────────────────────────────────

	/**
	 * Verifica que {@code updateById} (RF-8) revalida las reglas y permite mover
	 * una sesión a un día y hora válidos sin cruces.
	 */
	@Test
	void testUpdateById_Exitoso() {
		when(sesionRepo.findById((Long) 10L)).thenReturn(Optional.of(sesion));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(sesion));
		when(grupoRepo.findByNumeroGrupo(1)).thenReturn(List.of(grupo));
		when(sesionRepo.save(any(Sesion.class))).thenReturn(sesion);

		SesionDTO cambio = new SesionDTO();
		cambio.setDiaSemana(2); // Martes
		cambio.setHoraInicio(LocalTime.of(9, 0));
		cambio.setHoraFin(LocalTime.of(11, 0));

		int resultado = service.updateById(10L, cambio);

		assertEquals(0, resultado);
		verify(sesionRepo, times(1)).save(sesion);
		verify(parametroServ, times(1)).marcarPublicado(false);
	}

	/**
	 * Verifica el criterio de aceptación de RF-8: mover una sesión a una franja
	 * inválida o con cruce se rechaza y no persiste el cambio.
	 */
	@Test
	void testUpdateById_RechazaMovimientoInvalido() {
		when(sesionRepo.findById((Long) 10L)).thenReturn(Optional.of(sesion));

		SesionDTO cambio = new SesionDTO();
		cambio.setDiaSemana(2);
		cambio.setHoraInicio(LocalTime.of(11, 0));
		cambio.setHoraFin(LocalTime.of(13, 0)); // Invade pausa

		assertThrows(FranjaInstitucionalInvalidaException.class, () -> service.updateById(10L, cambio));
		verify(sesionRepo, never()).save(any());
	}

	/**
	 * Verifica que {@code deleteById} (RF-8) elimina la sesión existente y marca
	 * el horario como no publicado.
	 */
	@Test
	void testDeleteById_ExitosoYNoEncontrado() {
		when(sesionRepo.findById((Long) 10L)).thenReturn(Optional.of(sesion));
		assertEquals(0, service.deleteById(10L));
		verify(sesionRepo, times(1)).delete(sesion);
		verify(parametroServ, times(1)).marcarPublicado(false);

		when(sesionRepo.findById((Long) 999L)).thenReturn(Optional.empty());
		assertThrows(RecursoNoEncontradoException.class, () -> service.deleteById(999L));
	}

	// ─── CONSULTA POR SEMESTRE (RF-10), POR GRUPO Y ADVERTENCIAS ──────────────

	/**
	 * Verifica el criterio de aceptación de RF-10: {@code getBySemestre} retorna
	 * únicamente las sesiones de las materias pertenecientes al semestre filtrado.
	 */
	@Test
	void testGetBySemestre_FiltraPorSemestreCorrectamente() {
		when(materiaRepo.findBySemestre(4)).thenReturn(List.of(materia));
		when(grupoRepo.findByMateriaId(101L)).thenReturn(List.of(grupo));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(sesion));

		List<SesionDTO> resultado = service.getBySemestre(4);

		assertNotNull(resultado);
		assertEquals(1, resultado.size());
		assertEquals(10L, resultado.get(0).getId());
		assertThrows(DatosInvalidosException.class, () -> service.getBySemestre(0));
		assertThrows(DatosInvalidosException.class, () -> service.getBySemestre(10));
	}

	/**
	 * Verifica que {@code obtenerAdvertencias} detecta sesiones ubicadas en días
	 * consecutivos.
	 */
	@Test
	void testObtenerAdvertencias_DetectaDiasConsecutivos() {
		Sesion sLunes = new Sesion(1L, 1, LocalTime.of(7, 0), LocalTime.of(9, 0));
		Sesion sMartes = new Sesion(1L, 2, LocalTime.of(7, 0), LocalTime.of(9, 0));

		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(sLunes, sMartes));

		List<String> advertencias = service.obtenerAdvertencias(1L);

		assertNotNull(advertencias);
		assertFalse(advertencias.isEmpty());
		assertTrue(advertencias.get(0).contains("ADVERTENCIA - El grupo 1 tiene sesiones en dias consecutivos"));
	}

	/**
	 * Verifica las operaciones {@code getAll}, {@code getById},
	 * {@code getByGrupoId}, {@code count} y {@code existById}.
	 */
	@Test
	void testConsultasGeneralesConteoYExistencia() {
		when(sesionRepo.findAll()).thenReturn(List.of(sesion));
		when(sesionRepo.findById((Long) 10L)).thenReturn(Optional.of(sesion));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(sesion));
		when(sesionRepo.count()).thenReturn(15L);
		when(sesionRepo.existsById(10L)).thenReturn(true);

		assertEquals(1, service.getAll().size());
		assertEquals(10L, service.getById(10L).getId());
		assertEquals(1, service.getByGrupoId(1L).size());
		assertEquals(15L, service.count());
		assertTrue(service.existById(10L));
		assertFalse(service.existById(null));
	}
}
