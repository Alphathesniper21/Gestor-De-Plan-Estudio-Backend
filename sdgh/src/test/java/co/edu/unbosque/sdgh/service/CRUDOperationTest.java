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

import co.edu.unbosque.sdgh.dto.MateriaDTO;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.MateriaRepository;

/**
 * Pruebas unitarias del contrato generico definido por la interfaz
 * {@link CRUDOperation}, verificadas a traves de {@link MateriaService}.
 * <p>
 * Cubre todos los metodos del contrato generico ({@code create},
 * {@code getAll}, {@code deleteById}, {@code updateById}, {@code count},
 * {@code existById}), garantizando que se cumpla la interfaz comun de servicios
 * en el backend.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see CRUDOperation
 * @see MateriaService
 */
class CRUDOperationTest {

	/** Mock del repositorio. */
	@Mock
	private MateriaRepository materiaRepo;

	/** Mapper para conversion entre entidad y DTO. */
	private ModelMapper mapper;

	/** Instancia del servicio bajo prueba (implementacion de CRUDOperation). */
	private CRUDOperation<MateriaDTO> crudService;

	/** Entidad de referencia. */
	private Materia materia;

	/** DTO de referencia. */
	private MateriaDTO dto;

	/**
	 * Prepara los mocks e inicializa el servicio como {@link CRUDOperation} antes
	 * de cada caso.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mapper = new ModelMapper();
		MateriaService service = new MateriaService();

		try {
			var f1 = MateriaService.class.getDeclaredField("materiaRepo");
			f1.setAccessible(true);
			f1.set(service, materiaRepo);

			var f2 = MateriaService.class.getDeclaredField("mapper");
			f2.setAccessible(true);
			f2.set(service, mapper);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		crudService = service;

		materia = new Materia();
		materia.setId(101L);
		materia.setNombre("Calculo Diferencial");
		materia.setSemestre(1);
		materia.setNcreditos(3);
		materia.setHorasDeLaMateria(4);
		materia.setCorrelativa(false);

		dto = new MateriaDTO();
		dto.setId(101L);
		dto.setNombre("Calculo Diferencial");
		dto.setSemestre(1);
		dto.setNcreditos(3);
		dto.setHorasDeLaMateria(4);
		dto.setCorrelativa(false);
	}

	// ─── CONTRATO: CREATE ─────────────────────────────────────────────────

	/**
	 * Verifica que {@code create} retorna 0 cuando la entidad es valida y no esta
	 * duplicada.
	 */
	@Test
	void testCreate_Exitoso() {
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
		when(materiaRepo.findByNombre("Calculo Diferencial")).thenReturn(Optional.of(materia));

		assertThrows(RecursoDuplicadoException.class, () -> crudService.create(dto));
	}

	// ─── CONTRATO: GET ALL ────────────────────────────────────────────────

	/**
	 * Verifica que {@code getAll} retorna la lista de elementos en formato DTO.
	 */
	@Test
	void testGetAll_Exitoso() {
		when(materiaRepo.findAll()).thenReturn(List.of(materia));

		List<MateriaDTO> lista = crudService.getAll();

		assertNotNull(lista);
		assertEquals(1, lista.size());
	}

	// ─── CONTRATO: UPDATE BY ID ───────────────────────────────────────────

	/**
	 * Verifica que {@code updateById} actualiza correctamente el recurso existente
	 * y retorna 0.
	 */
	@Test
	void testUpdateById_Exitoso() {
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(materiaRepo.findByNombre("Calculo Diferencial")).thenReturn(Optional.of(materia));
		when(materiaRepo.save(any(Materia.class))).thenReturn(materia);

		int resultado = crudService.updateById(101L, dto);

		assertEquals(0, resultado);
		verify(materiaRepo, times(1)).save(materia);
	}

	/**
	 * Verifica que {@code updateById} lanza {@link RecursoNoEncontradoException} si
	 * el id no existe.
	 */
	@Test
	void testUpdateById_LanzaExcepcionNoEncontrado() {
		when(materiaRepo.findById(999L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> crudService.updateById(999L, dto));
	}

	// ─── CONTRATO: DELETE BY ID ───────────────────────────────────────────

	/**
	 * Verifica que {@code deleteById} elimina el recurso existente y retorna 0.
	 */
	@Test
	void testDeleteById_Exitoso() {
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));

		int resultado = crudService.deleteById(101L);

		assertEquals(0, resultado);
		verify(materiaRepo, times(1)).delete(materia);
	}

	/**
	 * Verifica que {@code deleteById} lanza {@link RecursoNoEncontradoException}
	 * cuando el id a eliminar no existe.
	 */
	@Test
	void testDeleteById_LanzaExcepcionNoEncontrado() {
		when(materiaRepo.findById(999L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> crudService.deleteById(999L));
	}

	// ─── CONTRATO: COUNT Y EXIST BY ID ────────────────────────────────────

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
