package co.edu.unbosque.sdgh.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.dto.UsuarioDTO;
import co.edu.unbosque.sdgh.entity.Usuario;
import co.edu.unbosque.sdgh.exception.DatosInvalidosException;
import co.edu.unbosque.sdgh.exception.RecursoDuplicadoException;
import co.edu.unbosque.sdgh.exception.RecursoNoEncontradoException;
import co.edu.unbosque.sdgh.repository.UsuarioRepository;

/**
 * Servicio que gestiona la lógica de negocio relacionada con los usuarios del
 * sistema, incluyendo su registro, consulta, actualización y eliminación.
 * Las contraseñas se almacenan mediante un proceso de hash y nunca se incluyen
 * en los DTO devueltos. Las validaciones se realizan al inicio de cada método
 * y utilizan excepciones personalizadas para informar los errores.
 *
 * @author Sebastian Toyo
 */
@Service
public class UsuarioService implements CRUDOperation<UsuarioDTO> {

	/**
	 * Repositorio encargado de realizar las operaciones de persistencia de los
	 * usuarios.
	 */
	@Autowired
	private UsuarioRepository usuarioRepo;

	/**
	 * Codificador encargado de generar el hash de las contraseñas antes de
	 * almacenarlas.
	 */
	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Mapeador de objetos utilizado para convertir entre entidades {@link Usuario}
	 * y objetos {@link UsuarioDTO}.
	 */
	@Autowired
	private ModelMapper mapper;

//	/**
//	 * Constante de rol de gestor
//	 */
//	private final String ROL_GESTOR= "GESTOR";
//	
//	/**
//	 * Constante de rol de consulta
//	 */
//	private final String ROL_CONSULTA= "CONSULTA";

	/**
	 * Constructor vacío de la clase UsuarioService.
	 */
	public UsuarioService() {
	}

	/**
	 * Registra un nuevo usuario validando sus datos y la unicidad del correo
	 * electrónico y del nombre de usuario. La contraseña se almacena mediante
	 * un proceso de hash.
	 *
	 * @param data DTO con los datos del usuario que se desea registrar.
	 * @return 0 si el registro se realiza correctamente.
	 * @throws DatosInvalidosException si los datos son nulos, incompletos o
	 *         presentan un formato inválido.
	 * @throws RecursoDuplicadoException si el correo electrónico o el nombre
	 *         de usuario ya se encuentran registrados.
	 */
	@Override
	public int create(UsuarioDTO data) {

		String regexNombre = "[\\p{L} \\-]+";
		String regexCorreo = "^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$";
		String regexUsername = "[a-zA-Z0-9._\\-]+";

		if (data == null) {
			throw new DatosInvalidosException("Los datos del usuario no pueden ser nulos");
		}
		if (data.getNombre() == null || data.getNombre().isBlank()) {
			throw new DatosInvalidosException("El nombre no puede estar vacio");
		}
		if (!data.getNombre().trim().matches(regexNombre)) {
			throw new DatosInvalidosException("El nombre solo puede contener letras, espacios y guiones");
		}
		if (data.getCorreo() == null || data.getCorreo().isBlank()) {
			throw new DatosInvalidosException("El correo no puede estar vacio");
		}
		if (!data.getCorreo().trim().matches(regexCorreo)) {
			throw new DatosInvalidosException("El correo no tiene un formato valido (ej: usuario@dominio.com)");
		}
		if (data.getRol() == null) {
			throw new DatosInvalidosException("El rol no puede estar vacio");
		}
		if (data.getUsername() == null || data.getUsername().isBlank()) {
			throw new DatosInvalidosException("El nombre de usuario no puede estar vacio");
		}
		if (data.getUsername().trim().length() < 4) {
			throw new DatosInvalidosException("El nombre de usuario debe tener minimo 4 caracteres");
		}
		if (!data.getUsername().trim().matches(regexUsername)) {
			throw new DatosInvalidosException(
					"El nombre de usuario solo puede contener letras, numeros, puntos, guiones y guiones bajos");
		}
		if (data.getPassword() == null || data.getPassword().isBlank()) {
			throw new DatosInvalidosException("La contrasena no puede estar vacia");
		}
		if (data.getPassword().length() < 8) {
			throw new DatosInvalidosException("La contrasena debe tener minimo 8 caracteres");
		}
		if (!data.getPassword().matches(".*[A-Z].*")) {
			throw new DatosInvalidosException("La contrasena debe tener al menos una letra mayuscula");
		}
		if (!data.getPassword().matches(".*[a-z].*")) {
			throw new DatosInvalidosException("La contrasena debe tener al menos una letra minuscula");
		}
		if (!data.getPassword().matches(".*[0-9].*")) {
			throw new DatosInvalidosException("La contrasena debe tener al menos un numero");
		}

		String correo = data.getCorreo().trim();
		String username = data.getUsername().trim();

		if (usuarioRepo.existsByCorreo(correo)) {
			throw new RecursoDuplicadoException("Ya existe un usuario con el correo " + correo);
		}
		if (usuarioRepo.existsByUsername(username)) {
			throw new RecursoDuplicadoException("Ya existe un usuario con el nombre de usuario " + username);
		}

		Usuario entity = mapper.map(data, Usuario.class);
		entity.setNombre(data.getNombre().trim());
		entity.setCorreo(correo);
		entity.setUsername(username);
		entity.setRol(data.getRol());
		entity.setPassword(passwordEncoder.encode(data.getPassword()));
		usuarioRepo.save(entity);
		return 0;
	}

