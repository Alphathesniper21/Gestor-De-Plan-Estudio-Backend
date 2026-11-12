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

@ExtendWith(MockitoExtension.class)
class GrupoServiceTest {

	@Mock
	private GrupoRepository grupoRepo;

	@Mock
	private MateriaRepository materiaRepo;

	@Mock
	private SesionRepository sesionRepo;

	@Mock
	private ModelMapper mapper;

	@InjectMocks
	private GrupoService grupoService;

	private GrupoDTO grupoDTOValido;
	private Grupo grupoEntidad;

	@BeforeEach
	void setUp() {
		grupoDTOValido = new GrupoDTO();
		grupoDTOValido.setId(1L);
		grupoDTOValido.setMateriaId(101L);
		grupoDTOValido.setNumeroGrupo(1);
		grupoDTOValido.setCupoPlaneado(40);

		grupoEntidad = new Grupo();
		grupoEntidad.setId(1L);
		grupoEntidad.setMateriaId(101L);
		grupoEntidad.setNumeroGrupo(1);
		grupoEntidad.setCupoPlaneado(40);
	}

	@Test
	@DisplayName("RF-2: Debe crear grupo exitosamente con cupo base de 40")
	void debeCrearGrupoConCupoNormal() {
		when(materiaRepo.existsById(101L)).thenReturn(true);
		when(grupoRepo.existsByMateriaIdAndNumeroGrupo(101L, 1)).thenReturn(false);
		when(mapper.map(grupoDTOValido, Grupo.class)).thenReturn(grupoEntidad);

		int resultado = grupoService.create(grupoDTOValido);

		assertEquals(0, resultado);
		verify(grupoRepo, times(1)).save(grupoEntidad);
	}

	@Test
	@DisplayName("RF-2 Criterio de Aceptacion: Cupo 44 se acepta (40 + 10 %)")
	void debeAceptarCupoMaximoConToleranciaDe44() {
		grupoDTOValido.setCupoPlaneado(44);
		grupoEntidad.setCupoPlaneado(44);

		when(materiaRepo.existsById(101L)).thenReturn(true);
		when(grupoRepo.existsByMateriaIdAndNumeroGrupo(101L, 1)).thenReturn(false);
		when(mapper.map(grupoDTOValido, Grupo.class)).thenReturn(grupoEntidad);

		int resultado = grupoService.create(grupoDTOValido);

		assertEquals(0, resultado);
		verify(grupoRepo, times(1)).save(grupoEntidad);
	}

	@Test
	@DisplayName("RF-2 Criterio de Aceptacion: Cupo 45 se rechaza con CupoExcedidoException")
	void debeRechazarCupoMayorA44() {
		grupoDTOValido.setCupoPlaneado(45);

		assertThrows(CupoExcedidoException.class, () -> grupoService.create(grupoDTOValido));
		verify(grupoRepo, never()).save(any());
	}

	@Test
	@DisplayName("RF-2: Debe lanzar excepcion cuando la materia no existe")
	void debeRechazarCuandoMateriaNoExiste() {
		when(materiaRepo.existsById(101L)).thenReturn(false);

		assertThrows(RecursoNoEncontradoException.class, () -> grupoService.create(grupoDTOValido));
		verify(grupoRepo, never()).save(any());
	}

	@Test
	@DisplayName("RF-2: Debe rechazar grupo duplicado en la misma materia")
	void debeRechazarGrupoDuplicadoEnMateria() {
		when(materiaRepo.existsById(101L)).thenReturn(true);
		when(grupoRepo.existsByMateriaIdAndNumeroGrupo(101L, 1)).thenReturn(true);

		assertThrows(RecursoDuplicadoException.class, () -> grupoService.create(grupoDTOValido));
		verify(grupoRepo, never()).save(any());
	}

