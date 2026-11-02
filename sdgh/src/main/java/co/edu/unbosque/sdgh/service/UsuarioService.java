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
 * Servicio que gestiona la logica de negocio de los usuarios del sistema:
 * registro, consulta, actualizacion y eliminacion. Las contrasenas se
 * almacenan con hash (RNF de seguridad). Las validaciones se hacen al inicio
 * de cada metodo y lanzan excepciones personalizadas que son capturadas en el
 * controlador. La conversion entre entidad y DTO se hace directamente con
 * ModelMapper.
 */
@Service
public class UsuarioService implements CRUDOperation<UsuarioDTO> {

	/**
	 * Rol que puede armar el horario.
	 */
	public static final String ROL_GESTOR = "GESTOR";

	/**
	 * Rol que solo consulta el horario.
	 */
	public static final String ROL_CONSULTA = "CONSULTA";

	/**
	 * Repositorio de usuarios.
	 */
	@Autowired
	private UsuarioRepository usuarioRepo;

	/**
	 * Codificador de contrasenas para el hash.
	 */
	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Mapeador de objetos para la conversion entre entidades y DTO.
	 */
	@Autowired
	private ModelMapper mapper;

	/**
	 * Constructor vacio de la clase UsuarioService.
	 */
	public UsuarioService() {
	}

