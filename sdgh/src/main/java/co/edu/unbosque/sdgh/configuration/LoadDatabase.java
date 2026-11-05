package co.edu.unbosque.sdgh.configuration;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.sdgh.entity.Usuario;
import co.edu.unbosque.sdgh.repository.UsuarioRepository;

/**
 * Inicializador de datos predeterminados del aplicativo.
 * <p>
 * Al arrancar la aplicacion verifica si existen los usuarios de sistema
 * ({@code gestor} y {@code consulta}) y los crea en caso de no encontrarlos,
 * con la contrasena codificada con BCrypt. La contrasena por defecto se lee
 * de la propiedad {@code app.default-password}.
 * </p>
 */
@Configuration
public class LoadDatabase {

	/**
	 * Logger para registrar mensajes durante la carga de datos.
	 */
	private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

	/**
	 * Contrasena por defecto de los usuarios de sistema.
	 */
	@Value("${app.default-password}")
	private String defaultPassword;

	/**
	 * Inicializa la base de datos con usuarios predeterminados. Crea un usuario
	 * GESTOR y un usuario CONSULTA si no existen.
	 *
	 * @param userRepo Repositorio de usuarios.
	 * @param passwordEncoder Codificador de contrasenas.
	 * @return Un CommandLineRunner que se ejecuta al iniciar la aplicacion.
	 */
	@Bean
	CommandLineRunner initDatabase(UsuarioRepository userRepo, PasswordEncoder passwordEncoder) {

		return args -> {
			Optional<Usuario> found = userRepo.findByUsername("gestor");
			if (found.isPresent()) {
				log.info("El gestor ya existe, omitiendo la creacion del gestor...");
			} else {
				Usuario gestor = new Usuario("Gestor Inicial", "gestor@unbosque.edu.co", "gestor",
						passwordEncoder.encode(defaultPassword), "GESTOR");
				userRepo.save(gestor);
				log.info("Precargando usuario gestor");
			}

			Optional<Usuario> found2 = userRepo.findByUsername("consulta");
			if (found2.isPresent()) {
				log.info("El usuario de consulta ya existe, omitiendo la creacion...");
			} else {
				Usuario consulta = new Usuario("Usuario Consulta", "consulta@unbosque.edu.co", "consulta",
						passwordEncoder.encode(defaultPassword), "CONSULTA");
				userRepo.save(consulta);
				log.info("Precargando usuario de consulta");
			}
		};
	}
}