	/**
	 * Obtiene todos los usuarios registrados en el sistema. Las contraseñas se
	 * excluyen de los DTO devueltos.
	 *
	 * @return Lista de objetos {@link UsuarioDTO} correspondientes a los usuarios
	 *         registrados, sin incluir sus contraseñas.
	 */
	@Override
	public List<UsuarioDTO> getAll() {

		List<Usuario> entityList = (List<Usuario>) usuarioRepo.findAll();
		List<UsuarioDTO> dtoList = new ArrayList<>();

		entityList.forEach((entidad) -> {

			UsuarioDTO dto = mapper.map(entidad, UsuarioDTO.class);
			dto.setPassword(null);
			dtoList.add(dto);
		});

		return dtoList;
	}

	/**
	 * Elimina un usuario a partir de su identificador.
	 *
	 * @param id Identificador del usuario que se desea eliminar.
	 * @return 0 si la eliminación se realiza correctamente.
	 * @throws DatosInvalidosException si el identificador es nulo.
	 * @throws RecursoNoEncontradoException si no existe un usuario con el
	 *         identificador indicado.
	 */
	@Override
	public int deleteById(Long id) {

		if (id == null) {
			throw new DatosInvalidosException("El codigo del usuario no puede ser nulo");
		}

		Usuario encontrado = usuarioRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con el codigo " + id));

		usuarioRepo.delete(encontrado);
		return 0;
	}

	/**
	 * Actualiza el nombre, correo electrónico y rol de un usuario. Si se
	 * proporciona una nueva contraseña, esta se valida y se almacena mediante
	 * un proceso de hash. Si la contraseña viene vacía, se conserva la actual.
	 * El nombre de usuario utilizado para iniciar sesión no se modifica.
	 *
	 * @param id Identificador del usuario que se desea actualizar.
	 * @param newData DTO con los nuevos datos del usuario.
	 * @return 0 si la actualización se realiza correctamente.
	 * @throws DatosInvalidosException si el identificador es nulo o los nuevos
	 *         datos son nulos, incompletos o presentan un formato inválido.
	 * @throws RecursoNoEncontradoException si no existe un usuario con el
	 *         identificador indicado.
	 * @throws RecursoDuplicadoException si el correo electrónico pertenece a
	 *         otro usuario.
	 */
	@Override
	public int updateById(Long id, UsuarioDTO newData) {

		String regexNombre = "[\\p{L} \\-]+";
		String regexCorreo = "^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$";

		if (id == null) {
			throw new DatosInvalidosException("El codigo del usuario no puede ser nulo");
		}

		Usuario temp = usuarioRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con el codigo " + id));

		if (newData == null) {
			throw new DatosInvalidosException("Los datos del usuario no pueden ser nulos");
		}
		if (newData.getNombre() == null || newData.getNombre().isBlank()) {
			throw new DatosInvalidosException("El nombre no puede estar vacio");
		}
		if (!newData.getNombre().trim().matches(regexNombre)) {
			throw new DatosInvalidosException("El nombre solo puede contener letras, espacios y guiones");
		}
		if (newData.getCorreo() == null || newData.getCorreo().isBlank()) {
			throw new DatosInvalidosException("El correo no puede estar vacio");
		}
		if (!newData.getCorreo().trim().matches(regexCorreo)) {
			throw new DatosInvalidosException("El correo no tiene un formato valido (ej: usuario@dominio.com)");
		}

		boolean cambiaContrasena = newData.getPassword() != null && !newData.getPassword().isBlank();

		if (cambiaContrasena) {
			if (newData.getPassword().length() < 8) {
				throw new DatosInvalidosException("La contrasena debe tener minimo 8 caracteres");
			}
			if (!newData.getPassword().matches(".*[A-Z].*")) {
				throw new DatosInvalidosException("La contrasena debe tener al menos una letra mayuscula");
			}
			if (!newData.getPassword().matches(".*[a-z].*")) {
				throw new DatosInvalidosException("La contrasena debe tener al menos una letra minuscula");
			}
			if (!newData.getPassword().matches(".*[0-9].*")) {
				throw new DatosInvalidosException("La contrasena debe tener al menos un numero");
			}
		}

		String correo = newData.getCorreo().trim();
		Optional<Usuario> porCorreo = usuarioRepo.findByCorreo(correo);

		if (porCorreo.isPresent() && porCorreo.get().getId() != id.longValue()) {
			throw new RecursoDuplicadoException("El correo " + correo + " ya pertenece a otro usuario");
		}

		temp.setNombre(newData.getNombre().trim());
		temp.setCorreo(correo);
		temp.setRol(newData.getRol());

		if (cambiaContrasena) {
			temp.setPassword(passwordEncoder.encode(newData.getPassword()));
		}

		usuarioRepo.save(temp);
		return 0;
	}

