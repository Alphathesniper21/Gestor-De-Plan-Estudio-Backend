package co.edu.unbosque.sdgh.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import co.edu.unbosque.sdgh.dto.GrupoDTO;
import co.edu.unbosque.sdgh.entity.Grupo;
import co.edu.unbosque.sdgh.entity.Sesion;
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
 * Verifica la logica de negocio de los grupos de materias, incluyendo la
 * creacion con validacion de cupo maximo de 40 mas 10 % de tolerancia (RF-2),
 * unicidad de grupo por curso, eliminacion en cascada de sesiones vinculadas,
 * actualizacion controlada y operaciones CRUD.
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
	 * Mock del repositorio de materias (para verificar existencia del curso).
	 */
	@Mock
	private MateriaRepository materiaRepo;

	/**
	 * Mock del repositorio de sesiones (para eliminacion en cascada).
	 */
	@Mock
	private SesionRepository sesionRepo;

	/**
	 * Mapper para la conversion entre entidades y DTOs.
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
	 * Inicializa los mocks, inyecta las dependencias en el servicio y prepara los
	 * objetos de prueba antes de cada caso.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mapper = new ModelMapper();
		service = new GrupoService();

		try {
			var f1 = GrupoService.class.getDeclaredField("grupoRepo");
			f1.setAccessible(true);
			f1.set(service, grupoRepo);

			var f2 = GrupoService.class.getDeclaredField("materiaRepo");
			f2.setAccessible(true);
			f2.set(service, materiaRepo);

			var f3 = GrupoService.class.getDeclaredField("sesionRepo");
			f3.setAccessible(true);
			f3.set(service, sesionRepo);

			var f4 = GrupoService.class.getDeclaredField("mapper");
			f4.setAccessible(true);
			f4.set(service, mapper);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

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

	// ─── CREATE (RF-2) ──────────────────────────────────────────────────────────

	/**
	 * Verifica que la creacion de un grupo retorna 0 cuando los datos son validos,
	 * la materia existe y el cupo planeado es de 40 estudiantes (cupo base).
	 */
	@Test
	void testCreate_ExitosoCupoBase() {
		when(materiaRepo.existsById(101L)).thenReturn(true);
		when(grupoRepo.existsByMateriaIdAndNumeroGrupo(101L, 1)).thenReturn(false);
		when(grupoRepo.save(any(Grupo.class))).thenReturn(grupo);

		int resultado = service.create(dto);

		assertEquals(0, resultado);
		verify(grupoRepo, times(1)).save(any(Grupo.class));
	}

	/**
	 * Verifica el criterio de aceptacion de RF-2: un cupo de 44 estudiantes se
	 * acepta exitosamente (40 mas el 10 % de tolerancia).
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
	 * Verifica el criterio de aceptacion de RF-2: un cupo de 45 estudiantes se
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

		RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
				() -> service.create(dto));

		assertEquals("No existe una materia con el codigo 101", ex.getMessage());
		verify(grupoRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link RecursoDuplicadoException} cuando la materia ya
	 * tiene asignado un grupo con ese mismo numero.
	 */
	@Test
	void testCreate_LanzaExcepcionNumeroGrupoDuplicado() {
		when(materiaRepo.existsById(101L)).thenReturn(true);
		when(grupoRepo.existsByMateriaIdAndNumeroGrupo(101L, 1)).thenReturn(true);

		RecursoDuplicadoException ex = assertThrows(RecursoDuplicadoException.class,
				() -> service.create(dto));

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

	// ─── GET ALL ──────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code getAll} retorna la lista de grupos en formato DTO.
	 */
	@Test
	void testGetAll_Exitoso() {
		when(grupoRepo.findAll()).thenReturn(List.of(grupo));

		List<GrupoDTO> lista = service.getAll();

		assertNotNull(lista);
		assertEquals(1, lista.size());
		assertEquals(1, lista.get(0).getNumeroGrupo());
	}

	/**
	 * Verifica que {@code getAll} retorna una lista vacia cuando no existen
	 * grupos.
	 */
	@Test
	void testGetAll_ListaVacia() {
		when(grupoRepo.findAll()).thenReturn(Collections.emptyList());

		List<GrupoDTO> lista = service.getAll();

		assertNotNull(lista);
		assertTrue(lista.isEmpty());
	}

	// ─── GET BY ID / MATERIA ──────────────────────────────────────────────

	/**
	 * Verifica que {@code getById} retorna el DTO del grupo cuando el id existe.
	 */
	@Test
	void testGetById_Exitoso() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));

		GrupoDTO resultado = service.getById(1L);

		assertNotNull(resultado);
		assertEquals(1, resultado.getNumeroGrupo());
	}

	/**
	 * Verifica que {@code getById} lanza excepciones si el id es nulo o
	 * inexistente.
	 */
	@Test
	void testGetById_LanzaExcepcionIdInvalido() {
		assertThrows(DatosInvalidosException.class, () -> service.getById(null));

		when(grupoRepo.findById(999L)).thenReturn(Optional.empty());
		assertThrows(RecursoNoEncontradoException.class, () -> service.getById(999L));
	}

	/**
	 * Verifica que {@code getByMateriaId} retorna los grupos de la materia o lanza
	 * excepcion si el id es menor o igual a 0.
	 */
	@Test
	void testGetByMateriaId_ExitosoYValidaciones() {
		when(grupoRepo.findByMateriaId(101L)).thenReturn(List.of(grupo));

		List<GrupoDTO> lista = service.getByMateriaId(101L);
		assertNotNull(lista);
		assertEquals(1, lista.size());

		assertThrows(DatosInvalidosException.class, () -> service.getByMateriaId(0));
	}

	// ─── UPDATE BY ID ─────────────────────────────────────────────────────

	/**
	 * Verifica que {@code updateById} modifica exitosamente el numero y cupo del
	 * grupo y retorna 0.
	 */
	@Test
	void testUpdateById_Exitoso() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(grupoRepo.findByMateriaIdAndNumeroGrupo(101L, 2)).thenReturn(Optional.empty());
		when(grupoRepo.save(any(Grupo.class))).thenReturn(grupo);

		GrupoDTO actualizacion = new GrupoDTO();
		actualizacion.setNumeroGrupo(2);
		actualizacion.setCupoPlaneado(35);

		int resultado = service.updateById(1L, actualizacion);

		assertEquals(0, resultado);
		verify(grupoRepo, times(1)).save(grupo);
	}

	/**
	 * Verifica que {@code updateById} rechaza una actualizacion con cupo superior a
	 * 44 lanzando {@link CupoExcedidoException}.
	 */
	@Test
	void testUpdateById_LanzaExcepcionCupoExcedido() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));

		GrupoDTO actualizacion = new GrupoDTO();
		actualizacion.setNumeroGrupo(1);
		actualizacion.setCupoPlaneado(45);

		assertThrows(CupoExcedidoException.class, () -> service.updateById(1L, actualizacion));
		verify(grupoRepo, never()).save(any());
	}

	/**
	 * Verifica que {@code updateById} lanza {@link RecursoDuplicadoException} si el
	 * nuevo numero de grupo ya pertenece a otro grupo de la misma materia.
	 */
	@Test
	void testUpdateById_LanzaExcepcionNumeroDuplicado() {
		Grupo otroGrupo = new Grupo();
		otroGrupo.setId(2L);
		otroGrupo.setMateriaId(101L);
		otroGrupo.setNumeroGrupo(2);

		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(grupoRepo.findByMateriaIdAndNumeroGrupo(101L, 2)).thenReturn(Optional.of(otroGrupo));

		GrupoDTO actualizacion = new GrupoDTO();
		actualizacion.setNumeroGrupo(2);
		actualizacion.setCupoPlaneado(35);

		assertThrows(RecursoDuplicadoException.class, () -> service.updateById(1L, actualizacion));
	}

	// ─── DELETE BY ID ─────────────────────────────────────────────────────

	/**
	 * Verifica que {@code deleteById} elimina el grupo y borra en cascada todas
	 * sus sesiones asociadas en el repositorio de sesiones.
	 */
	@Test
	void testDeleteById_ExitosoConBorradoDeSesiones() {
		Sesion s1 = new Sesion();
		s1.setId(50L);
		s1.setGrupoId(1);

		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupo));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(s1));

		int resultado = service.deleteById(1L);

		assertEquals(0, resultado);
		verify(sesionRepo, times(1)).deleteAll(List.of(s1));
		verify(grupoRepo, times(1)).delete(grupo);
	}

	/**
	 * Verifica que {@code deleteById} lanza excepciones si el id es nulo o
	 * inexistente.
	 */
	@Test
	void testDeleteById_LanzaExcepcionIdInvalido() {
		assertThrows(DatosInvalidosException.class, () -> service.deleteById(null));

		when(grupoRepo.findById(999L)).thenReturn(Optional.empty());
		assertThrows(RecursoNoEncontradoException.class, () -> service.deleteById(999L));
	}

	// ─── OPERACIONES DE CONTEO Y EXISTENCIA ────────────────────────────────

	/**
	 * Verifica que {@code count} retorna la cantidad exacta de grupos.
	 */
	@Test
	void testCount_Exitoso() {
		when(grupoRepo.count()).thenReturn(4L);

		assertEquals(4L, service.count());
	}

	/**
	 * Verifica que {@code existById} devuelve true cuando el grupo existe y false
	 * cuando no existe o es nulo.
	 */
	@Test
	void testExistById_Exitoso() {
		when(grupoRepo.existsById(1L)).thenReturn(true);
		when(grupoRepo.existsById(999L)).thenReturn(false);

		assertTrue(service.existById(1L));
		assertFalse(service.existById(999L));
		assertFalse(service.existById(null));
	}
}