	@Test
	@DisplayName("RF-2: Debe rechazar datos nulos o parametros menores o iguales a 0")
	void debeRechazarDatosInvalidos() {
		assertThrows(DatosInvalidosException.class, () -> grupoService.create(null));

		grupoDTOValido.setMateriaId(0);
		assertThrows(DatosInvalidosException.class, () -> grupoService.create(grupoDTOValido));

		grupoDTOValido.setMateriaId(101L);
		grupoDTOValido.setNumeroGrupo(0);
		assertThrows(DatosInvalidosException.class, () -> grupoService.create(grupoDTOValido));

		grupoDTOValido.setNumeroGrupo(1);
		grupoDTOValido.setCupoPlaneado(0);
		assertThrows(DatosInvalidosException.class, () -> grupoService.create(grupoDTOValido));
	}

	@Test
	@DisplayName("getAll: Debe retornar todos los grupos en formato DTO")
	void debeRetornarTodosLosGrupos() {
		when(grupoRepo.findAll()).thenReturn(List.of(grupoEntidad));
		when(mapper.map(grupoEntidad, GrupoDTO.class)).thenReturn(grupoDTOValido);

		List<GrupoDTO> lista = grupoService.getAll();

		assertNotNull(lista);
		assertEquals(1, lista.size());
	}

	@Test
	@DisplayName("deleteById: Debe eliminar el grupo y sus sesiones asociadas en cascada")
	void debeEliminarGrupoYSusSesionesAsociadas() {
		Sesion s1 = new Sesion();
		s1.setId(50L);
		s1.setGrupoId(1);

		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupoEntidad));
		when(sesionRepo.findByGrupoId(1L)).thenReturn(List.of(s1));

		int resultado = grupoService.deleteById(1L);

		assertEquals(0, resultado);
		verify(sesionRepo, times(1)).deleteAll(List.of(s1));
		verify(grupoRepo, times(1)).delete(grupoEntidad);
	}

	@Test
	@DisplayName("deleteById: Debe lanzar excepcion cuando el grupo a eliminar no existe")
	void debeLanzarExcepcionAlEliminarGrupoInexistente() {
		when(grupoRepo.findById(99L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> grupoService.deleteById(99L));
		verify(grupoRepo, never()).delete(any());
	}

	@Test
	@DisplayName("updateById: Debe actualizar numero de grupo y cupo planeado")
	void debeActualizarGrupoExitosamente() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupoEntidad));
		when(grupoRepo.findByMateriaIdAndNumeroGrupo(101L, 2)).thenReturn(Optional.empty());

		GrupoDTO actualizacion = new GrupoDTO();
		actualizacion.setNumeroGrupo(2);
		actualizacion.setCupoPlaneado(35);

		int resultado = grupoService.updateById(1L, actualizacion);

		assertEquals(0, resultado);
		verify(grupoRepo, times(1)).save(grupoEntidad);
	}

	@Test
	@DisplayName("updateById: Debe rechazar actualizacion con cupo mayor a 44")
	void debeRechazarActualizacionConCupoExcedido() {
		when(grupoRepo.findById(1L)).thenReturn(Optional.of(grupoEntidad));

		GrupoDTO actualizacion = new GrupoDTO();
		actualizacion.setNumeroGrupo(1);
		actualizacion.setCupoPlaneado(45);

		assertThrows(CupoExcedidoException.class, () -> grupoService.updateById(1L, actualizacion));
	}

	@Test
	@DisplayName("count y existById: Deben verificar operaciones basicas")
	void debeContarYVerificarExistencia() {
		when(grupoRepo.count()).thenReturn(3L);
		assertEquals(3L, grupoService.count());

		when(grupoRepo.existsById(1L)).thenReturn(true);
		assertTrue(grupoService.existById(1L));

		assertFalse(grupoService.existById(null));
	}

	@Test
	@DisplayName("getByMateriaId: Debe retornar los grupos de la materia")
	void debeRetornarGruposPorMateria() {
		when(grupoRepo.findByMateriaId(101L)).thenReturn(List.of(grupoEntidad));
		when(mapper.map(grupoEntidad, GrupoDTO.class)).thenReturn(grupoDTOValido);

		List<GrupoDTO> lista = grupoService.getByMateriaId(101L);

		assertEquals(1, lista.size());
		assertThrows(DatosInvalidosException.class, () -> grupoService.getByMateriaId(0));
	}
}
