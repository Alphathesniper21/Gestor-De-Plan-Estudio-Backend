package co.edu.unbosque.sdgh;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Clase principal del Sistema de Gestión de Horarios (SDGH) del plan de
 * Ingeniería de Sistemas de la Universidad El Bosque.
 * <p>
 * Arranca la aplicación Spring Boot que expone la API REST del prototipo y
 * registra el bean de {@link ModelMapper} utilizado por los servicios para
 * convertir entre entidades y DTOs.
 * </p>
 *
 * @author Martín Peña
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
public class SdghApplication {

	/**
	 * Punto de entrada de la aplicación.
	 *
	 * @param args argumentos de línea de comandos
	 */
	public static void main(String[] args) {
		SpringApplication.run(SdghApplication.class, args);
	}

	/**
	 * Metodo de creacion y registro de bean ModelMApper.
	 * Permite la conversion de objetos y DTOs.
	 * @return Retorna una instancia de ModelMapper configurada para su uso.
	 */
	@Bean
	public ModelMapper getModelMapper() {
		
		return new ModelMapper();
	}

}