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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.sdgh.dto.UsuarioDTO;
import co.edu.unbosque.sdgh.entity.RolUsuario;
import co.edu.unbosque.sdgh.entity.Usuario;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.UsuarioRepository;

/**
 * Pruebas unitarias para la clase {@link UsuarioService}.
 * <p>
 * Verifica la lógica de negocio del servicio de usuarios, incluyendo el
 * registro con hash BCrypt de contraseñas (RNF-4), validaciones de formato de
 * nombre, correo electrónico, nombre de usuario y fortaleza de contraseña,
 * exclusión de contraseñas en los DTOs retornados (RNF-7), actualización de
 * datos (incluyendo el cambio de {@code username} y contraseña opcional) y
 * eliminación.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see UsuarioService
 * @see CRUDOperation
 */
class UsuarioServiceTest {

	/**
	 * Mock del repositorio de usuarios.
	 */
	@Mock
	private UsuarioRepository usuarioRepo;

	/**
	 * Codificador de contraseñas BCrypt real (utilidad criptográfica pura).
	 */
	private PasswordEncoder passwordEncoder;

	/**
	 * Mapper para la conversión entre entidades y DTOs.
	 */
	private ModelMapper mapper;

	/**
	 * Instancia del servicio bajo prueba.
	 */
	private UsuarioService service;

	/**
	 * Entidad de usuario de referencia utilizada en los tests.
	 */
	private Usuario usuario;

	/**
	 * DTO de usuario de referencia utilizado en los tests.
	 */
	private UsuarioDTO dto;

	/**
	 * Inicializa los mocks, inyecta las dependencias en el servicio por reflexión
	 * y prepara los objetos de prueba antes de cada caso.
	 */
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mapper = new ModelMapper();
		passwordEncoder = new BCryptPasswordEncoder();
		service = new UsuarioService();
		ReflectionTestUtils.setField(service, "usuarioRepo", usuarioRepo);
		ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
		ReflectionTestUtils.setField(service, "mapper", mapper);

		usuario = new Usuario();
		usuario.setId(1L);
		usuario.setNombre("Martin Pena");
		usuario.setCorreo("martin@unbosque.edu.co");
		usuario.setUsername("martinp");
		usuario.setPassword(passwordEncoder.encode("ClaveSegura123"));
		usuario.setRol(RolUsuario.GESTOR);

