package co.edu.unbosque.sdgh;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Prueba de integración del contexto principal de Spring Boot para
 * {@link SdghApplication}.
 * <p>
 * Verifica que el contenedor de inversión de control cargue correctamente
 * todos los beans, configuraciones y componentes del sistema usando la base de
 * datos en memoria H2 de pruebas.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see SdghApplication
 */
@SpringBootTest
class SdghApplicationTests {

	/**
	 * Contexto de la aplicación inyectado por Spring Boot durante la prueba.
	 */
	@Autowired
	private ApplicationContext context;

	/**
	 * Verifica que el contexto de la aplicación se inicialice correctamente y no
	 * sea nulo.
	 */
	@Test
	void contextLoads() {
		assertNotNull(context);
	}

}
