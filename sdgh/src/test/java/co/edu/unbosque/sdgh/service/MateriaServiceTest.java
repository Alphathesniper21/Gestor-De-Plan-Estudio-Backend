package co.edu.unbosque.sdgh.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import co.edu.unbosque.sdgh.dto.MateriaDTO;
import co.edu.unbosque.sdgh.entity.Materia;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.MateriaRepository;

@ExtendWith(MockitoExtension.class)
class MateriaServiceTest {

	@Mock
	private MateriaRepository materiaRepo;

	@Mock
	private ModelMapper mapper;

	@InjectMocks
	private MateriaService materiaService;

	private MateriaDTO materiaDTOValida;
	private Materia materiaEntidad;

	@BeforeEach
	void setUp() {
		materiaDTOValida = new MateriaDTO();
		materiaDTOValida.setId(101L);
		materiaDTOValida.setNombre("Programacion I");
		materiaDTOValida.setSemestre(2);
		materiaDTOValida.setNcreditos(3);
		materiaDTOValida.setHorasDeLaMateria(4);
		materiaDTOValida.setCorrelativa(false);

		materiaEntidad = new Materia();
		materiaEntidad.setId(101L);
		materiaEntidad.setNombre("Programacion I");
		materiaEntidad.setSemestre(2);
		materiaEntidad.setNcreditos(3);
		materiaEntidad.setHorasDeLaMateria(4);
		materiaEntidad.setCorrelativa(false);
	}

	@Test
	@DisplayName("RF-1: Debe crear materia exitosamente cuando los datos son validos y no esta duplicada")
	void debeCrearMateriaExitosamente() {
		when(materiaRepo.findByNombre("Programacion I")).thenReturn(Optional.empty());
		when(mapper.map(materiaDTOValida, Materia.class)).thenReturn(materiaEntidad);

		int resultado = materiaService.create(materiaDTOValida);

		assertEquals(0, resultado);
		verify(materiaRepo, times(1)).save(materiaEntidad);
	}

	@Test
	@DisplayName("RF-1: Debe lanzar excepcion cuando los datos de la materia son nulos")
	void debeLanzarExcepcionCuandoDatosSonNulos() {
		assertThrows(DatosInvalidosException.class, () -> materiaService.create(null));
		verify(materiaRepo, never()).save(any());
	}

	@Test
	@DisplayName("RF-1: Debe lanzar excepcion cuando el nombre es vacio o nulo")
	void debeLanzarExcepcionCuandoNombreEsInvalido() {
		materiaDTOValida.setNombre("   ");
		assertThrows(DatosInvalidosException.class, () -> materiaService.create(materiaDTOValida));

		materiaDTOValida.setNombre(null);
		assertThrows(DatosInvalidosException.class, () -> materiaService.create(materiaDTOValida));
	}

	@Test
	@DisplayName("RF-1: Debe rechazar semestre fuera de rango (1 a 9)")
	void debeRechazarSemestreFueraDeRango() {
		materiaDTOValida.setSemestre(0);
		assertThrows(DatosInvalidosException.class, () -> materiaService.create(materiaDTOValida));

		materiaDTOValida.setSemestre(10);
		assertThrows(DatosInvalidosException.class, () -> materiaService.create(materiaDTOValida));
	}

	@Test
	@DisplayName("RF-1: Debe rechazar creditos o horas menores o iguales a 0")
	void debeRechazarCreditosOHorasInvalidas() {
		materiaDTOValida.setNcreditos(0);
		assertThrows(DatosInvalidosException.class, () -> materiaService.create(materiaDTOValida));

		materiaDTOValida.setNcreditos(3);
		materiaDTOValida.setHorasDeLaMateria(0);
		assertThrows(DatosInvalidosException.class, () -> materiaService.create(materiaDTOValida));
	}

