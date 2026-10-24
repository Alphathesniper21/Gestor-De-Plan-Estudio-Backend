package co.edu.unbosque.sdgh.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.edu.unbosque.sdgh.dto.UsuarioDTO;
import co.edu.unbosque.sdgh.entity.Usuario;
import co.edu.unbosque.sdgh.repository.UsuarioRepository;

/**
 * Servicio que gestiona la logica de negocio para los usuarios del sistema
 * (CU01, CU09 - RF11, RF22, RF24).
 */
@Service
public class UsuarioService implements CRUDOperation<UsuarioDTO> {

	@Autowired
	private UsuarioRepository usuarioRepo;

	public UsuarioService() {
	}

	/**
	 * Registra un nuevo usuario validando unicidad de codigo y correo (CU09 / RF24).
	 *
	 * @param data DTO con los datos del usuario a registrar.
	 * @return 0 si se registro con exito;
	 *         1 si el codigo o correo ya existe (Correo duplicado);
	 *         2 si los datos son nulos o incompletos.
	 */
	@Override
	public int create(UsuarioDTO data) {
		if (!esUsuarioValido(data)) {
			return 2;
		}
		if (usuarioRepo.existsById(data.getCodigo())) {
			return 1;
		}
		if (usuarioRepo.existsByCorreo(data.getCorreo().trim())) {
			return 1;
		}

		Usuario entity = toEntity(data);
		usuarioRepo.save(entity);
		return 0;
	}

	/**
	 * Obtiene todos los usuarios registrados en el sistema (CU09 / RF24).
	 *
	 * @return Lista de {@link UsuarioDTO}.
	 */
	@Override
	public List<UsuarioDTO> getAll() {
		List<UsuarioDTO> dtoList = new ArrayList<>();
		Iterable<Usuario> entities = usuarioRepo.findAll();
		for (Usuario entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Elimina un usuario por su ID (codigo) (CU09 / RF24).
	 *
	 * @param id Codigo del usuario a eliminar.
	 * @return 0 si se elimino correctamente; 1 si no existe o el id es nulo.
	 */
	@Override
	public int deleteById(Integer id) {
		if (id == null || !usuarioRepo.existsById(id)) {
			return 1;
		}
		usuarioRepo.deleteById(id);
		return 0;
	}

	/**
	 * Actualiza la informacion de un usuario existente por su ID (codigo) (CU09 / RF24).
	 *
	 * @param id      Codigo del usuario a actualizar.
	 * @param newData Nuevos datos del usuario.
	 * @return 0 si se actualizo con exito;
	 *         1 si el usuario no existe o el correo ya pertenece a otro usuario;
	 *         2 si los datos nuevos son nulos o incompletos.
	 */
	@Override
	public int updateById(Integer id, UsuarioDTO newData) {
		if (id == null) {
			return 1;
		}
		Optional<Usuario> foundOpt = usuarioRepo.findById(id);
		if (foundOpt.isEmpty()) {
			return 1;
		}
		if (newData == null || newData.getNombre() == null || newData.getNombre().trim().isEmpty()
				|| newData.getCorreo() == null || newData.getCorreo().trim().isEmpty()
				|| newData.getFacultad() == null || newData.getFacultad().trim().isEmpty()) {
			return 2;
		}

		Optional<Usuario> porCorreo = usuarioRepo.findByCorreo(newData.getCorreo().trim());
		if (porCorreo.isPresent() && porCorreo.get().getCodigo() != id.intValue()) {
			return 1;
		}

		Usuario entityToUpdate = foundOpt.get();
		entityToUpdate.setNombre(newData.getNombre().trim());
		entityToUpdate.setCorreo(newData.getCorreo().trim());
		entityToUpdate.setFacultad(newData.getFacultad().trim());

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
	 * Verifica si existe un usuario con el ID (codigo) indicado.
	 *
	 * @param id Codigo del usuario.
	 * @return true si existe, false en caso contrario.
	 */
	@Override
	public boolean exist(Integer id) {
		return id != null && usuarioRepo.existsById(id);
	}

	/**
	 * Busca un usuario por su codigo unico.
	 *
	 * @param id Codigo del usuario.
	 * @return {@link UsuarioDTO} si existe, o null si no se encuentra.
	 */
	public UsuarioDTO getById(Integer id) {
		if (id == null) {
			return null;
		}
		return usuarioRepo.findById(id).map(this::toDTO).orElse(null);
	}

	/**
	 * Busca un usuario por su nombre de usuario (credencial de login) (CU01 / RF11).
	 *
	 * @param username Nombre de usuario.
	 * @return {@link UsuarioDTO} si existe, o null si no se encuentra.
	 */
	public UsuarioDTO getByUsername(String username) {
		if (username == null || username.trim().isEmpty()) {
			return null;
		}
		return usuarioRepo.findByUsername(username.trim()).map(this::toDTO).orElse(null);
	}

	/**
	 * Busca un usuario por su correo electronico.
	 *
	 * @param correo Correo institucional del usuario.
	 * @return {@link UsuarioDTO} si existe, o null si no se encuentra.
	 */
	public UsuarioDTO getByCorreo(String correo) {
		if (correo == null || correo.trim().isEmpty()) {
			return null;
		}
		return usuarioRepo.findByCorreo(correo.trim()).map(this::toDTO).orElse(null);
	}

	/**
	 * Obtiene todos los usuarios pertenecientes a una facultad especifica.
	 *
	 * @param facultad Nombre de la facultad.
	 * @return Lista de {@link UsuarioDTO} de la facultad.
	 */
	public List<UsuarioDTO> getByFacultad(String facultad) {
		List<UsuarioDTO> dtoList = new ArrayList<>();
		if (facultad == null || facultad.trim().isEmpty()) {
			return dtoList;
		}
		List<Usuario> entities = usuarioRepo.findByFacultad(facultad.trim());
		for (Usuario entity : entities) {
			dtoList.add(toDTO(entity));
		}
		return dtoList;
	}

	/**
	 * Valida los datos obligatorios del usuario (CU09 / RF24).
	 *
	 * @param data DTO a validar.
	 * @return true si los datos son validos, false en caso contrario.
	 */
	private boolean esUsuarioValido(UsuarioDTO data) {
		if (data == null) {
			return false;
		}
		if (data.getCodigo() <= 0) {
			return false;
		}
		if (data.getNombre() == null || data.getNombre().trim().isEmpty()) {
			return false;
		}
		if (data.getCorreo() == null || data.getCorreo().trim().isEmpty()) {
			return false;
		}
		if (data.getFacultad() == null || data.getFacultad().trim().isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Convierte una entidad {@link Usuario} a {@link UsuarioDTO}.
	 *
	 * @param entity Entidad Usuario.
	 * @return Objeto UsuarioDTO.
	 */
	public UsuarioDTO toDTO(Usuario entity) {
		if (entity == null) {
			return null;
		}
		return new UsuarioDTO(
				entity.getNombre(),
				entity.getCodigo(),
				entity.getCorreo(),
				entity.getFacultad()
		);
	}

	/**
	 * Convierte un {@link UsuarioDTO} a entidad {@link Usuario}.
	 *
	 * @param dto Objeto UsuarioDTO.
	 * @return Entidad Usuario.
	 */
	public Usuario toEntity(UsuarioDTO dto) {
		if (dto == null) {
			return null;
		}
		return new Usuario(
				dto.getNombre() != null ? dto.getNombre().trim() : null,
				dto.getCodigo(),
				dto.getCorreo() != null ? dto.getCorreo().trim() : null,
				dto.getFacultad() != null ? dto.getFacultad().trim() : null
		);
	}

}
