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
 * Pruebas unitarias para la clase {@link MateriaService}.
 * <p>
 * Verifica la logica de negocio del servicio de materias, incluyendo la
 * creacion y edicion de cursos (RF-1), validacion de rango de semestres (1 a 9),
 * creditos, horas de clase, unicidad del nombre, operaciones CRUD completas y
 * filtros por semestre y correlatividad.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see MateriaService
 * @see CRUDOperation
 */
class MateriaServiceTest {

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
	private MateriaService service;

	/**
	 * Entidad de materia de referencia utilizada en los tests.
	 */
	private Materia materia;

	/**
	 * DTO de materia de referencia utilizado en los tests.
	 */
	private MateriaDTO dto;

	/**
	 * Inicializa los mocks, inyecta las dependencias en el servicio y prepara los
	 * objetos de prueba antes de cada caso.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mapper = new ModelMapper();
		service = new MateriaService();

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

		materia = new Materia();
		materia.setId(101L);
		materia.setNombre("Programacion I");
		materia.setSemestre(2);
		materia.setNcreditos(3);
		materia.setHorasDeLaMateria(4);
		materia.setCorrelativa(false);

		dto = new MateriaDTO();
		dto.setId(101L);
		dto.setNombre("Programacion I");
		dto.setSemestre(2);
		dto.setNcreditos(3);
		dto.setHorasDeLaMateria(4);
		dto.setCorrelativa(false);
	}

	// ─── CREATE (RF-1) ──────────────────────────────────────────────────────────

	/**
	 * Verifica que la creacion de una materia nueva retorna 0 cuando todos los
	 * datos son validos y el nombre no esta duplicado en la base de datos.
	 */
	@Test
	void testCreate_Exitoso() {
		when(materiaRepo.findByNombre("Programacion I")).thenReturn(Optional.empty());
		when(materiaRepo.save(any(Materia.class))).thenReturn(materia);

		int resultado = service.create(dto);

		assertEquals(0, resultado);
		verify(materiaRepo, times(1)).save(any(Materia.class));
	}

