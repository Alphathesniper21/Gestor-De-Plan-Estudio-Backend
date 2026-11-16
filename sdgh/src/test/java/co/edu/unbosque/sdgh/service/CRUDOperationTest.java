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
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.sdgh.dto.MateriaDTO;
import co.edu.unbosque.sdgh.entity.ClaveParametro;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.GrupoRepository;
import co.edu.unbosque.sdgh.repository.MateriaRepository;
import co.edu.unbosque.sdgh.repository.SesionRepository;

/**
 * Pruebas unitarias del contrato genérico definido por la interfaz
 * {@link CRUDOperation}, verificadas a través de {@link MateriaService}.
 * <p>
 * Cubre todos los métodos del contrato ({@code create}, {@code getAll},
 * {@code deleteById}, {@code updateById}, {@code count}, {@code existById}),
 * incluyendo casos de éxito, validaciones de entrada y lanzamiento de
 * excepciones de negocio.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see CRUDOperation
 * @see MateriaService
 */
class CRUDOperationTest {

	/**
	 * Mock del repositorio de materias.
	 */
	@Mock
	private MateriaRepository materiaRepo;

	/**
	 * Mock del repositorio de grupos.
	 */
	@Mock
	private GrupoRepository grupoRepo;

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
	 * Instancia del servicio bajo prueba (implementación de {@link CRUDOperation}).
	 */
	private CRUDOperation<MateriaDTO> crudService;

	/**
	 * Entidad de referencia utilizada en los tests.
	 */
	private Materia materia;

	/**
	 * DTO de referencia utilizado en los tests.
	 */
	private MateriaDTO dto;

	/**
	 * Inicializa los mocks, inyecta las dependencias en el servicio por reflexión
	 * y prepara los objetos de prueba antes de cada caso.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mapper = new ModelMapper();
		MateriaService service = new MateriaService();
		ReflectionTestUtils.setField(service, "materiaRepo", materiaRepo);
		ReflectionTestUtils.setField(service, "grupoRepo", grupoRepo);
		ReflectionTestUtils.setField(service, "sesionRepo", sesionRepo);
		ReflectionTestUtils.setField(service, "parametroServ", parametroServ);
		ReflectionTestUtils.setField(service, "mapper", mapper);

		when(parametroServ.getEntero(ClaveParametro.SESIONES_MAXIMAS)).thenReturn(4);

		crudService = service;

		materia = new Materia();
		materia.setId(101L);
		materia.setCodigo(1001L);
		materia.setNombre("Calculo Diferencial");
		materia.setSemestre(1);
		materia.setNcreditos(3);
		materia.setHorasDeLaMateria(4);
		materia.setCorrelativa(false);

		dto = new MateriaDTO();
		dto.setId(101L);
		dto.setCodigo(1001L);
		dto.setNombre("Calculo Diferencial");
		dto.setSemestre(1);
		dto.setNcreditos(3);
		dto.setHorasDeLaMateria(4);
		dto.setCorrelativa(false);
	}

	// ─── CONTRATO: CREATE ─────────────────────────────────────────────────────

	/**
	 * Verifica que {@code create} retorna 0 cuando la entidad es válida y no está
	 * duplicada.
	 */
	@Test
	void testCreate_Exitoso() {
		when(materiaRepo.existsByCodigo(1001L)).thenReturn(false);
		when(materiaRepo.findByNombre("Calculo Diferencial")).thenReturn(Optional.empty());
		when(materiaRepo.save(any(Materia.class))).thenReturn(materia);

		int resultado = crudService.create(dto);

		assertEquals(0, resultado);
		verify(materiaRepo, times(1)).save(any(Materia.class));
	}

	/**
	 * Verifica que {@code create} lanza {@link DatosInvalidosException} cuando los
	 * datos son nulos.
	 */
	@Test
	void testCreate_LanzaExcepcionDatosNulos() {
		assertThrows(DatosInvalidosException.class, () -> crudService.create(null));
	}