	@Test
	@DisplayName("RF-1: Debe rechazar cuando el nombre ya existe")
	void debeLanzarExcepcionCuandoNombreYaExiste() {
		when(materiaRepo.findByNombre("Programacion I")).thenReturn(Optional.of(materiaEntidad));

		assertThrows(RecursoDuplicadoException.class, () -> materiaService.create(materiaDTOValida));
		verify(materiaRepo, never()).save(any());
	}

	@Test
	@DisplayName("getAll: Debe retornar la lista completa de materias convertidas a DTO")
	void debeRetornarTodasLasMaterias() {
		when(materiaRepo.findAll()).thenReturn(List.of(materiaEntidad));
		when(mapper.map(materiaEntidad, MateriaDTO.class)).thenReturn(materiaDTOValida);

		List<MateriaDTO> lista = materiaService.getAll();

		assertNotNull(lista);
		assertEquals(1, lista.size());
		assertEquals("Programacion I", lista.get(0).getNombre());
	}

	@Test
	@DisplayName("deleteById: Debe eliminar materia cuando el id existe")
	void debeEliminarMateriaExitosamente() {
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materiaEntidad));

		int resultado = materiaService.deleteById(101L);

		assertEquals(0, resultado);
		verify(materiaRepo, times(1)).delete(materiaEntidad);
	}

	@Test
	@DisplayName("deleteById: Debe lanzar excepcion cuando el id no existe o es nulo")
	void debeLanzarExcepcionAlEliminarInexistenteONulo() {
		assertThrows(DatosInvalidosException.class, () -> materiaService.deleteById(null));

		when(materiaRepo.findById(999L)).thenReturn(Optional.empty());
		assertThrows(RecursoNoEncontradoException.class, () -> materiaService.deleteById(999L));
	}

	@Test
	@DisplayName("updateById: Debe actualizar correctamente una materia existente")
	void debeActualizarMateriaExitosamente() {
		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materiaEntidad));
		when(materiaRepo.findByNombre("Programacion I")).thenReturn(Optional.of(materiaEntidad));

		int resultado = materiaService.updateById(101L, materiaDTOValida);

		assertEquals(0, resultado);
		verify(materiaRepo, times(1)).save(materiaEntidad);
	}

	@Test
	@DisplayName("updateById: Debe lanzar excepcion si se intenta usar un nombre ya tomado por otra materia")
	void debeRechazarActualizacionConNombreDuplicado() {
		Materia otraMateria = new Materia();
		otraMateria.setId(202L);
		otraMateria.setNombre("Programacion I");

		when(materiaRepo.findById(101L)).thenReturn(Optional.of(materiaEntidad));
		when(materiaRepo.findByNombre("Programacion I")).thenReturn(Optional.of(otraMateria));

		assertThrows(RecursoDuplicadoException.class, () -> materiaService.updateById(101L, materiaDTOValida));
	}

	@Test
	@DisplayName("existById: Debe verificar si existe por id")
	void debeVerificarExistenciaPorId() {
		when(materiaRepo.existsById(101L)).thenReturn(true);
		assertTrue(materiaService.existById(101L));

		when(materiaRepo.existsById(999L)).thenReturn(false);
		assertFalse(materiaService.existById(999L));
		assertFalse(materiaService.existById(null));
	}

	@Test
	@DisplayName("count: Debe retornar la cantidad total de materias")
	void debeContarMaterias() {
		when(materiaRepo.count()).thenReturn(5L);
		assertEquals(5L, materiaService.count());
	}

	@Test
	@DisplayName("getBySemestre: Debe retornar materias del semestre especificado")
	void debeRetornarMateriasPorSemestre() {
		when(materiaRepo.findBySemestre(2)).thenReturn(List.of(materiaEntidad));
		when(mapper.map(materiaEntidad, MateriaDTO.class)).thenReturn(materiaDTOValida);

		List<MateriaDTO> resultado = materiaService.getBySemestre(2);

		assertNotNull(resultado);
		assertEquals(1, resultado.size());
		assertThrows(DatosInvalidosException.class, () -> materiaService.getBySemestre(0));
		assertThrows(DatosInvalidosException.class, () -> materiaService.getBySemestre(10));
	}
}