	/**
	 * Verifica que se lanza {@link DatosInvalidosException} con el mensaje exacto
	 * cuando el DTO de entrada es nulo.
	 */
	@Test
	void testCreate_LanzaExcepcionDatosNulos() {
		DatosInvalidosException ex = assertThrows(DatosInvalidosException.class, () -> service.create(null));

		assertEquals("Los datos de la materia no pueden ser nulos", ex.getMessage());
		verify(materiaRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link DatosInvalidosException} cuando el nombre de la
	 * materia es nulo o contiene unicamente espacios en blanco.
	 */
	@Test
	void testCreate_LanzaExcepcionNombreInvalido() {
		dto.setNombre("   ");
		DatosInvalidosException exEspacios = assertThrows(DatosInvalidosException.class, () -> service.create(dto));
		assertEquals("El nombre de la materia no puede estar vacio", exEspacios.getMessage());

		dto.setNombre(null);
		DatosInvalidosException exNulo = assertThrows(DatosInvalidosException.class, () -> service.create(dto));
		assertEquals("El nombre de la materia no puede estar vacio", exNulo.getMessage());

		verify(materiaRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link DatosInvalidosException} cuando el semestre esta
	 * fuera del rango institucional permitido (menor a 1 o mayor a 9).
	 */
	@Test
	void testCreate_LanzaExcepcionSemestreFueraDeRango() {
		dto.setSemestre(0);
		DatosInvalidosException exBajo = assertThrows(DatosInvalidosException.class, () -> service.create(dto));
		assertTrue(exBajo.getMessage().contains("El semestre debe estar entre 1 y 9"));

		dto.setSemestre(10);
		DatosInvalidosException exAlto = assertThrows(DatosInvalidosException.class, () -> service.create(dto));
		assertTrue(exAlto.getMessage().contains("El semestre debe estar entre 1 y 9"));

		verify(materiaRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link DatosInvalidosException} cuando el numero de
	 * creditos o las horas de la materia son menores o iguales a cero.
	 */
	@Test
	void testCreate_LanzaExcepcionCreditosOHorasInvalidas() {
		dto.setNcreditos(0);
		DatosInvalidosException exCreditos = assertThrows(DatosInvalidosException.class, () -> service.create(dto));
		assertEquals("El numero de creditos debe ser mayor a 0", exCreditos.getMessage());

		dto.setNcreditos(3);
		dto.setHorasDeLaMateria(0);
		DatosInvalidosException exHoras = assertThrows(DatosInvalidosException.class, () -> service.create(dto));
		assertEquals("Las horas de la materia deben ser mayores a 0", exHoras.getMessage());

		verify(materiaRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link RecursoDuplicadoException} cuando ya existe una
	 * materia con el mismo nombre registrado en el sistema.
	 */
	@Test
	void testCreate_LanzaExcepcionNombreDuplicado() {
		when(materiaRepo.findByNombre("Programacion I")).thenReturn(Optional.of(materia));

		RecursoDuplicadoException ex = assertThrows(RecursoDuplicadoException.class, () -> service.create(dto));

		assertEquals("Ya existe una materia con el nombre Programacion I", ex.getMessage());
		verify(materiaRepo, never()).save(any());
	}

	// ─── GET ALL ──────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code getAll} retorna la lista de todas las materias
	 * registradas convertidas a objetos DTO.
	 */
	@Test
	void testGetAll_Exitoso() {
		when(materiaRepo.findAll()).thenReturn(List.of(materia));

		List<MateriaDTO> lista = service.getAll();

		assertNotNull(lista);
		assertEquals(1, lista.size());
		assertEquals("Programacion I", lista.get(0).getNombre());
		verify(materiaRepo, times(1)).findAll();
	}

	/**
	 * Verifica que {@code getAll} retorna una lista vacia sin fallar cuando no hay
	 * materias registradas en la base de datos.
	 */
	@Test
	void testGetAll_ListaVacia() {
		when(materiaRepo.findAll()).thenReturn(Collections.emptyList());

		List<MateriaDTO> lista = service.getAll();

		assertNotNull(lista);
		assertTrue(lista.isEmpty());
	}

	// ─── GET BY ID / NOMBRE / SEMESTRE ────────────────────────────────────

	/**
	 * Verifica que {@code getById} retorna el DTO de la materia cuando el codigo
	 * existe en la base de datos.
	 */
	@Test
	void testGetById_Exitoso() {
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));

		MateriaDTO resultado = service.getById(101L);

		assertNotNull(resultado);
		assertEquals("Programacion I", resultado.getNombre());
	}

	/**
	 * Verifica que {@code getById} lanza {@link RecursoNoEncontradoException}
	 * cuando no existe una materia con el codigo consultado.
	 */
	@Test
	void testGetById_LanzaExcepcionNoEncontrado() {
		when(materiaRepo.findById(999L)).thenReturn(Optional.empty());

		RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
				() -> service.getById(999L));

		assertEquals("No existe una materia con el codigo 999", ex.getMessage());
	}

	/**
	 * Verifica que {@code getByNombre} recupera la materia correctamente o lanza
	 * excepcion si el nombre no existe o esta vacio.
	 */
	@Test
	void testGetByNombre_ExitosoYValidaciones() {
		when(materiaRepo.findByNombre("Programacion I")).thenReturn(Optional.of(materia));

		MateriaDTO resultado = service.getByNombre("Programacion I");
		assertNotNull(resultado);
		assertEquals("Programacion I", resultado.getNombre());

		assertThrows(DatosInvalidosException.class, () -> service.getByNombre("  "));

		when(materiaRepo.findByNombre("Inexistente")).thenReturn(Optional.empty());
		assertThrows(RecursoNoEncontradoException.class, () -> service.getByNombre("Inexistente"));
	}

	/**
	 * Verifica que {@code getBySemestre} retorna las materias del semestre
	 * consultado y valida que el semestre este entre 1 y 9.
	 */
	@Test
	void testGetBySemestre_ExitosoYValidaciones() {
		when(materiaRepo.findBySemestre(2)).thenReturn(List.of(materia));

		List<MateriaDTO> resultado = service.getBySemestre(2);
		assertNotNull(resultado);
		assertEquals(1, resultado.size());

		assertThrows(DatosInvalidosException.class, () -> service.getBySemestre(0));
		assertThrows(DatosInvalidosException.class, () -> service.getBySemestre(10));
	}

	// ─── UPDATE BY ID ─────────────────────────────────────────────────────

	/**
	 * Verifica que {@code updateById} actualiza correctamente los datos de una
	 * materia existente y retorna 0.
	 */
	@Test
	void testUpdateById_Exitoso() {
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(materiaRepo.findByNombre("Programacion I")).thenReturn(Optional.of(materia));
		when(materiaRepo.save(any(Materia.class))).thenReturn(materia);

		int resultado = service.updateById(101L, dto);

		assertEquals(0, resultado);
		verify(materiaRepo, times(1)).save(materia);
	}

	/**
	 * Verifica que {@code updateById} lanza {@link DatosInvalidosException} cuando
	 * el id proporcionado es nulo.
	 */
	@Test
	void testUpdateById_LanzaExcepcionIdNulo() {
		DatosInvalidosException ex = assertThrows(DatosInvalidosException.class,
				() -> service.updateById(null, dto));

		assertEquals("El id de la materia no puede ser nulo", ex.getMessage());
	}

	/**
	 * Verifica que {@code updateById} lanza {@link RecursoNoEncontradoException}
	 * cuando la materia a modificar no existe en el repositorio.
	 */
	@Test
	void testUpdateById_LanzaExcepcionNoEncontrado() {
		when(materiaRepo.findById(999L)).thenReturn(Optional.empty());

		RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
				() -> service.updateById(999L, dto));

		assertEquals("No existe una materia con el id 999", ex.getMessage());
	}