		dto = new UsuarioDTO();
		dto.setId(1L);
		dto.setNombre("Martin Pena");
		dto.setCorreo("martin@unbosque.edu.co");
		dto.setUsername("martinp");
		dto.setPassword("ClaveSegura123");
		dto.setRol(RolUsuario.GESTOR);
	}

	// ─── CREATE ───────────────────────────────────────────────────────────────

	/**
	 * Verifica que la creación de un usuario es exitosa y almacena la contraseña
	 * codificada con hash BCrypt en la base de datos (RNF-4).
	 */
	@Test
	void testCreate_Exitoso() {
		when(usuarioRepo.existsByCorreo("martin@unbosque.edu.co")).thenReturn(false);
		when(usuarioRepo.existsByUsername("martinp")).thenReturn(false);
		when(usuarioRepo.save(any(Usuario.class))).thenReturn(usuario);

		int resultado = service.create(dto);

		assertEquals(0, resultado);
		verify(usuarioRepo, times(1)).save(argThat(u -> passwordEncoder.matches("ClaveSegura123", u.getPassword())));
	}

	/**
	 * Verifica que se lanza {@link DatosInvalidosException} cuando los datos del
	 * usuario o el nombre son nulos, vacíos o contienen caracteres numéricos.
	 */
	@Test
	void testCreate_LanzaExcepcionNombreInvalido() {
		assertThrows(DatosInvalidosException.class, () -> service.create(null));

		dto.setNombre("   ");
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setNombre("Martin123");
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		verify(usuarioRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link DatosInvalidosException} cuando el correo no
	 * cumple el formato válido o cuando el username tiene menos de 4 caracteres.
	 */
	@Test
	void testCreate_LanzaExcepcionCorreoOUsernameInvalido() {
		dto.setCorreo("correo-sin-formato");
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setCorreo("martin@unbosque.edu.co");
		dto.setUsername("abc"); // Menor a 4 caracteres
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setUsername("usuario con espacio");
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		verify(usuarioRepo, never()).save(any());
	}

	/**
	 * Verifica que se lanza {@link DatosInvalidosException} cuando la contraseña es
	 * menor a 8 caracteres o no cumple con mayúscula, minúscula y número.
	 */
	@Test
	void testCreate_LanzaExcepcionContrasenaDebil() {
		dto.setPassword("Ab1");
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setPassword("solominuscula123");
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setPassword("SOLOMAYUSCULA123");
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));

		dto.setPassword("SinNumeroValido");
		assertThrows(DatosInvalidosException.class, () -> service.create(dto));
	}

	/**
	 * Verifica que se lanza {@link RecursoDuplicadoException} cuando el correo o el
	 * nombre de usuario ya existen en el repositorio.
	 */
	@Test
	void testCreate_LanzaExcepcionUsuarioDuplicado() {
		when(usuarioRepo.existsByCorreo("martin@unbosque.edu.co")).thenReturn(true);
		assertThrows(RecursoDuplicadoException.class, () -> service.create(dto));

		when(usuarioRepo.existsByCorreo("martin@unbosque.edu.co")).thenReturn(false);
		when(usuarioRepo.existsByUsername("martinp")).thenReturn(true);
		assertThrows(RecursoDuplicadoException.class, () -> service.create(dto));

		verify(usuarioRepo, never()).save(any());
	}

	// ─── GET ALL, GET BY ID, USERNAME Y CORREO ────────────────────────────────

	/**
	 * Verifica que {@code getAll} retorna la lista de usuarios y garantiza que
	 * ningún DTO exponga la contraseña del usuario (RNF-4, RNF-7).
	 */
	@Test
	void testGetAll_ExitosoSinContrasena() {
		when(usuarioRepo.findAll()).thenReturn(List.of(usuario));

		List<UsuarioDTO> lista = service.getAll();

		assertNotNull(lista);
		assertEquals(1, lista.size());
		assertNull(lista.get(0).getPassword(), "La contrasena nunca debe exponerse en los DTOs");
	}

	/**
	 * Verifica las consultas por identificador, username y correo electrónico.
	 */
	@Test
	void testGetByIdUsernameYCorreo_ExitosoYValidaciones() {
		when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));
		when(usuarioRepo.findByUsername("martinp")).thenReturn(Optional.of(usuario));
		when(usuarioRepo.findByCorreo("martin@unbosque.edu.co")).thenReturn(Optional.of(usuario));

		assertNull(service.getById(1L).getPassword());
		assertEquals("martinp", service.getByUsername("martinp").getUsername());
		assertEquals("martin@unbosque.edu.co", service.getByCorreo("martin@unbosque.edu.co").getCorreo());

		assertThrows(DatosInvalidosException.class, () -> service.getById(null));
		assertThrows(DatosInvalidosException.class, () -> service.getByUsername(" "));
		assertThrows(DatosInvalidosException.class, () -> service.getByCorreo(" "));
	}

	// ─── UPDATE BY ID ─────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code updateById} actualiza el nombre, correo, rol y
	 * {@code username} del usuario existente, y si se envía nueva contraseña la
	 * almacena con hash BCrypt.
	 */
	@Test
	void testUpdateById_ExitosoConCambioDeUsernameYPassword() {
		when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));
		when(usuarioRepo.findByCorreo("martin.nuevo@unbosque.edu.co")).thenReturn(Optional.empty());
		when(usuarioRepo.save(any(Usuario.class))).thenReturn(usuario);

		UsuarioDTO actualizacion = new UsuarioDTO();
		actualizacion.setNombre("Martin Actualizado");
		actualizacion.setCorreo("martin.nuevo@unbosque.edu.co");
		actualizacion.setUsername("martin_actualizado");
		actualizacion.setRol(RolUsuario.CONSULTANTE);
		actualizacion.setPassword("NuevaClave99");

		int resultado = service.updateById(1L, actualizacion);

		assertEquals(0, resultado);
		assertEquals("Martin Actualizado", usuario.getNombre());
		assertEquals("martin.nuevo@unbosque.edu.co", usuario.getCorreo());
		assertEquals("martin_actualizado", usuario.getUsername());
		assertEquals(RolUsuario.CONSULTANTE, usuario.getRol());
		assertTrue(passwordEncoder.matches("NuevaClave99", usuario.getPassword()));
		verify(usuarioRepo, times(1)).save(usuario);
	}

	/**
	 * Verifica que {@code updateById} lanza {@link RecursoDuplicadoException} si el
	 * correo nuevo ya pertenece a otro usuario distinto.
	 */
	@Test
	void testUpdateById_LanzaExcepcionCorreoDuplicadoOtroUsuario() {
		Usuario otro = new Usuario();
		otro.setId(2L);
		otro.setCorreo("ocupado@unbosque.edu.co");

		when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));
		when(usuarioRepo.findByCorreo("ocupado@unbosque.edu.co")).thenReturn(Optional.of(otro));

		UsuarioDTO actualizacion = new UsuarioDTO();
		actualizacion.setNombre("Martin Pena");
		actualizacion.setCorreo("ocupado@unbosque.edu.co");
		actualizacion.setUsername("martinp");
		actualizacion.setRol(RolUsuario.GESTOR);

		assertThrows(RecursoDuplicadoException.class, () -> service.updateById(1L, actualizacion));
	}

	// ─── DELETE BY ID, COUNT Y EXIST BY ID ────────────────────────────────────

	/**
	 * Verifica que {@code deleteById} elimina correctamente al usuario de la base
	 * de datos o lanza {@link RecursoNoEncontradoException} cuando no existe.
	 */
	@Test
	void testDeleteById_ExitosoYNoEncontrado() {
		when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));
		assertEquals(0, service.deleteById(1L));
		verify(usuarioRepo, times(1)).delete(usuario);

		when(usuarioRepo.findById(999L)).thenReturn(Optional.empty());
		assertThrows(RecursoNoEncontradoException.class, () -> service.deleteById(999L));
	}

	/**
	 * Verifica que {@code count} y {@code existById} devuelven los resultados
	 * esperados.
	 */
	@Test
	void testCountYExistById_Exitoso() {
		when(usuarioRepo.count()).thenReturn(3L);
		assertEquals(3L, service.count());

		when(usuarioRepo.existsById(1L)).thenReturn(true);
		assertTrue(service.existById(1L));
		assertFalse(service.existById(null));
	}
}