	/**
	 * Registra un nuevo usuario validando datos y unicidad de codigo, correo y
	 * nombre de usuario. La contrasena se guarda con hash.
	 *
	 * @param data DTO con los datos del usuario a registrar.
	 * @return 0 si se registro con exito.
	 * @throws DatosInvalidosException si los datos son nulos, incompletos o con
	 *         formato invalido (incluye nombre de usuario y contrasena).
	 * @throws RecursoDuplicadoException si el codigo, el correo o el nombre de
	 *         usuario ya existen.
	 */
	@Override
	public int create(UsuarioDTO data) {

		if (data == null) {
			throw new DatosInvalidosException("Los datos del usuario no pueden ser nulos");
		}
		if (data.getId() <= 0) {
			throw new DatosInvalidosException("El codigo del usuario debe ser un numero positivo");
		}
		if (data.getNombre() == null || data.getNombre().trim().isEmpty()) {
			throw new DatosInvalidosException("El nombre no puede estar vacio");
		}
		if (data.getCorreo() == null || data.getCorreo().trim().isEmpty()) {
			throw new DatosInvalidosException("El correo no puede estar vacio");
		}
		if (data.getRol() == null || data.getRol().trim().isEmpty()) {
			throw new DatosInvalidosException("El rol no puede estar vacio");
		}
		if (!data.getNombre().trim().matches("[\\p{L} \\-]+")) {
			throw new DatosInvalidosException("El nombre solo puede contener letras, espacios y guiones");
		}
		if (!data.getCorreo().trim().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
			throw new DatosInvalidosException("El correo no tiene un formato valido (ej: usuario@dominio.com)");
		}
		if (!data.getRol().trim().equalsIgnoreCase(ROL_GESTOR)
				&& !data.getRol().trim().equalsIgnoreCase(ROL_CONSULTA)) {
			throw new DatosInvalidosException("El rol debe ser " + ROL_GESTOR + " o " + ROL_CONSULTA);
		}

		if (data.getUsername() == null || data.getUsername().trim().isEmpty()) {
			throw new DatosInvalidosException("El nombre de usuario no puede estar vacio");
		}
		if (data.getUsername().trim().length() < 4) {
			throw new DatosInvalidosException("El nombre de usuario debe tener minimo 4 caracteres");
		}
		if (!data.getUsername().trim().matches("[a-zA-Z0-9._\\-]+")) {
			throw new DatosInvalidosException(
					"El nombre de usuario solo puede contener letras, numeros, puntos, guiones y guiones bajos");
		}

		if (data.getPassword() == null || data.getPassword().isEmpty()) {
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

		if (usuarioRepo.existsById(data.getId())) {
			throw new RecursoDuplicadoException("Ya existe un usuario con el codigo " + data.getId());
		}
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
		entity.setRol(data.getRol().trim().toUpperCase());
		entity.setPassword(passwordEncoder.encode(data.getPassword()));
		usuarioRepo.save(entity);
		return 0;
	}

	/**
	 * Obtiene todos los usuarios registrados. La contrasena nunca se incluye.
	 *
	 * @return Lista de {@link UsuarioDTO}.
	 */
	@Override
	public List<UsuarioDTO> getAll() {

		List<UsuarioDTO> dtoList = new ArrayList<>();
		Iterable<Usuario> entities = usuarioRepo.findAll();

		for (Usuario entity : entities) {
			UsuarioDTO dto = mapper.map(entity, UsuarioDTO.class);
			dto.setPassword(null);
			dtoList.add(dto);
		}

		return dtoList;
	}

	/**
	 * Elimina un usuario por su codigo.
	 *
	 * @param id Codigo del usuario a eliminar.
	 * @return 0 si se elimino correctamente.
	 * @throws DatosInvalidosException si el codigo es nulo.
	 * @throws RecursoNoEncontradoException si el usuario no existe.
	 */
	@Override
	public int deleteById(Long id) {

		if (id == null) {
			throw new DatosInvalidosException("El codigo del usuario no puede ser nulo");
		}
		if (!usuarioRepo.existsById(id)) {
			throw new RecursoNoEncontradoException("No existe un usuario con el codigo " + id);
		}

		usuarioRepo.deleteById(id);
		return 0;
	}

	/**
	 * Actualiza nombre, correo y rol de un usuario. Si se envia una contrasena
	 * nueva, se valida y se guarda con hash; si viene vacia, se conserva la
	 * actual. El nombre de usuario (login) no se modifica.
	 *
	 * @param id      Codigo del usuario a actualizar.
	 * @param newData Nuevos datos del usuario.
	 * @return 0 si se actualizo con exito.
	 * @throws DatosInvalidosException si el codigo es nulo o los datos nuevos son
	 *         nulos, incompletos o con formato invalido.
	 * @throws RecursoNoEncontradoException si el usuario no existe.
	 * @throws RecursoDuplicadoException si el correo pertenece a otro usuario.
	 */
	@Override
	public int updateById(Long id, UsuarioDTO newData) {

		if (id == null) {
			throw new DatosInvalidosException("El codigo del usuario no puede ser nulo");
		}

		Usuario entityToUpdate = usuarioRepo.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con el codigo " + id));

		if (newData == null) {
			throw new DatosInvalidosException("Los datos del usuario no pueden ser nulos");
		}
		if (newData.getId() <= 0) {
			throw new DatosInvalidosException("El codigo del usuario debe ser un numero positivo");
		}
		if (newData.getNombre() == null || newData.getNombre().trim().isEmpty()) {
			throw new DatosInvalidosException("El nombre no puede estar vacio");
		}
		if (newData.getCorreo() == null || newData.getCorreo().trim().isEmpty()) {
			throw new DatosInvalidosException("El correo no puede estar vacio");
		}
		if (newData.getRol() == null || newData.getRol().trim().isEmpty()) {
			throw new DatosInvalidosException("El rol no puede estar vacio");
		}
		if (!newData.getNombre().trim().matches("[\\p{L} \\-]+")) {
			throw new DatosInvalidosException("El nombre solo puede contener letras, espacios y guiones");
		}
		if (!newData.getCorreo().trim().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
			throw new DatosInvalidosException("El correo no tiene un formato valido (ej: usuario@dominio.com)");
		}
		if (!newData.getRol().trim().equalsIgnoreCase(ROL_GESTOR)
				&& !newData.getRol().trim().equalsIgnoreCase(ROL_CONSULTA)) {
			throw new DatosInvalidosException("El rol debe ser " + ROL_GESTOR + " o " + ROL_CONSULTA);
		}

		boolean cambiaContrasena = newData.getPassword() != null && !newData.getPassword().isEmpty();

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

		if (porCorreo.isPresent() && porCorreo.get().getId() != id.intValue()) {
			throw new RecursoDuplicadoException("El correo " + correo + " ya pertenece a otro usuario");
		}

		entityToUpdate.setNombre(newData.getNombre().trim());
		entityToUpdate.setCorreo(correo);
		entityToUpdate.setRol(newData.getRol().trim().toUpperCase());

		if (cambiaContrasena) {
			entityToUpdate.setPassword(passwordEncoder.encode(newData.getPassword()));
		}

		usuarioRepo.save(entityToUpdate);
		return 0;
	}

	/**
	 * Cuenta el total de usuarios registrados.
	 *
	 * @return Cantidad total de usuarios.
	 */
	@Override
	public long count() {

		return usuarioRepo.count();
	}

	/**
	 * Verifica si existe un usuario con el codigo indicado.
	 *
	 * @param id Codigo del usuario.
	 * @return true si existe, false en caso contrario.
	 */
	@Override
	public boolean exist(Long id) {

		return id != null && usuarioRepo.existsById(id);
	}

	/**
	 * Busca un usuario por su codigo unico.
	 *
	 * @param id Codigo del usuario.
	 * @return {@link UsuarioDTO} del usuario encontrado, sin contrasena.
	 * @throws DatosInvalidosException si el codigo es nulo.
	 * @throws RecursoNoEncontradoException si el usuario no existe.
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
	 * Busca un usuario por su nombre de usuario (credencial de login). Retorna
	 * null si no existe.
	 *
	 * @param username Nombre de usuario.
	 * @return {@link UsuarioDTO} sin contrasena, o null si no se encuentra.
	 */
	public UsuarioDTO getByUsername(String username) {

		if (username == null || username.trim().isEmpty()) {
			return null;
		}

		Optional<Usuario> found = usuarioRepo.findByUsername(username.trim());

		if (found.isEmpty()) {
			return null;
		}

		UsuarioDTO dto = mapper.map(found.get(), UsuarioDTO.class);
		dto.setPassword(null);
		return dto;
	}

	/**
	 * Busca un usuario por su correo electronico.
	 *
	 * @param correo Correo institucional del usuario.
	 * @return {@link UsuarioDTO} del usuario encontrado, sin contrasena.
	 * @throws DatosInvalidosException si el correo es nulo o esta vacio.
	 * @throws RecursoNoEncontradoException si no existe un usuario con ese correo.
	 */
	public UsuarioDTO getByCorreo(String correo) {

		if (correo == null || correo.trim().isEmpty()) {
			throw new DatosInvalidosException("El correo no puede estar vacio");
		}

		Usuario encontrado = usuarioRepo.findByCorreo(correo.trim()).orElseThrow(
				() -> new RecursoNoEncontradoException("No existe un usuario con el correo " + correo.trim()));

		UsuarioDTO dto = mapper.map(encontrado, UsuarioDTO.class);
		dto.setPassword(null);
		return dto;
	}
}