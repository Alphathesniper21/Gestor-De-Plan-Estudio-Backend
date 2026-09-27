package co.edu.unbosque.sdgh.repository;

import org.springframework.data.repository.CrudRepository;
import co.edu.unbosque.sdgh.entity.Usuario;

public interface UsuarioRepository extends CrudRepository<Usuario, Integer> {
}