	/**
	 * Verifica que {@code updateById} lanza {@link RecursoDuplicadoException} si el
	 * nuevo nombre asignado ya pertenece a otra materia diferente.
	 */
	@Test
	void testUpdateById_LanzaExcepcionNombreDuplicado() {
		Materia otra = new Materia();
		otra.setId(202L);
		otra.setNombre("Programacion I");

		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(materiaRepo.findByNombre("Programacion I")).thenReturn(Optional.of(otra));

		RecursoDuplicadoException ex = assertThrows(RecursoDuplicadoException.class,
				() -> service.updateById(101L, dto));

		assertEquals("Ya existe otra materia con el nombre Programacion I", ex.getMessage());
	}

	// ─── DELETE BY ID ─────────────────────────────────────────────────────

	/**
	 * Verifica que {@code deleteById} elimina correctamente una materia de la base
	 * de datos cuando el codigo existe y retorna 0.
	 */
	@Test
	void testDeleteById_Exitoso() {
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));

		int resultado = service.deleteById(101L);

		assertEquals(0, resultado);
		verify(materiaRepo, times(1)).delete(materia);
	}

	/**
	 * Verifica que {@code deleteById} lanza {@link DatosInvalidosException} cuando
	 * el id a eliminar es nulo.
	 */
	@Test
	void testDeleteById_LanzaExcepcionIdNulo() {
		DatosInvalidosException ex = assertThrows(DatosInvalidosException.class,
				() -> service.deleteById(null));

		assertEquals("El id de la materia no puede ser nulo", ex.getMessage());
	}

	/**
	 * Verifica que {@code deleteById} lanza {@link RecursoNoEncontradoException}
	 * cuando se intenta eliminar una materia con un codigo inexistente.
	 */
	@Test
	void testDeleteById_LanzaExcepcionNoEncontrado() {
		when(materiaRepo.findById(999L)).thenReturn(Optional.empty());

		RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
				() -> service.deleteById(999L));

		assertEquals("No existe una materia con el id 999", ex.getMessage());
	}

	// ─── OPERACIONES DE CONTEO Y EXISTENCIA ────────────────────────────────

	/**
	 * Verifica que {@code count} retorna la cantidad exacta de registros del
	 * repositorio.
	 */
	@Test
	void testCount_Exitoso() {
		when(materiaRepo.count()).thenReturn(8L);

		assertEquals(8L, service.count());
	}

	/**
	 * Verifica que {@code existById} devuelve true cuando la materia existe y
	 * false cuando no existe o el parametro es nulo.
	 */
	@Test
	void testExistById_Exitoso() {
		when(materiaRepo.existsById(101L)).thenReturn(true);
		when(materiaRepo.existsById(999L)).thenReturn(false);

		assertTrue(service.existById(101L));
		assertFalse(service.existById(999L));
		assertFalse(service.existById(null));
	}
}
