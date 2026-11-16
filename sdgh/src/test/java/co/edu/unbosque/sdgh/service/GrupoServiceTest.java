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

import co.edu.unbosque.sdgh.dto.GrupoDTO;
import co.edu.unbosque.sdgh.entity.Grupo;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.entity.Sesion;
import co.edu.unbosque.sdgh.exception.CruceHorarioException;
import co.edu.unbosque.sdgh.exception.CupoExcedidoException;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.GrupoRepository;
import co.edu.unbosque.sdgh.repository.MateriaRepository;
import co.edu.unbosque.sdgh.repository.SesionRepository;

/**
 * Pruebas unitarias para la clase {@link GrupoService}.
 * <p>
 * Verifica la lógica de negocio de los grupos de materias (CU03 - RF-2),
 * incluyendo la creación con validación de cupo máximo de 40 más 10 % de
 * tolerancia (44 estudiantes), unicidad de grupo por curso, eliminación en
 * cascada de sesiones vinculadas, verificación de cruces de horario al cambiar
 * de bloque y operaciones CRUD.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see GrupoService
 * @see CRUDOperation
 */
class GrupoServiceTest {

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
	private GrupoService service;

	/**
	 * Entidad de grupo de referencia utilizada en los tests.
	 */
	private Grupo grupo;

	/**
	 * DTO de grupo de referencia utilizado en los tests.
	 */
	private GrupoDTO dto;

	/**
	 * Inicializa los mocks, inyecta las dependencias en el servicio por reflexión
	 * y prepara los objetos de prueba antes de cada caso.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mapper = new ModelMapper();
		service = new GrupoService();
		ReflectionTestUtils.setField(service, "grupoRepo", grupoRepo);
		ReflectionTestUtils.setField(service, "materiaRepo", materiaRepo);
		ReflectionTestUtils.setField(service, "sesionRepo", sesionRepo);
		ReflectionTestUtils.setField(service, "parametroServ", parametroServ);
		ReflectionTestUtils.setField(service, "mapper", mapper);

		// Por defecto: cupo base 40 + 10 % tolerancia = 44 (RF-2)
		when(parametroServ.getCupoMaximo()).thenReturn(44);

		grupo = new Grupo();
		grupo.setId(1L);
		grupo.setMateriaId(101L);
		grupo.setNumeroGrupo(1);
		grupo.setCupoPlaneado(40);

		dto = new GrupoDTO();
		dto.setId(1L);
		dto.setMateriaId(101L);
		dto.setNumeroGrupo(1);
		dto.setCupoPlaneado(40);
	}

	// ─── CREATE (RF-2) ────────────────────────────────────────────────────────

	/**
	 * Verifica que la creación de un grupo retorna 0 cuando los datos son válidos,
	 * la materia existe y el cupo planeado es de 40 estudiantes.
	 */
	@Test
	void testCreate_ExitosoCupoBase() {
		when(materiaRepo.existsById(101L)).thenReturn(true);
		when(grupoRepo.existsByMateriaIdAndNumeroGrupo(101L, 1)).thenReturn(false);
		when(grupoRepo.save(any(Grupo.class))).thenReturn(grupo);

		int resultado = service.create(dto);

		assertEquals(0, resultado);
		verify(grupoRepo, times(1)).save(any(Grupo.class));
		verify(parametroServ, times(1)).marcarPublicado(false);
	}

	/**
	 * Verifica el criterio de aceptación de RF-2: un cupo de 44 estudiantes se
	 * acepta exitosamente (40 más el 10 % de tolerancia).
	 */
	@Test
	void testCreate_AceptaCupoMaximoConTolerancia44() {
		dto.setCupoPlaneado(44);

		when(materiaRepo.existsById(101L)).thenReturn(true);
		when(grupoRepo.existsByMateriaIdAndNumeroGrupo(101L, 1)).thenReturn(false);
		when(grupoRepo.save(any(Grupo.class))).thenReturn(grupo);

		int resultado = service.create(dto);

		assertEquals(0, resultado);
		verify(grupoRepo, times(1)).save(any(Grupo.class));
	}

