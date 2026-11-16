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
import co.edu.unbosque.sdgh.entity.Grupo;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.entity.Sesion;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.LimiteSesionesExcedidoException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.GrupoRepository;
import co.edu.unbosque.sdgh.repository.MateriaRepository;
import co.edu.unbosque.sdgh.repository.SesionRepository;

/**
 * Pruebas unitarias para la clase {@link MateriaService}.
 * <p>
 * Verifica la lógica de negocio del servicio de materias (CU02 - RF-1),
 * incluyendo la creación y edición de cursos, validación de código institucional,
 * rango de semestres (1 a 9), créditos, horas pares (sesiones de 2 horas),
 * límite de sesiones semanales según parámetro (1 a 4), unicidad de código y
 * nombre, restricciones por grupos/sesiones asignadas y operaciones CRUD.
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
	 * Inicializa los mocks, inyecta las dependencias en el servicio por reflexión
	 * y prepara los objetos de prueba antes de cada caso.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mapper = new ModelMapper();
		service = new MateriaService();
		ReflectionTestUtils.setField(service, "materiaRepo", materiaRepo);
		ReflectionTestUtils.setField(service, "grupoRepo", grupoRepo);
		ReflectionTestUtils.setField(service, "sesionRepo", sesionRepo);
		ReflectionTestUtils.setField(service, "parametroServ", parametroServ);
		ReflectionTestUtils.setField(service, "mapper", mapper);

		when(parametroServ.getEntero(ClaveParametro.SESIONES_MAXIMAS)).thenReturn(4);

		materia = new Materia();
		materia.setId(101L);
		materia.setCodigo(1001L);
		materia.setNombre("Programacion I");
		materia.setSemestre(2);
		materia.setNcreditos(3);
		materia.setHorasDeLaMateria(4);
		materia.setCorrelativa(false);

		dto = new MateriaDTO();
		dto.setId(101L);
		dto.setCodigo(1001L);
		dto.setNombre("Programacion I");
		dto.setSemestre(2);
		dto.setNcreditos(3);
		dto.setHorasDeLaMateria(4);
		dto.setCorrelativa(false);
	}

	// ─── CREATE (RF-1) ────────────────────────────────────────────────────────

	/**
	 * Verifica que la creación de una materia nueva retorna 0 cuando todos los
	 * datos son válidos y ni el código ni el nombre están duplicados.
	 */
	@Test
	void testCreate_Exitoso() {
		when(materiaRepo.existsByCodigo(1001L)).thenReturn(false);
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
	 * Verifica que se lanza {@link DatosInvalidosException} cuando el código
	 * institucional es menor o igual a 0 o supera los 10 dígitos.
	 */
	@Test
	void testCreate_LanzaExcepcionCodigoInvalido() {
		dto.setCodigo(0);
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setCodigo(10000000000L);
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		verify(materiaRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link DatosInvalidosException} cuando el nombre de la
	 * materia es nulo o contiene únicamente espacios en blanco.
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
	 * Verifica el criterio de aceptación de RF-1: se lanza
	 * {@link DatosInvalidosException} cuando el semestre está fuera del rango
	 * institucional permitido (menor a 1 o mayor a 9).
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
	 * Verifica que se lanza {@link DatosInvalidosException} cuando los créditos son
	 * menores o iguales a 0, las horas son impares o superan el máximo de sesiones
	 * semanales configurado (RF-1).
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

		dto.setHorasDeLaMateria(3); // Impar: no es múltiplo de 2
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setHorasDeLaMateria(10); // 5 sesiones > 4 sesiones máximas
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		verify(materiaRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link RecursoDuplicadoException} cuando ya existe una
	 * materia con el mismo código o con el mismo nombre en el sistema.
	 */
	@Test
	void testCreate_LanzaExcepcionCodigoONombreDuplicado() {
		when(materiaRepo.existsByCodigo(1001L)).thenReturn(true);
		assertThrows(RecursoDuplicadoException.class, () -> service.create(dto));

		when(materiaRepo.existsByCodigo(1001L)).thenReturn(false);
		when(materiaRepo.findByNombre("Programacion I")).thenReturn(Optional.of(materia));
		RecursoDuplicadoException ex = assertThrows(RecursoDuplicadoException.class, () -> service.create(dto));
		assertEquals("Ya existe una materia con el nombre Programacion I", ex.getMessage());

		verify(materiaRepo, never()).save(any());
	}

	// ─── GET ALL, GET BY ID, CODIGO, NOMBRE, SEMESTRE, CORRELATIVA ────────────

	/**
	 * Verifica que {@code getAll} retorna la lista de todas las materias
	 * registradas convertidas a objetos DTO.
	 */
	@Test
	void testGetAll_ExitosoYListaVacia() {
		when(materiaRepo.findAll()).thenReturn(List.of(materia));
		List<MateriaDTO> lista = service.getAll();
		assertEquals(1, lista.size());
		assertEquals("Programacion I", lista.get(0).getNombre());

		when(materiaRepo.findAll()).thenReturn(Collections.emptyList());
		assertTrue(service.getAll().isEmpty());
	}

	/**
	 * Verifica que {@code getById} y {@code getByCodigo} retornan el DTO de la
	 * materia cuando existe o lanzan {@link RecursoNoEncontradoException} cuando no
	 * existe.
	 */
	@Test
	void testGetByIdYGetByCodigo_ExitosoYNoEncontrado() {
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(materiaRepo.findByCodigo(1001L)).thenReturn(Optional.of(materia));

		assertEquals("Programacion I", service.getById(101L).getNombre());
		assertEquals(1001L, service.getByCodigo(1001L).getCodigo());

		when(materiaRepo.findById(999L)).thenReturn(Optional.empty());
		assertThrows(RecursoNoEncontradoException.class, () -> service.getById(999L));

		when(materiaRepo.findByCodigo(999L)).thenReturn(Optional.empty());
		assertThrows(RecursoNoEncontradoException.class, () -> service.getByCodigo(999L));
	}

	/**
	 * Verifica que {@code getByNombre}, {@code getBySemestre} y
	 * {@code getByCorrelativa} filtran las materias correctamente.
	 */
	@Test
	void testFiltrosPorNombreSemestreYCorrelativa() {
		when(materiaRepo.findByNombre("Programacion I")).thenReturn(Optional.of(materia));
		assertEquals("Programacion I", service.getByNombre("Programacion I").getNombre());
		assertThrows(DatosInvalidosException.class, () -> service.getByNombre("  "));

		when(materiaRepo.findBySemestre(2)).thenReturn(List.of(materia));
		assertEquals(1, service.getBySemestre(2).size());
		assertThrows(DatosInvalidosException.class, () -> service.getBySemestre(0));
		assertThrows(DatosInvalidosException.class, () -> service.getBySemestre(10));

		when(materiaRepo.findByCorrelativa(false)).thenReturn(List.of(materia));
		assertEquals(1, service.getByCorrelativa(false).size());
	}

	// ─── UPDATE BY ID ─────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code updateById} actualiza correctamente los datos de una
	 * materia existente, despublica el horario y retorna 0.
	 */
	@Test
	void testUpdateById_Exitoso() {
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(materiaRepo.findByCodigo(1001L)).thenReturn(Optional.of(materia));
		when(materiaRepo.findByNombre("Programacion I")).thenReturn(Optional.of(materia));
		when(grupoRepo.findByMateriaId(101L)).thenReturn(Collections.emptyList());
		when(materiaRepo.save(any(Materia.class))).thenReturn(materia);

		int resultado = service.updateById(101L, dto);

		assertEquals(0, resultado);
		verify(materiaRepo, times(1)).save(materia);
		verify(parametroServ, times(1)).marcarPublicado(false);
	}

	/**
	 * Verifica que {@code updateById} impide reducir las horas de una materia si
	 * algún grupo ya tiene más sesiones asignadas que las nuevas permitidas,
	 * lanzando {@link LimiteSesionesExcedidoException}.
	 */
	@Test
	void testUpdateById_LanzaExcepcionCuandoGrupoExcedeNuevasSesiones() {
		Grupo g = new Grupo(101L, 1, 35);
		g.setId(1L);

		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(materiaRepo.findByCodigo(1001L)).thenReturn(Optional.of(materia));
		when(materiaRepo.findByNombre("Programacion I")).thenReturn(Optional.of(materia));
		when(grupoRepo.findByMateriaId(101L)).thenReturn(List.of(g));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(new Sesion(), new Sesion()));

		dto.setHorasDeLaMateria(2); // Reduce a 1 sesión, pero el grupo ya tiene 2

		assertThrows(LimiteSesionesExcedidoException.class, () -> service.updateById(101L, dto));
	}

	/**
	 * Verifica que {@code updateById} lanza {@link DatosInvalidosException} si se
	 * intenta cambiar el semestre de una materia que ya tiene sesiones asignadas.
	 */
	@Test
	void testUpdateById_LanzaExcepcionCambioSemestreConSesiones() {
		Grupo g = new Grupo(101L, 1, 35);
		g.setId(1L);

		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(materiaRepo.findByCodigo(1001L)).thenReturn(Optional.of(materia));
		when(materiaRepo.findByNombre("Programacion I")).thenReturn(Optional.of(materia));
		when(grupoRepo.findByMateriaId(101L)).thenReturn(List.of(g));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(new Sesion()));

		dto.setSemestre(4);

		assertThrows(DatosInvalidosException.class, () -> service.updateById(101L, dto));
	}

	// ─── DELETE BY ID Y POR CODIGO ────────────────────────────────────────────

	/**
	 * Verifica que {@code deleteById} y {@code deleteByCodigo} eliminan la materia
	 * cuando no tiene grupos asociados, o lanzan {@link DatosInvalidosException}
	 * si ya tiene grupos registrados.
	 */
	@Test
	void testDeleteByIdYDeleteByCodigo_ExitosoYRestriccionGrupos() {
		when(grupoRepo.findByMateriaId(101L)).thenReturn(Collections.emptyList());
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materia));
		when(materiaRepo.findByCodigo(1001L)).thenReturn(Optional.of(materia));

		assertEquals(0, service.deleteById(101L));
		assertEquals(0, service.deleteByCodigo(1001L));
		verify(materiaRepo, times(2)).delete(materia);

		when(grupoRepo.findByMateriaId(101L)).thenReturn(List.of(new Grupo()));
		assertThrows(DatosInvalidosException.class, () -> service.deleteById(101L));
		assertThrows(DatosInvalidosException.class, () -> service.deleteByCodigo(1001L));
	}

	// ─── COUNT Y EXIST BY ID ──────────────────────────────────────────────────

	/**
	 * Verifica que {@code count} y {@code existById} retornan los valores
	 * esperados.
	 */
	@Test
	void testCountYExistById_Exitoso() {
		when(materiaRepo.count()).thenReturn(8L);
		when(materiaRepo.existsById(101L)).thenReturn(true);

		assertEquals(8L, service.count());
		assertTrue(service.existById(101L));
		assertFalse(service.existById(null));
	}
}
