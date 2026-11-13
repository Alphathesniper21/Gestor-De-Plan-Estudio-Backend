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

import co.edu.unbosque.sdgh.dto.GrupoDTO;
import co.edu.unbosque.sdgh.exception.CupoExcedidoException;
import co.edu.unbosque.sdgh.service.GrupoService;

@ExtendWith(MockitoExtension.class)
class GrupoControllerTest {

	private MockMvc mockMvc;

	@Mock
	private GrupoService grupoServ;

	@InjectMocks
	private GrupoController grupoController;

	private GrupoDTO grupoDTO;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(grupoController).build();

		grupoDTO = new GrupoDTO();
		grupoDTO.setId(1L);
		grupoDTO.setMateriaId(101L);
		grupoDTO.setNumeroGrupo(1);
		grupoDTO.setCupoPlaneado(40);
	}

	@Test
	@DisplayName("POST /grupo/crear: Debe responder 201 CREATED al crear grupo exitosamente")
	void debeCrearGrupoExitosamente() throws Exception {
		when(grupoServ.create(any(GrupoDTO.class))).thenReturn(0);

		mockMvc.perform(post("/grupo/crear")
				.param("materiaId", "101")
				.param("numeroGrupo", "1")
				.param("cupoPlaneado", "40"))
				.andExpect(status().isCreated())
				.andExpect(content().string("Grupo creado exitosamente"));
	}

	@Test
	@DisplayName("POST /grupo/crear: Debe responder 400 BAD_REQUEST si el cupo es excedido")
	void debeResponder400AlExcederCupo() throws Exception {
		when(grupoServ.create(any(GrupoDTO.class)))
				.thenThrow(new CupoExcedidoException("Cupo supera limite"));

		mockMvc.perform(post("/grupo/crear")
				.param("materiaId", "101")
				.param("numeroGrupo", "1")
				.param("cupoPlaneado", "50"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("GET /grupo/mostrartodo: Debe responder 202 ACCEPTED cuando hay grupos")
	void debeRetornarGrupos() throws Exception {
		when(grupoServ.getAll()).thenReturn(List.of(grupoDTO));

		mockMvc.perform(get("/grupo/mostrartodo"))
				.andExpect(status().isAccepted());
	}

	@Test
	@DisplayName("GET /grupo/mostrartodo: Debe responder 204 NO_CONTENT cuando no hay grupos")
	void debeResponder204CuandoNoHayGrupos() throws Exception {
		when(grupoServ.getAll()).thenReturn(new ArrayList<>());

		mockMvc.perform(get("/grupo/mostrartodo"))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("GET /grupo/mostrar/{id}: Debe responder 202 ACCEPTED cuando el grupo existe")
	void debeObtenerGrupoPorId() throws Exception {
		when(grupoServ.getById(1L)).thenReturn(grupoDTO);

		mockMvc.perform(get("/grupo/mostrar/1"))
				.andExpect(status().isAccepted());
	}

	@Test
	@DisplayName("PUT /grupo/actualizar: Debe responder 202 ACCEPTED al actualizar correctamente")
	void debeActualizarGrupo() throws Exception {
		when(grupoServ.updateById(eq(1L), any(GrupoDTO.class))).thenReturn(0);

		mockMvc.perform(put("/grupo/actualizar")
				.param("id", "1")
				.param("numeroGrupo", "2")
				.param("cupoPlaneado", "35"))
				.andExpect(status().isAccepted())
				.andExpect(content().string("Grupo actualizado exitosamente"));
	}

	@Test
	@DisplayName("DELETE /grupo/eliminar/{id}: Debe responder 202 ACCEPTED al eliminar grupo")
	void debeEliminarGrupo() throws Exception {
		when(grupoServ.deleteById(1L)).thenReturn(0);

		mockMvc.perform(delete("/grupo/eliminar/1"))
				.andExpect(status().isAccepted())
				.andExpect(content().string("Grupo eliminado exitosamente"));
	}

	@Test
	@DisplayName("GET /grupo/contar: Debe responder 202 ACCEPTED con la cantidad de grupos")
	void debeContarGrupos() throws Exception {
		when(grupoServ.count()).thenReturn(3L);

		mockMvc.perform(get("/grupo/contar"))
				.andExpect(status().isAccepted())
				.andExpect(content().string("3"));
	}

	@Test
	@DisplayName("GET /grupo/existe/{id}: Debe responder 202 si existe")
	void debeVerificarExistencia() throws Exception {
		when(grupoServ.existById(1L)).thenReturn(true);

		mockMvc.perform(get("/grupo/existe/1"))
				.andExpect(status().isAccepted())
				.andExpect(content().string("true"));
	}
}