	/**
	 * Verifica el criterio de aceptación de RF-2: un cupo de 45 estudiantes se
	 * rechaza lanzando {@link CupoExcedidoException}.
	 */
	@Test
	void testCreate_LanzaExcepcionCupoExcedido45() {
		dto.setCupoPlaneado(45);

		CupoExcedidoException ex = assertThrows(CupoExcedidoException.class, () -> service.create(dto));

		assertTrue(ex.getMessage().contains("supera el limite institucional"));
		verify(grupoRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link RecursoNoEncontradoException} cuando la materia
	 * a la que se asocia el grupo no existe en la base de datos.
	 */
	@Test
	void testCreate_LanzaExcepcionMateriaInexistente() {
		when(materiaRepo.existsById(101L)).thenReturn(false);

		RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class, () -> service.create(dto));

		assertEquals("No existe una materia con el codigo 101", ex.getMessage());
		verify(grupoRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link RecursoDuplicadoException} cuando la materia ya
	 * tiene asignado un grupo con ese mismo número.
	 */
	@Test
	void testCreate_LanzaExcepcionNumeroGrupoDuplicado() {
		when(materiaRepo.existsById(101L)).thenReturn(true);
		when(grupoRepo.existsByMateriaIdAndNumeroGrupo(101L, 1)).thenReturn(true);

		RecursoDuplicadoException ex = assertThrows(RecursoDuplicadoException.class, () -> service.create(dto));

		assertTrue(ex.getMessage().contains("ya tiene el grupo 1"));
		verify(grupoRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link DatosInvalidosException} cuando los datos son
	 * nulos o los identificadores son negativos o cero.
	 */
	@Test
	void testCreate_LanzaExcepcionDatosInvalidos() {
		assertThrows(DatosInvalidosException.class, () -> service.create(null));

		dto.setMateriaId(0);
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setMateriaId(101L);
		dto.setNumeroGrupo(0);
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setNumeroGrupo(1);
		dto.setCupoPlaneado(0);
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		verify(grupoRepo, never()).save(any());
	}

	// ─── GET ALL, GET BY ID, GET BY MATERIA ───────────────────────────────────

	/**
	 * Verifica que {@code getAll} retorna la lista de grupos en formato DTO
	 * incluyendo el nombre de la materia asociada.
	 */
	@Test
	void testGetAll_ExitosoYListaVacia() {
		Materia m = new Materia(1001L, "Estructuras", 4, 3, 4, false);
		when(grupoRepo.findAll()).thenReturn(List.of(grupo));
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(m));

		List<GrupoDTO> lista = service.getAll();

		assertNotNull(lista);
		assertEquals(1, lista.size());
		assertEquals("Estructuras", lista.get(0).getNombreMateria());

		when(grupoRepo.findAll()).thenReturn(Collections.emptyList());
		assertTrue(service.getAll().isEmpty());
	}

	/**
	 * Verifica que {@code getById} y {@code getByMateriaId} retornan los DTOs
	 * esperados y lanzan excepciones ante identificadores inválidos.
	 */
	@Test
	void testGetByIdYGetByMateriaId_ExitosoYValidaciones() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(grupoRepo.findByMateriaId(101L)).thenReturn(List.of(grupo));

		assertEquals(1, service.getById(1L).getNumeroGrupo());
		assertEquals(1, service.getByMateriaId(101L).size());

		assertThrows(DatosInvalidosException.class, () -> service.getById(null));
		assertThrows(DatosInvalidosException.class, () -> service.getByMateriaId(0));

		when(grupoRepo.findById(999L)).thenReturn(Optional.empty());
		assertThrows(RecursoNoEncontradoException.class, () -> service.getById(999L));
	}

	// ─── UPDATE BY ID ─────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code updateById} modifica exitosamente el número y cupo del
	 * grupo cuando no hay cruces en el nuevo bloque.
	 */
	@Test
	void testUpdateById_Exitoso() {
		Materia m = new Materia(1001L, "Estructuras", 4, 3, 4, false);
		m.setId(101L);

		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(grupoRepo.findByMateriaIdAndNumeroGrupo(101L, 2)).thenReturn(Optional.empty());
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(m));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(Collections.emptyList());
		when(grupoRepo.findByNumeroGrupo(2)).thenReturn(Collections.emptyList());
		when(grupoRepo.save(any(Grupo.class))).thenReturn(grupo);