	/**
	 * Verifica que {@code create} lanza {@link RecursoDuplicadoException} si el
	 * recurso ya existe.
	 */
	@Test
	void testCreate_LanzaExcepcionDuplicado() {
		when(materiaRepo.existsByCodigo(1001L)).thenReturn(false);
		when(materiaRepo.findByNombre("Calculo Diferencial")).thenReturn(Optional.of(materia));

		assertThrows(RecursoDuplicadoException.class, () -> crudService.create(dto));
	}

	// ─── CONTRATO: GET ALL ────────────────────────────────────────────────────

	/**
	 * Verifica que {@code getAll} retorna la lista de elementos en formato DTO.
	 */
	@Test
	void testGetAll_Exitoso() {
		when(materiaRepo.findAll()).thenReturn(List.of(materia));

		List<MateriaDTO> lista = crudService.getAll();

		assertNotNull(lista);
		assertEquals(1, lista.size());
		assertEquals("Calculo Diferencial", lista.get(0).getNombre());
	}

	/**
	 * Verifica que {@code getAll} retorna una lista vacía cuando no existen
	 * registros.
	 */
	@Test
	void testGetAll_ListaVacia() {
		when(materiaRepo.findAll()).thenReturn(Collections.emptyList());

		List<MateriaDTO> lista = crudService.getAll();

		assertNotNull(lista);
		assertTrue(lista.isEmpty());
	}

	// ─── CONTRATO: UPDATE BY ID ───────────────────────────────────────────────

	/**
	 * Verifica que {@code updateById} actualiza correctamente el recurso existente
	 * y retorna 0.
	 */
	@Test
	void testUpdateById_Exitoso() {
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(materiaRepo.findByCodigo(1001L)).thenReturn(Optional.of(materia));
		when(materiaRepo.findByNombre("Calculo Diferencial")).thenReturn(Optional.of(materia));
		when(grupoRepo.findByMateriaId(101L)).thenReturn(Collections.emptyList());
		when(materiaRepo.save(any(Materia.class))).thenReturn(materia);

		int resultado = crudService.updateById(101L, dto);

		assertEquals(0, resultado);
		verify(materiaRepo, times(1)).save(materia);
		verify(parametroServ, times(1)).marcarPublicado(false);
	}

	/**
	 * Verifica que {@code updateById} lanza {@link RecursoNoEncontradoException} si
	 * el identificador no existe.
	 */
	@Test
	void testUpdateById_LanzaExcepcionNoEncontrado() {
		when(materiaRepo.findById(999L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> crudService.updateById(999L, dto));
	}

	// ─── CONTRATO: DELETE BY ID ───────────────────────────────────────────────

	/**
	 * Verifica que {@code deleteById} elimina el recurso existente y retorna 0.
	 */
	@Test
	void testDeleteById_Exitoso() {
		when(grupoRepo.findByMateriaId(101L)).thenReturn(Collections.emptyList());
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));

		int resultado = crudService.deleteById(101L);

		assertEquals(0, resultado);
		verify(materiaRepo, times(1)).delete(materia);
		verify(parametroServ, times(1)).marcarPublicado(false);
	}

	/**
	 * Verifica que {@code deleteById} lanza {@link RecursoNoEncontradoException}
	 * cuando el id a eliminar no existe.
	 */
	@Test
	void testDeleteById_LanzaExcepcionNoEncontrado() {
		when(grupoRepo.findByMateriaId(999L)).thenReturn(Collections.emptyList());
		when(materiaRepo.findById(999L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> crudService.deleteById(999L));
	}

	// ─── CONTRATO: COUNT Y EXIST BY ID ────────────────────────────────────────

	/**
	 * Verifica las operaciones del contrato {@code count} y {@code existById}.
	 */
	@Test
	void testCountYExistById_Exitoso() {
		when(materiaRepo.count()).thenReturn(10L);
		when(materiaRepo.existsById(101L)).thenReturn(true);

		assertEquals(10L, crudService.count());
		assertTrue(crudService.existById(101L));
		assertFalse(crudService.existById(null));
	}
}