	/**
	 * Cuenta la cantidad total de usuarios registrados en el sistema.
	 *
	 * @return Número total de usuarios registrados.
	 */
	@Override
	public long count() {

		return usuarioRepo.count();
	}

	/**
	 * Verifica si existe un usuario con el identificador indicado.
	 *
	 * @param id Identificador del usuario que se desea consultar.
	 * @return true si existe un usuario con ese identificador, false en caso
	 *         contrario o si el identificador es nulo.
	 */
	@Override
	public boolean existById(Long id) {

		return id != null && usuarioRepo.existsById(id);
	}

	/**
	 * Busca un usuario a partir de su identificador. La contraseña se excluye
	 * del DTO devuelto.
	 *
	 * @param id Identificador del usuario que se desea consultar.
	 * @return Objeto {@link UsuarioDTO} correspondiente al usuario encontrado,
	 *         sin incluir su contraseña.
	 * @throws DatosInvalidosException si el identificador es nulo.
	 * @throws RecursoNoEncontradoException si no existe un usuario con el
	 *         identificador indicado.
	 */
	public UsuarioDTO getById(Long id) {

		if (id == null) {
			throw new DatosInvalidosException("El codigo del usuario no puede ser nulo");
		}

		Usuario encontrado = usuarioRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con el codigo " + id));

		UsuarioDTO dto = mapper.map(encontrado, UsuarioDTO.class);
		dto.setPassword(null);
		return dto;
	}

	/**
	 * Busca un usuario por su nombre de usuario utilizado para iniciar sesión.
	 *
	 * @param username Nombre de usuario que se desea consultar.
	 * @return Objeto {@link UsuarioDTO} correspondiente al usuario encontrado,
	 *         sin incluir su contraseña.
	 * @throws DatosInvalidosException si el nombre de usuario es nulo o está vacío.
	 * @throws RecursoNoEncontradoException si no existe un usuario con el nombre
	 *         de usuario indicado.
	 */
	public UsuarioDTO getByUsername(String username) {

		if (username == null || username.isBlank()) {
			throw new DatosInvalidosException("El nombre de usuario no puede estar vacio");
		}

		Usuario encontrado = usuarioRepo.findByUsername(username.trim()).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe un usuario con el nombre " + username.trim()));

		UsuarioDTO dto = mapper.map(encontrado, UsuarioDTO.class);
		dto.setPassword(null);
		return dto;
	}

	/**
	 * Busca un usuario a partir de su correo electrónico.
	 *
	 * @param correo Correo electrónico del usuario que se desea consultar.
	 * @return Objeto {@link UsuarioDTO} correspondiente al usuario encontrado,
	 *         sin incluir su contraseña.
	 * @throws DatosInvalidosException si el correo es nulo o está vacío.
	 * @throws RecursoNoEncontradoException si no existe un usuario con el correo
	 *         indicado.
	 */
	public UsuarioDTO getByCorreo(String correo) {

		if (correo == null || correo.isBlank()) {
			throw new DatosInvalidosException("El correo no puede estar vacio");
		}

		Usuario encontrado = usuarioRepo.findByCorreo(correo.trim()).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe un usuario con el correo " + correo.trim()));

		UsuarioDTO dto = mapper.map(encontrado, UsuarioDTO.class);
		dto.setPassword(null);
		return dto;
	}

}