		GrupoDTO actualizacion = new GrupoDTO();
		actualizacion.setNumeroGrupo(2);
		actualizacion.setCupoPlaneado(35);

		int resultado = service.updateById(1L, actualizacion);

		assertEquals(0, resultado);
		verify(grupoRepo, times(1)).save(grupo);
		verify(parametroServ, times(1)).marcarPublicado(false);
	}

	/**
	 * Verifica que {@code updateById} lanza {@link CruceHorarioException} si al
	 * cambiar el número de grupo (bloque) sus sesiones se cruzan con otro grupo
	 * del mismo semestre en el nuevo bloque (RF-6).
	 */
	@Test
	void testUpdateById_LanzaExcepcionCruceAlCambiarDeBloque() {
		Materia m1 = new Materia(1001L, "Estructuras", 4, 3, 4, false);
		m1.setId(101L);
		Materia m2 = new Materia(1002L, "Bases de Datos", 4, 3, 4, false);
		m2.setId(202L);

		Grupo otroGrupoBloque2 = new Grupo(202L, 2, 35);
		otroGrupoBloque2.setId(2L);

		Sesion sesionPropia = new Sesion(1L, 1, LocalTime.of(8, 0), LocalTime.of(10, 0));
		Sesion sesionOtra = new Sesion(2L, 1, LocalTime.of(8, 0), LocalTime.of(10, 0));

		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(grupoRepo.findByMateriaIdAndNumeroGrupo(101L, 2)).thenReturn(Optional.empty());
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(m1));
		when(materiaRepo.findById(202L)).thenReturn(Optional.of(m2));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(sesionPropia));
		when(grupoRepo.findByNumeroGrupo(2)).thenReturn(List.of(otroGrupoBloque2));
		when(sesionRepo.findByGrupoId(2L)).thenReturn(List.of(sesionOtra));

		GrupoDTO actualizacion = new GrupoDTO();
		actualizacion.setNumeroGrupo(2);
		actualizacion.setCupoPlaneado(35);

		assertThrows(CruceHorarioException.class, () -> service.updateById(1L, actualizacion));
		verify(grupoRepo, never()).save(any());
	}

	/**
	 * Verifica que {@code updateById} rechaza cupo excedido y número de grupo
	 * duplicado.
	 */
	@Test
	void testUpdateById_LanzaExcepcionCupoExcedidoYDuplicado() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));

		GrupoDTO excedido = new GrupoDTO();
		excedido.setNumeroGrupo(1);
		excedido.setCupoPlaneado(45);
		assertThrows(CupoExcedidoException.class, () -> service.updateById(1L, excedido));

		Grupo otroGrupo = new Grupo(101L, 2, 35);
		otroGrupo.setId(2L);
		when(grupoRepo.findByMateriaIdAndNumeroGrupo(101L, 2)).thenReturn(Optional.of(otroGrupo));

		GrupoDTO duplicado = new GrupoDTO();
		duplicado.setNumeroGrupo(2);
		duplicado.setCupoPlaneado(35);
		assertThrows(RecursoDuplicadoException.class, () -> service.updateById(1L, duplicado));
	}

	// ─── DELETE BY ID, COUNT Y EXIST BY ID ────────────────────────────────────

	/**
	 * Verifica que {@code deleteById} elimina el grupo junto con todas sus
	 * sesiones asociadas en cascada y despublica el horario.
	 */
	@Test
	void testDeleteById_ExitosoConBorradoDeSesiones() {
		Sesion s1 = new Sesion(1L, 1, LocalTime.of(7, 0), LocalTime.of(9, 0));
		s1.setId(50L);

		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(s1));

		int resultado = service.deleteById(1L);

		assertEquals(0, resultado);
		verify(sesionRepo, times(1)).deleteAll(List.of(s1));
		verify(grupoRepo, times(1)).delete(grupo);
		verify(parametroServ, times(1)).marcarPublicado(false);
	}

	/**
	 * Verifica las operaciones {@code count} y {@code existById}.
	 */
	@Test
	void testCountYExistById_Exitoso() {
		when(grupoRepo.count()).thenReturn(4L);
		when(grupoRepo.existsById(1L)).thenReturn(true);

		assertEquals(4L, service.count());
		assertTrue(service.existById(1L));
		assertFalse(service.existById(null));
	}
}
