package co.edu.unbosque.sdgh.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.edu.unbosque.sdgh.dto.MateriaDTO;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.service.MateriaService;

@ExtendWith(MockitoExtension.class)
class MateriaControllerTest {

	private MockMvc mockMvc;

	@Mock
	private MateriaService materiaService;

	@InjectMocks
	private MateriaController materiaController;

	private MateriaDTO materiaDTO;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(materiaController).build();

		materiaDTO = new MateriaDTO();
		materiaDTO.setId(101L);
		materiaDTO.setNombre("Programacion I");
		materiaDTO.setSemestre(2);
		materiaDTO.setNcreditos(3);
		materiaDTO.setHorasDeLaMateria(4);
		materiaDTO.setCorrelativa(false);
	}

	@Test
	@DisplayName("POST /materia/crear: Debe responder 201 CREATED al crear exitosamente")
	void debeCrearMateriaExitosamente() throws Exception {
		when(materiaService.create(any(MateriaDTO.class))).thenReturn(0);

		mockMvc.perform(post("/materia/crear")
				.param("nombre", "Programacion I")
				.param("semestre", "2")
				.param("ncreditos", "3")
				.param("horasDeLaMateria", "4")
				.param("correlativa", "false"))
				.andExpect(status().isCreated())
				.andExpect(content().string("Materia creada exitosamente"));
	}

	@Test
	@DisplayName("POST /materia/crear: Debe responder 400 BAD_REQUEST si hay datos invalidos o nombre duplicado")
	void debeResponder400AlCrearConDatosInvalidos() throws Exception {
		when(materiaService.create(any(MateriaDTO.class)))
				.thenThrow(new DatosInvalidosException("Semestre fuera de rango"));

		mockMvc.perform(post("/materia/crear")
				.param("nombre", "Programacion I")
				.param("semestre", "15")
				.param("ncreditos", "3")
				.param("horasDeLaMateria", "4")
				.param("correlativa", "false"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("GET /materia/mostrartodo: Debe responder 202 ACCEPTED cuando hay materias")
	void debeRetornar202ConListaDeMaterias() throws Exception {
		when(materiaService.getAll()).thenReturn(List.of(materiaDTO));

		mockMvc.perform(get("/materia/mostrartodo"))
				.andExpect(status().isAccepted());
	}

	@Test
	@DisplayName("GET /materia/mostrartodo: Debe responder 204 NO_CONTENT cuando la lista esta vacia")
	void debeRetornar204CuandoNoHayMaterias() throws Exception {
		when(materiaService.getAll()).thenReturn(new ArrayList<>());

		mockMvc.perform(get("/materia/mostrartodo"))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("DELETE /materia/eliminar/{codigo}: Debe responder 202 ACCEPTED al eliminar exitosamente")
	void debeEliminarMateriaExitosamente() throws Exception {
		when(materiaService.deleteById(101L)).thenReturn(0);

		mockMvc.perform(delete("/materia/eliminar/101"))
				.andExpect(status().isAccepted())
				.andExpect(content().string("Materia eliminada exitosamente"));
	}

	@Test
	@DisplayName("DELETE /materia/eliminar/{codigo}: Debe responder 404 NOT_FOUND si el servicio retorna distinto de 0")
	void debeResponder404AlEliminarInexistente() throws Exception {
		when(materiaService.deleteById(999L)).thenReturn(1);

		mockMvc.perform(delete("/materia/eliminar/999"))
				.andExpect(status().isNotFound())
				.andExpect(content().string("Error al eliminar"));
	}

	@Test
	@DisplayName("GET /materia/mostrar/{id}: Debe responder 202 ACCEPTED con los datos de la materia")
	void debeBuscarMateriaPorId() throws Exception {
		when(materiaService.getById(101L)).thenReturn(materiaDTO);

		mockMvc.perform(get("/materia/mostrar/101"))
				.andExpect(status().isAccepted());
	}

	@Test
	@DisplayName("GET /materia/contar: Debe responder 202 ACCEPTED con el total")
	void debeContarMaterias() throws Exception {
		when(materiaService.count()).thenReturn(5L);

		mockMvc.perform(get("/materia/contar"))
				.andExpect(status().isAccepted())
				.andExpect(content().string("5"));
	}
}
