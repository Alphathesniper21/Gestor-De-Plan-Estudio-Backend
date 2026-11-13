package co.edu.unbosque.sdgh;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SdghApplication {

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
