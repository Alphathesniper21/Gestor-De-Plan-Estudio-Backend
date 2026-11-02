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

@Configuration
public class LoadDatabase {

	private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

	@Value("${app.default-password}")
	private String defaultPassword;

	@Bean
	CommandLineRunner initDatabase(UsuarioRepository usuarioRepo, PasswordEncoder passwordEncoder) {
		return args -> {

			Optional<Usuario> found = usuarioRepo.findByUsername("gestor");
			if (found.isPresent()) {
				log.info("El gestor ya existe, omitiendo creacion...");
			} else {
				Usuario gestor = new Usuario("Gestor", "gestor@unbosque.edu.co", "gestor",
						passwordEncoder.encode(defaultPassword), "GESTOR");
				usuarioRepo.save(gestor);
				log.info("Precargando usuario gestor");
			}

			Optional<Usuario> found2 = usuarioRepo.findByUsername("consulta");
			if (found2.isPresent()) {
				log.info("El usuario de consulta ya existe, omitiendo creacion...");
			} else {
				Usuario consulta = new Usuario("Consulta", "consulta@unbosque.edu.co", "consulta",
						passwordEncoder.encode(defaultPassword), "CONSULTA");
				usuarioRepo.save(consulta);
				log.info("Precargando usuario de consulta");
			}
		};
	}
}