package co.edu.unbosque.sdgh.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class SesionServiceTest {

	@Mock
	private SesionRepository sesionRepo;

	@Mock
	private GrupoRepository grupoRepo;

	@Mock
	private MateriaRepository materiaRepo;

	@Mock
	private ModelMapper mapper;

	@InjectMocks
	private SesionService sesionService;

	private Grupo grupoValido;
	private Materia materiaValida;
	private SesionDTO sesionDTOValida;
	private Sesion sesionEntidad;

	@BeforeEach
	void setUp() {
		materiaValida = new Materia();
		materiaValida.setId(101L);
		materiaValida.setNombre("Estructuras de Datos");
		materiaValida.setSemestre(4);
		materiaValida.setHorasDeLaMateria(4); // 4 horas = 2 sesiones semanales

		grupoValido = new Grupo();
		grupoValido.setId(1L);
		grupoValido.setMateriaId(101L);
		grupoValido.setNumeroGrupo(1);
		grupoValido.setCupoPlaneado(40);

		sesionDTOValida = new SesionDTO();
		sesionDTOValida.setId(1L);
		sesionDTOValida.setGrupoId(1);
		sesionDTOValida.setDiaSemana(1); // Lunes
		sesionDTOValida.setHoraInicio(LocalTime.of(7, 0));
		sesionDTOValida.setHoraFin(LocalTime.of(9, 0));

		sesionEntidad = new Sesion();
		sesionEntidad.setId(10L);
		sesionEntidad.setGrupoId(1);
		sesionEntidad.setDiaSemana(1);
		sesionEntidad.setHoraInicio(LocalTime.of(7, 0));
		sesionEntidad.setHoraFin(LocalTime.of(9, 0));
	}

	@Test
	@DisplayName("RF-3 y RF-4 Criterio de Aceptacion: Lunes 7:00 a 9:00 se acepta exitosamente")
	void debeCrearSesionExitosamenteEnFranjaValida() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupoValido));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materiaValida));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(new ArrayList<>());
		when(grupoRepo.findByNumeroGrupo(1)).thenReturn(List.of(grupoValido));
		when(mapper.map(sesionDTOValida, Sesion.class)).thenReturn(sesionEntidad);

		int resultado = sesionService.create(sesionDTOValida);

		assertEquals(0, resultado);
		verify(sesionRepo, times(1)).save(sesionEntidad);
	}

	@Test
	@DisplayName("RF-4 Criterio de Aceptacion: Lunes 21:00-23:00 se rechaza (pasa de las 22:00)")
	void debeRechazarSesionFueraDeFranjaLunesAViernes() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupoValido));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materiaValida));

		sesionDTOValida.setHoraInicio(LocalTime.of(21, 0));
		sesionDTOValida.setHoraFin(LocalTime.of(23, 0));

		assertThrows(FranjaInstitucionalInvalidaException.class, () -> sesionService.create(sesionDTOValida));
		verify(sesionRepo, never()).save(any());
	}

	@Test
	@DisplayName("RF-4 Criterio de Aceptacion: Sabado 12:00-14:00 se rechaza (pasa de las 13:00)")
	void debeRechazarSesionFueraDeFranjaSabado() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupoValido));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materiaValida));

		sesionDTOValida.setDiaSemana(6); // Sabado
		sesionDTOValida.setHoraInicio(LocalTime.of(12, 0));
		sesionDTOValida.setHoraFin(LocalTime.of(14, 0));

		assertThrows(FranjaInstitucionalInvalidaException.class, () -> sesionService.create(sesionDTOValida));
	}

	@Test
	@DisplayName("RF-4: Debe rechazar sesion que no dure exactamente 2 horas (120 min)")
	void debeRechazarDuracionDiferenteADosHoras() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupoValido));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materiaValida));

		sesionDTOValida.setHoraInicio(LocalTime.of(7, 0));
		sesionDTOValida.setHoraFin(LocalTime.of(8, 0)); // 1 hora

		assertThrows(FranjaInstitucionalInvalidaException.class, () -> sesionService.create(sesionDTOValida));
	}

	@Test
	@DisplayName("RF-4: Debe rechazar dias fuera de lunes (1) a sabado (6)")
	void debeRechazarDiaInvalido() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupoValido));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materiaValida));

		sesionDTOValida.setDiaSemana(7); // Domingo
		assertThrows(FranjaInstitucionalInvalidaException.class, () -> sesionService.create(sesionDTOValida));

		sesionDTOValida.setDiaSemana(0);
		assertThrows(FranjaInstitucionalInvalidaException.class, () -> sesionService.create(sesionDTOValida));
	}

	@Test
	@DisplayName("RF-5 Criterio de Aceptacion: Sesion 11:00-13:00 o 12:00-14:00 se rechaza por cruzar pausa (12:00-13:00)")
	void debeRechazarSesionQueTocaPausaMediodia() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupoValido));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materiaValida));

		sesionDTOValida.setHoraInicio(LocalTime.of(11, 0));
		sesionDTOValida.setHoraFin(LocalTime.of(13, 0));

		assertThrows(FranjaInstitucionalInvalidaException.class, () -> sesionService.create(sesionDTOValida));
	}

	@Test
	@DisplayName("RF-6 Criterio de Aceptacion: Dos cursos de 4.º semestre en mismo bloque se cruzan -> segundo se rechaza")
	void debeRechazarCruceEntreCursosMismoSemestreYBloque() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupoValido));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materiaValida));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(new ArrayList<>());

		// Otro grupo del mismo bloque (numeroGrupo = 1) en otra materia del 4.º semestre
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

		when(grupoRepo.findByNumeroGrupo(1)).thenReturn(List.of(grupoValido, otroGrupo));
		when(materiaRepo.findById(202L)).thenReturn(Optional.of(otraMateria));
		when(sesionRepo.findByGrupoId(2L)).thenReturn(List.of(sesionExistente));

		assertThrows(CruceHorarioException.class, () -> sesionService.create(sesionDTOValida));
		verify(sesionRepo, never()).save(any());
	}

	@Test
	@DisplayName("RF-7 Criterio de Aceptacion: Un curso de 4 horas acepta 2 sesiones y rechaza la 3.ª")
	void debeRechazarTerceraSesionCuandoElCursoSoloPermiteDos() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupoValido));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materiaValida));

		Sesion s1 = new Sesion();
		Sesion s2 = new Sesion();
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(s1, s2));

		assertThrows(LimiteSesionesExcedidoException.class, () -> sesionService.create(sesionDTOValida));
		verify(sesionRepo, never()).save(any());
	}

	@Test
	@DisplayName("RF-8: deleteById elimina la sesion existente")
	void debeEliminarSesionExitosamente() {
		when(sesionRepo.findById((Long) 10L)).thenReturn(Optional.of(sesionEntidad));

		int resultado = sesionService.deleteById(10L);

		assertEquals(0, resultado);
		verify(sesionRepo, times(1)).delete(sesionEntidad);
	}

	@Test
	@DisplayName("RF-8: deleteById lanza excepcion cuando la sesion no existe")
	void debeLanzarExcepcionAlEliminarSesionInexistente() {
		when(sesionRepo.findById((Long) 999L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> sesionService.deleteById(999L));
	}

	@Test
	@DisplayName("obtenerAdvertencias: Reporta advertencia si hay sesiones en dias consecutivos")
	void debeDetectarAdvertenciaPorDiasConsecutivos() {
		Sesion diaLunes = new Sesion();
		diaLunes.setDiaSemana(1); // Lunes

		Sesion diaMartes = new Sesion();
		diaMartes.setDiaSemana(2); // Martes

		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(diaLunes, diaMartes));

		List<String> advertencias = sesionService.obtenerAdvertencias(1L);

		assertNotNull(advertencias);
		assertFalse(advertencias.isEmpty());
		assertTrue(advertencias.get(0).contains("ADVERTENCIA RF8"));
	}

	@Test
	@DisplayName("count y existById: Deben verificar operaciones basicas de sesion")
	void debeContarYVerificarExistenciaSesion() {
		when(sesionRepo.count()).thenReturn(10L);
		assertEquals(10L, sesionService.count());

		when(sesionRepo.existsById(10L)).thenReturn(true);
		assertTrue(sesionService.existById(10L));
		assertFalse(sesionService.existById(null));
	}
}
