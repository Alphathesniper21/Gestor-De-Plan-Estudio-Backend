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
 * Verifica el nucleo de restricciones duras del prototipo de horarios:
 * ubicacion de sesiones de 2 horas (RF-3), franja institucional permitida
 * (RF-4), rechazo de la pausa del mediodia de 12:00 a 13:00 (RF-5), regla de
 * no cruce entre materias del mismo semestre en el mismo bloque (RF-6), limite
 * de sesiones semanales derivadas de las horas del curso (RF-7), y actualizacion,
 * eliminacion y advertencias de dias consecutivos (RF-8).
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
	 * Mapper para la conversion entre entidades y DTOs.
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
	 * DTO de sesion utilizado en los tests.
	 */
	private SesionDTO dto;

	/**
	 * Entidad de sesion utilizada en los tests.
	 */
	private Sesion sesion;

	/**
	 * Inicializa los mocks, inyecta las dependencias en el servicio y prepara los
	 * objetos de prueba antes de cada caso.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mapper = new ModelMapper();
		service = new SesionService();

		try {
			var f1 = SesionService.class.getDeclaredField("sesionRepo");
			f1.setAccessible(true);
			f1.set(service, sesionRepo);

			var f2 = SesionService.class.getDeclaredField("grupoRepo");
			f2.setAccessible(true);
			f2.set(service, grupoRepo);

			var f3 = SesionService.class.getDeclaredField("materiaRepo");
			f3.setAccessible(true);
			f3.set(service, materiaRepo);

			var f4 = SesionService.class.getDeclaredField("mapper");
			f4.setAccessible(true);
			f4.set(service, mapper);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		materia = new Materia();
		materia.setId(101L);
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
		dto.setGrupoId(1);
		dto.setDiaSemana(1); // Lunes
		dto.setHoraInicio(LocalTime.of(7, 0));
		dto.setHoraFin(LocalTime.of(9, 0));

		sesion = new Sesion();
		sesion.setId(10L);
		sesion.setGrupoId(1);
		sesion.setDiaSemana(1);
		sesion.setHoraInicio(LocalTime.of(7, 0));
		sesion.setHoraFin(LocalTime.of(9, 0));
	}

	// ─── CREATE (RF-3 Y RESTRICCIONES DURAS RF-4 A RF-7) ──────────────────────────

	/**
	 * Verifica que una sesion valida en franja permitida (lunes 07:00–09:00) es
	 * aceptada y guardada correctamente retornando 0.
	 */
	@Test
	void testCreate_ExitosoFranjaValida() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(Collections.emptyList());
		when(grupoRepo.findByNumeroGrupo(1)).thenReturn(List.of(grupo));
		when(sesionRepo.save(any(Sesion.class))).thenReturn(sesion);

		int resultado = service.create(dto);

		assertEquals(0, resultado);
		verify(sesionRepo, times(1)).save(any(Sesion.class));
	}

	/**
	 * Verifica el criterio de aceptacion de RF-4: una sesion de lunes 21:00 a 23:00
	 * se rechaza lanzando {@link FranjaInstitucionalInvalidaException} porque supera
	 * el limite de las 22:00.
	 */
	@Test
	void testCreate_LanzaExcepcionFueraDeJornadaLunesAViernes() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));

		dto.setHoraInicio(LocalTime.of(21, 0));
		dto.setHoraFin(LocalTime.of(23, 0));

		FranjaInstitucionalInvalidaException ex = assertThrows(
				FranjaInstitucionalInvalidaException.class, () -> service.create(dto));

		assertTrue(ex.getMessage().contains("entre las 07:00 y las 22:00"));
		verify(sesionRepo, never()).save(any());
	}

	/**
	 * Verifica el criterio de aceptacion de RF-4: una sesion de sabado de 12:00 a
	 * 14:00 se rechaza lanzando {@link FranjaInstitucionalInvalidaException} porque
	 * supera el limite sabatino de las 13:00.
	 */
	@Test
	void testCreate_LanzaExcepcionFueraDeJornadaSabado() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));

		dto.setDiaSemana(6); // Sabado
		dto.setHoraInicio(LocalTime.of(12, 0));
		dto.setHoraFin(LocalTime.of(14, 0));

		FranjaInstitucionalInvalidaException ex = assertThrows(
				FranjaInstitucionalInvalidaException.class, () -> service.create(dto));

		assertTrue(ex.getMessage().contains("entre las 07:00 y las 13:00"));
		verify(sesionRepo, never()).save(any());
	}

	/**
	 * Verifica que una sesion con duracion diferente a 120 minutos (2 horas) se
	 * rechaza lanzando {@link FranjaInstitucionalInvalidaException}.
	 */
	@Test
	void testCreate_LanzaExcepcionDuracionInvalida() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));

		dto.setHoraInicio(LocalTime.of(7, 0));
		dto.setHoraFin(LocalTime.of(8, 0)); // 60 minutos

		FranjaInstitucionalInvalidaException ex = assertThrows(
				FranjaInstitucionalInvalidaException.class, () -> service.create(dto));

		assertTrue(ex.getMessage().contains("debe durar exactamente 2 horas"));
		verify(sesionRepo, never()).save(any());
	}

	/**
	 * Verifica que dias fuera del rango de lunes (1) a sabado (6) son rechazados
	 * lanzando {@link FranjaInstitucionalInvalidaException}.
	 */
	@Test
	void testCreate_LanzaExcepcionDiaInvalido() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));

		dto.setDiaSemana(7); // Domingo
		assertThrows(FranjaInstitucionalInvalidaException.class, () -> service.create(dto));

		dto.setDiaSemana(0);
		assertThrows(FranjaInstitucionalInvalidaException.class, () -> service.create(dto));
	}

	/**
	 * Verifica el criterio de aceptacion de RF-5: cualquier sesion que se cruce con
	 * la pausa del mediodia (12:00 a 13:00) es rechazada con
	 * {@link FranjaInstitucionalInvalidaException}.
	 */
	@Test
	void testCreate_LanzaExcepcionPausaMediodia() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));

		dto.setHoraInicio(LocalTime.of(11, 0));
		dto.setHoraFin(LocalTime.of(13, 0));

		FranjaInstitucionalInvalidaException ex = assertThrows(
				FranjaInstitucionalInvalidaException.class, () -> service.create(dto));

		assertTrue(ex.getMessage().contains("entre las 12:00 y las 13:00"));
		verify(sesionRepo, never()).save(any());
	}

	/**
	 * Verifica el criterio de aceptacion de RF-6: dos materias del 4.º semestre en
	 * el grupo 01 (mismo bloque) programadas a la misma hora se rechazan con
	 * {@link CruceHorarioException}.
	 */
	@Test
	void testCreate_LanzaExcepcionCruceHorarioMismoSemestreYBloque() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(Collections.emptyList());

		// Segundo grupo del mismo bloque (numeroGrupo = 1) en otra materia de 4.° semestre
		Grupo otroGrupo = new Grupo();
		otroGrupo.setId(2L);
		otroGrupo.setMateriaId(202L);
		otroGrupo.setNumeroGrupo(1);

		Materia otraMateria = new Materia();
		otraMateria.setId(202L);
		otraMateria.setNombre("Bases de Datos");
		otraMateria.setSemestre(4);

		Sesion sesionExistente = new Sesion();
		sesionExistente.setId(20L);
		sesionExistente.setGrupoId(2);
		sesionExistente.setDiaSemana(1); // Lunes
		sesionExistente.setHoraInicio(LocalTime.of(7, 0));
		sesionExistente.setHoraFin(LocalTime.of(9, 0));

		when(grupoRepo.findByNumeroGrupo(1)).thenReturn(List.of(grupo, otroGrupo));
		when(materiaRepo.findById(202L)).thenReturn(Optional.of(otraMateria));
		when(sesionRepo.findByGrupoId(2L)).thenReturn(List.of(sesionExistente));

		CruceHorarioException ex = assertThrows(CruceHorarioException.class, () -> service.create(dto));

		assertTrue(ex.getMessage().contains("La sesion se cruza"));
		verify(sesionRepo, never()).save(any());
	}

	/**
	 * Verifica el criterio de aceptacion de RF-7: un curso de 4 horas (2 sesiones)
	 * que ya tiene sus 2 sesiones completas rechaza la 3.ª sesion lanzando
	 * {@link LimiteSesionesExcedidoException}.
	 */
	@Test
	void testCreate_LanzaExcepcionLimiteSesionesExcedido() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));

		Sesion s1 = new Sesion();
		Sesion s2 = new Sesion();
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(s1, s2));

		LimiteSesionesExcedidoException ex = assertThrows(
				LimiteSesionesExcedidoException.class, () -> service.create(dto));

		assertTrue(ex.getMessage().contains("ya tiene las 2 sesiones semanales"));
		verify(sesionRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link DatosInvalidosException} cuando los datos de la
	 * sesion son nulos o incompletos.
	 */
	@Test
	void testCreate_LanzaExcepcionDatosInvalidos() {
		assertThrows(DatosInvalidosException.class, () -> service.create(null));

		dto.setGrupoId(0);
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setGrupoId(1);
		dto.setHoraInicio(null);
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));
	}

	// ─── GET ALL ──────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code getAll} retorna todas las sesiones en formato DTO.
	 */
	@Test
	void testGetAll_Exitoso() {
		when(sesionRepo.findAll()).thenReturn(List.of(sesion));

		List<SesionDTO> lista = service.getAll();

		assertNotNull(lista);
		assertEquals(1, lista.size());
	}

	// ─── UPDATE BY ID (RF-8) ──────────────────────────────────────────────

	/**
	 * Verifica que {@code updateById} revalida las reglas y permite mover una
	 * sesion a un dia u hora validos sin cruces.
	 */
	@Test
	void testUpdateById_Exitoso() {
		when(sesionRepo.findById((Long) 10L)).thenReturn(Optional.of(sesion));
		when(grupoRepo.findById(10L)).thenReturn(Optional.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(sesion));
		when(grupoRepo.findByNumeroGrupo(1)).thenReturn(List.of(grupo));
		when(sesionRepo.save(any(Sesion.class))).thenReturn(sesion);

		SesionDTO cambio = new SesionDTO();
		cambio.setDiaSemana(2); // Mover a Martes
		cambio.setHoraInicio(LocalTime.of(9, 0));
		cambio.setHoraFin(LocalTime.of(11, 0));

		int resultado = service.updateById(10L, cambio);

		assertEquals(0, resultado);
		verify(sesionRepo, times(1)).save(sesion);
	}

	/**
	 * Verifica que {@code updateById} rechaza el movimiento si la nueva posicion
	 * toca la pausa del mediodia.
	 */
	@Test
	void testUpdateById_LanzaExcepcionPausaMediodia() {
		when(sesionRepo.findById((Long) 10L)).thenReturn(Optional.of(sesion));
		when(grupoRepo.findById(10L)).thenReturn(Optional.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));

		SesionDTO cambio = new SesionDTO();
		cambio.setDiaSemana(2);
		cambio.setHoraInicio(LocalTime.of(11, 0));
		cambio.setHoraFin(LocalTime.of(13, 0));

		assertThrows(FranjaInstitucionalInvalidaException.class, () -> service.updateById(10L, cambio));
	}

	// ─── DELETE BY ID (RF-8) ──────────────────────────────────────────────

	/**
	 * Verifica que {@code deleteById} elimina la sesion existente de la base de
	 * datos y retorna 0.
	 */
	@Test
	void testDeleteById_Exitoso() {
		when(sesionRepo.findById((Long) 10L)).thenReturn(Optional.of(sesion));

		int resultado = service.deleteById(10L);

		assertEquals(0, resultado);
		verify(sesionRepo, times(1)).delete(sesion);
	}

	/**
	 * Verifica que {@code deleteById} lanza {@link RecursoNoEncontradoException}
	 * cuando la sesion a eliminar no existe.
	 */
	@Test
	void testDeleteById_LanzaExcepcionNoEncontrado() {
		when(sesionRepo.findById((Long) 999L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> service.deleteById(999L));
	}

	// ─── ADVERTENCIAS POR DIAS CONSECUTIVOS ────────────────────────────────

	/**
	 * Verifica que {@code obtenerAdvertencias} genera el mensaje de advertencia si
	 * el grupo tiene sesiones programadas en dias consecutivos (ej. lunes y
	 * martes).
	 */
	@Test
	void testObtenerAdvertencias_DetectaDiasConsecutivos() {
		Sesion sLunes = new Sesion();
		sLunes.setDiaSemana(1);

		Sesion sMartes = new Sesion();
		sMartes.setDiaSemana(2);

		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(sLunes, sMartes));

		List<String> advertencias = service.obtenerAdvertencias(1L);

		assertNotNull(advertencias);
		assertFalse(advertencias.isEmpty());
		assertTrue(advertencias.get(0).contains("ADVERTENCIA RF8"));
	}

	// ─── OPERACIONES DE CONTEO Y EXISTENCIA ────────────────────────────────

	/**
	 * Verifica que {@code count} retorna la cantidad exacta de sesiones.
	 */
	@Test
	void testCount_Exitoso() {
		when(sesionRepo.count()).thenReturn(15L);

		assertEquals(15L, service.count());
	}

	/**
	 * Verifica que {@code existById} devuelve true cuando la sesion existe y false
	 * en caso contrario.
	 */
	@Test
	void testExistById_Exitoso() {
		when(sesionRepo.existsById(10L)).thenReturn(true);
		when(sesionRepo.existsById(999L)).thenReturn(false);

		assertTrue(service.existById(10L));
		assertFalse(service.existById(999L));
		assertFalse(service.existById(null));
	}
}
