package co.edu.unbosque.sdgh;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Inicializador de servlet para el despliegue del Sistema de Gestión de
 * Horarios (SDGH) como archivo WAR en un contenedor de servlets externo (por
 * ejemplo, Tomcat).
 * <p>
 * Delega la configuración de la aplicación en {@link SdghApplication}.
 * </p>
 *
 * @author Martín Peña
 * @version 1.0
 * @since 1.0
 * @see SdghApplication
 */
public class ServletInitializer extends SpringBootServletInitializer {

	/**
	 * Configura el constructor de la aplicación indicando la clase principal.
	 *
	 * @param application constructor de la aplicación Spring
	 * @return constructor configurado con {@link SdghApplication}
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SdghApplication.class);
	}

}