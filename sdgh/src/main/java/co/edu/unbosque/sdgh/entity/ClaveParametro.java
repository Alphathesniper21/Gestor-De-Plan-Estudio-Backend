package co.edu.unbosque.sdgh.entity;

/**
 * Clase que contiene las claves constantes utilizadas para identificar
 * los parámetros configurables del sistema.
 *
 * Estas constantes permiten centralizar los nombres de los parámetros
 * relacionados con los horarios, las pausas, los cupos de los grupos
 * y la publicación del horario, evitando errores de escritura.
 *
 * Hace parte de los requerimientos funcionales RF2, RF4, RF5, RF7, RF9 y RF12.
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see Parametro
 */
public final class ClaveParametro {

    /**
     * Clave que identifica la hora de apertura de la jornada de lunes a sábado.
     * Corresponde al requerimiento funcional RF4.
     */
    public static final String HORA_INICIO_JORNADA = "hora.inicio.jornada";

    /**
     * Clave que identifica la hora de cierre de la jornada de lunes a viernes.
     * Corresponde al requerimiento funcional RF4.
     */
    public static final String HORA_FIN_SEMANA = "hora.fin.semana";

    /**
     * Clave que identifica la hora de cierre de la jornada del sábado.
     * Corresponde al requerimiento funcional RF4.
     */
    public static final String HORA_FIN_SABADO = "hora.fin.sabado";

    /**
     * Clave que identifica la hora de inicio de la pausa del mediodía.
     * Corresponde al requerimiento funcional RF5.
     */
    public static final String PAUSA_INICIO = "pausa.inicio";

    /**
     * Clave que identifica la hora de finalización de la pausa del mediodía.
     * Corresponde al requerimiento funcional RF5.
     */
    public static final String PAUSA_FIN = "pausa.fin";

    /**
     * Clave que identifica el cupo base establecido para cada grupo,
     * antes de aplicar la tolerancia permitida.
     * Corresponde al requerimiento funcional RF2.
     */
    public static final String CUPO_BASE = "cupo.base";

    /**
     * Clave que identifica el porcentaje de tolerancia aplicado al cupo base.
     * Su valor debe encontrarse entre 0 y 1.
     * Corresponde al requerimiento funcional RF2.
     */
    public static final String CUPO_TOLERANCIA = "cupo.tolerancia";

    /**
     * Clave que identifica el número máximo de sesiones semanales
     * permitidas para un curso.
     * Corresponde a los requerimientos funcionales RF1 y RF7.
     */
    public static final String SESIONES_MAXIMAS = "sesiones.maximas";

    /**
     * Clave reservada para almacenar el estado de publicación del horario.
     * Este parámetro únicamente debe modificarse desde el proceso
     * de publicación del horario.
     * Corresponde al requerimiento funcional RF12.
     */
    public static final String HORARIO_PUBLICADO = "horario.publicado";

    /**
     * Constructor de la clase ClaveParametro.
     *
     * Se declara privado para impedir la creación de instancias,
     * ya que esta clase únicamente proporciona constantes estáticas.
     */
    private ClaveParametro() {
        // No se permite crear instancias de esta clase.
    }
}