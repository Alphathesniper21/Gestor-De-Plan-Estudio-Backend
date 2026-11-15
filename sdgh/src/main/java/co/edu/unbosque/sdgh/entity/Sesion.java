package co.edu.unbosque.sdgh.entity;

import java.time.LocalTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa una sesión de clase perteneciente a un grupo
 * dentro de la distribución semanal de horarios.
 *
 * Cada sesión almacena el día de la semana en el que se realiza,
 * junto con su hora de inicio y finalización. Su duración corresponde
 * a dos horas y se encuentra asociada a un grupo específico.
 *
 * Corresponde al requerimiento funcional RF3.
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see Grupo
 */
@Entity
@Table(name = "sesion")
public class Sesion {

    /**
     * Identificador único de la sesión. Se genera automáticamente
     * al registrar una nueva sesión en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Identificador del grupo al que pertenece la sesión.
     */
    private long grupoId;

    /**
     * Número que representa el día de la semana en el que se realiza
     * la sesión. Los valores van de 1 a 6, donde 1 corresponde al lunes
     * y 6 al sábado.
     */
    private int diaSemana;

    /**
     * Hora en la que comienza la sesión de clase.
     */
    private LocalTime horaInicio;

    /**
     * Hora en la que finaliza la sesión de clase.
     */
    private LocalTime horaFin;

    /**
     * Constructor vacío de la entidad Sesion.
     * Se utiliza para permitir la creación de instancias por parte de JPA.
     */
    public Sesion() {
        super();
    }

    /**
     * Construye una sesión con el grupo, día y horario correspondientes.
     *
     * @param grupoId Identificador del grupo al que pertenece la sesión.
     * @param diaSemana Día de la semana en el que se realiza la sesión.
     * @param horaInicio Hora de inicio de la sesión.
     * @param horaFin Hora de finalización de la sesión.
     */
    public Sesion(long grupoId, int diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        super();
        this.grupoId = grupoId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    /**
     * Obtiene el identificador único de la sesión.
     *
     * @return Identificador de la sesión.
     */
    public long getId() {
        return id;
    }

    /**
     * Establece el identificador de la sesión.
     *
     * @param id Identificador que se asignará a la sesión.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Obtiene el identificador del grupo asociado a la sesión.
     *
     * @return Identificador del grupo.
     */
    public long getGrupoId() {
        return grupoId;
    }

    /**
     * Establece el identificador del grupo asociado a la sesión.
     *
     * @param grupoId Identificador del grupo.
     */
    public void setGrupoId(long grupoId) {
        this.grupoId = grupoId;
    }

    /**
     * Obtiene el día de la semana en el que se realiza la sesión.
     *
     * @return Número del día de la semana.
     */
    public int getDiaSemana() {
        return diaSemana;
    }

    /**
     * Establece el día de la semana de la sesión.
     *
     * @param diaSemana Número del día de la semana, de 1 (lunes)
     *                  a 6 (sábado).
     */
    public void setDiaSemana(int diaSemana) {
        this.diaSemana = diaSemana;
    }

    /**
     * Obtiene la hora de inicio de la sesión.
     *
     * @return Hora de inicio representada mediante LocalTime.
     */
    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    /**
     * Establece la hora de inicio de la sesión.
     *
     * @param horaInicio Hora en la que comienza la sesión.
     */
    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    /**
     * Obtiene la hora de finalización de la sesión.
     *
     * @return Hora de finalización representada mediante LocalTime.
     */
    public LocalTime getHoraFin() {
        return horaFin;
    }

    /**
     * Establece la hora de finalización de la sesión.
     *
     * @param horaFin Hora en la que termina la sesión.
     */
    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    /**
     * Genera una representación en texto de la sesión con sus atributos.
     *
     * @return Cadena de texto con la información de la sesión.
     */
    @Override
    public String toString() {
        return "Sesion [id=" + id + ", grupoId=" + grupoId + ", diaSemana=" + diaSemana + ", horaInicio=" + horaInicio
                + ", horaFin=" + horaFin + "]";
    }

    /**
     * Calcula el código hash de la sesión utilizando sus atributos.
     *
     * @return Código hash generado a partir de los datos de la sesión.
     */
    @Override
    public int hashCode() {
        return Objects.hash(Integer.valueOf(diaSemana), Long.valueOf(grupoId), horaFin, horaInicio, Long.valueOf(id));
    }

    /**
     * Compara esta sesión con otro objeto para determinar si son iguales.
     *
     * La comparación verifica que ambos objetos pertenezcan a la misma clase
     * y que sus atributos tengan los mismos valores.
     *
     * @param obj Objeto con el que se desea comparar la sesión.
     * @return TRUE si ambos objetos son iguales; FALSE en caso contrario.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Sesion other = (Sesion) obj;
        return diaSemana == other.diaSemana && grupoId == other.grupoId && Objects.equals(horaFin, other.horaFin)
                && Objects.equals(horaInicio, other.horaInicio) && id == other.id;
    }

}