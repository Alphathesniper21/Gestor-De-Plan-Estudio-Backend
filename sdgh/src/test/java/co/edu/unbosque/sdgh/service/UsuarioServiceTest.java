package co.edu.unbosque.sdgh.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.sdgh.dto.UsuarioDTO;
import co.edu.unbosque.sdgh.entity.RolUsuario;
import co.edu.unbosque.sdgh.entity.Usuario;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

	@Mock
	private UsuarioRepository usuarioRepo;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private ModelMapper mapper;

	@InjectMocks
	private UsuarioService usuarioService;

	private UsuarioDTO usuarioDTOValido;
	private Usuario usuarioEntidad;

	@BeforeEach
	void setUp() {
		usuarioDTOValido = new UsuarioDTO();
		usuarioDTOValido.setId(1L);
		usuarioDTOValido.setNombre("Martin Gonzalez");
		usuarioDTOValido.setCorreo("martin@unbosque.edu.co");
		usuarioDTOValido.setUsername("marting");
		usuarioDTOValido.setPassword("ClaveSegura123");
		usuarioDTOValido.setRol(RolUsuario.GESTOR);

		usuarioEntidad = new Usuario();
		usuarioEntidad.setId(1L);
		usuarioEntidad.setNombre("Martin Gonzalez");
		usuarioEntidad.setCorreo("martin@unbosque.edu.co");
		usuarioEntidad.setUsername("marting");
		usuarioEntidad.setPassword("hashedPassword");
		usuarioEntidad.setRol(RolUsuario.GESTOR);
	}

	@Test
	@DisplayName("create: Debe registrar un usuario exitosamente con contrasena encriptada")
	void debeRegistrarUsuarioExitosamente() {
		when(usuarioRepo.existsByCorreo("martin@unbosque.edu.co")).thenReturn(false);
		when(usuarioRepo.existsByUsername("marting")).thenReturn(false);
		when(passwordEncoder.encode("ClaveSegura123")).thenReturn("hashedPassword");
		when(mapper.map(usuarioDTOValido, Usuario.class)).thenReturn(usuarioEntidad);

		int resultado = usuarioService.create(usuarioDTOValido);

		assertEquals(0, resultado);
		verify(usuarioRepo, times(1)).save(usuarioEntidad);
	}

	@Test
	@DisplayName("create: Debe rechazar datos nulos o nombres vacios o invalidos")
	void debeRechazarDatosONombresInvalidos() {
		assertThrows(DatosInvalidosException.class, () -> usuarioService.create(null));

		usuarioDTOValido.setNombre("   ");
		assertThrows(DatosInvalidosException.class, () -> usuarioService.create(usuarioDTOValido));

		usuarioDTOValido.setNombre("Martin123"); // Caracteres no permitidos en nombre
		assertThrows(DatosInvalidosException.class, () -> usuarioService.create(usuarioDTOValido));
	}

	@Test
	@DisplayName("create: Debe rechazar formato de correo invalido")
	void debeRechazarCorreoInvalido() {
		usuarioDTOValido.setCorreo("correo-sin-arroba");
		assertThrows(DatosInvalidosException.class, () -> usuarioService.create(usuarioDTOValido));
	}

	@Test
	@DisplayName("create: Debe rechazar contrasenas debiles (sin mayuscula, sin minuscula o sin numero)")
	void debeRechazarContrasenasDebiles() {
		usuarioDTOValido.setPassword("corta");
		assertThrows(DatosInvalidosException.class, () -> usuarioService.create(usuarioDTOValido));

		usuarioDTOValido.setPassword("solominusculas123");
		assertThrows(DatosInvalidosException.class, () -> usuarioService.create(usuarioDTOValido));

		usuarioDTOValido.setPassword("SOLOMAYUSCULAS123");
		assertThrows(DatosInvalidosException.class, () -> usuarioService.create(usuarioDTOValido));

		usuarioDTOValido.setPassword("SinNumerosValidos");
		assertThrows(DatosInvalidosException.class, () -> usuarioService.create(usuarioDTOValido));
	}

	@Test
	@DisplayName("create: Debe rechazar correo o username duplicado")
	void debeRechazarUsuarioDuplicado() {
		when(usuarioRepo.existsByCorreo("martin@unbosque.edu.co")).thenReturn(true);
		assertThrows(RecursoDuplicadoException.class, () -> usuarioService.create(usuarioDTOValido));

		when(usuarioRepo.existsByCorreo("martin@unbosque.edu.co")).thenReturn(false);
		when(usuarioRepo.existsByUsername("marting")).thenReturn(true);
		assertThrows(RecursoDuplicadoException.class, () -> usuarioService.create(usuarioDTOValido));

		verify(usuarioRepo, never()).save(any());
	}

	@Test
	@DisplayName("getAll: Debe retornar usuarios sin exponer sus contrasenas")
	void debeRetornarUsuariosSinContrasena() {
		when(usuarioRepo.findAll()).thenReturn(List.of(usuarioEntidad));
		when(mapper.map(usuarioEntidad, UsuarioDTO.class)).thenReturn(usuarioDTOValido);

		List<UsuarioDTO> resultado = usuarioService.getAll();

		assertNotNull(resultado);
		assertEquals(1, resultado.size());
		assertNull(resultado.get(0).getPassword(), "La contrasena no debe retornarse");
	}

	@Test
	@DisplayName("deleteById: Debe eliminar usuario existente")
	void debeEliminarUsuarioExitosamente() {
		when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuarioEntidad));

		int resultado = usuarioService.deleteById(1L);

		assertEquals(0, resultado);
		verify(usuarioRepo, times(1)).delete(usuarioEntidad);
	}

	@Test
	@DisplayName("deleteById: Debe lanzar excepcion cuando el usuario no existe")
	void debeLanzarExcepcionAlEliminarInexistente() {
		when(usuarioRepo.findById(999L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> usuarioService.deleteById(999L));
	}

	@Test
	@DisplayName("updateById: Debe actualizar datos de usuario")
	void debeActualizarUsuarioExitosamente() {
		when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuarioEntidad));
		when(usuarioRepo.findByCorreo("martin@unbosque.edu.co")).thenReturn(Optional.of(usuarioEntidad));

		UsuarioDTO actualizacion = new UsuarioDTO();
		actualizacion.setNombre("Martin Modificado");
		actualizacion.setCorreo("martin@unbosque.edu.co");

		int resultado = usuarioService.updateById(1L, actualizacion);

		assertEquals(0, resultado);
		verify(usuarioRepo, times(1)).save(usuarioEntidad);
	}

	@Test
	@DisplayName("count y existById: Deben verificar operaciones basicas")
	void debeContarYVerificarExistencia() {
		when(usuarioRepo.count()).thenReturn(2L);
		assertEquals(2L, usuarioService.count());

		when(usuarioRepo.existsById(1L)).thenReturn(true);
		assertTrue(usuarioService.existById(1L));
		assertFalse(usuarioService.existById(null));
	}
}
