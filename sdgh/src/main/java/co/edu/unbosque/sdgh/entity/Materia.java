package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Entidad que representa una materia perteneciente al plan de estudios.
 *
 * Almacena la información académica de cada materia, incluyendo su código,
 * nombre, semestre, cantidad de créditos, horas y condición de correlatividad.
 *
 * También permite calcular la cantidad de sesiones semanales a partir
 * de las horas asignadas a la materia.
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see Grupo
 */
@Entity
@Table(name = "materia")
public class Materia {

    /**
     * Identificador único de la materia. Se genera automáticamente
     * al registrar una nueva materia en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Código institucional de la materia.
     * Es único y obligatorio dentro del sistema.
     */
    @Column(unique = true, nullable = false)
    private long codigo;

    /**
     * Nombre con el que se identifica la materia.
     */
    private String nombre;

    /**
     * Semestre académico al que pertenece la materia.
     */
    private int semestre;

    /**
     * Cantidad de créditos académicos asignados a la materia.
     */
    private int ncreditos;

    /**
     * Cantidad de horas semanales correspondientes a la materia.
     */
    private int horasDeLaMateria;

    /**
     * Indica si la materia tiene una condición de correlatividad.
     */
    private boolean correlativa;

    /**
     * Constructor vacío de la entidad Materia.
     * Se utiliza para permitir la creación de instancias por parte de JPA.
     */
    public Materia() {
        super();
    }

    /**
     * Construye una materia con todos sus datos académicos.
     *
     * @param codigo Código institucional de la materia.
     * @param nombre Nombre de la materia.
     * @param semestre Semestre al que pertenece la materia.
     * @param ncreditos Cantidad de créditos académicos.
     * @param horasDeLaMateria Cantidad de horas semanales de la materia.
     * @param correlativa Indica si la materia tiene correlatividad.
     */
    public Materia(long codigo, String nombre, int semestre, int ncreditos, int horasDeLaMateria,
            boolean correlativa) {
        super();
        this.codigo = codigo;
        this.nombre = nombre;
        this.semestre = semestre;
        this.ncreditos = ncreditos;
        this.horasDeLaMateria = horasDeLaMateria;
        this.correlativa = correlativa;
    }

    /**
     * Obtiene el identificador único de la materia.
     *
     * @return Identificador de la materia.
     */
    public long getId() {
        return id;
    }

    /**
     * Establece el identificador de la materia.
     *
     * @param id Identificador que se asignará a la materia.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Obtiene el código institucional de la materia.
     *
     * @return Código de la materia.
     */
    public long getCodigo() {
        return codigo;
    }

    /**
     * Establece el código institucional de la materia.
     *
     * @param codigo Código que se asignará a la materia.
     */
    public void setCodigo(long codigo) {
        this.codigo = codigo;
    }

    /**
     * Obtiene el nombre de la materia.
     *
     * @return Nombre de la materia.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre de la materia.
     *
     * @param nombre Nombre que se asignará a la materia.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el semestre al que pertenece la materia.
     *
     * @return Número del semestre.
     */
    public int getSemestre() {
        return semestre;
    }

    /**
     * Establece el semestre al que pertenece la materia.
     *
     * @param semestre Número del semestre.
     */
    public void setSemestre(int semestre) {
        this.semestre = semestre;
    }

    /**
     * Obtiene la cantidad de créditos académicos de la materia.
     *
     * @return Cantidad de créditos.
     */
    public int getNcreditos() {
        return ncreditos;
    }

    /**
     * Establece la cantidad de créditos académicos de la materia.
     *
     * @param ncreditos Cantidad de créditos que se asignará.
     */
    public void setNcreditos(int ncreditos) {
        this.ncreditos = ncreditos;
    }

    /**
     * Obtiene la cantidad de horas semanales de la materia.
     *
     * @return Cantidad de horas de la materia.
     */
    public int getHorasDeLaMateria() {
        return horasDeLaMateria;
    }

    /**
     * Establece la cantidad de horas semanales de la materia.
     *
     * @param horasDeLaMateria Cantidad de horas que se asignará.
     */
    public void setHorasDeLaMateria(int horasDeLaMateria) {
        this.horasDeLaMateria = horasDeLaMateria;
    }

    /**
     * Consulta si la materia tiene condición de correlatividad.
     *
     * @return TRUE si la materia es correlativa; FALSE en caso contrario.
     */
    public boolean isCorrelativa() {
        return correlativa;
    }

    /**
     * Establece la condición de correlatividad de la materia.
     *
     * @param correlativa TRUE si la materia es correlativa; FALSE en caso contrario.
     */
    public void setCorrelativa(boolean correlativa) {
        this.correlativa = correlativa;
    }

    /**
     * Genera una representación en texto de la materia con sus atributos.
     *
     * @return Cadena de texto con la información de la materia.
     */
    @Override
    public String toString() {
        return "Materia [id=" + id + ", codigo=" + codigo + ", nombre=" + nombre + ", semestre=" + semestre
                + ", ncreditos=" + ncreditos + ", horasDeLaMateria=" + horasDeLaMateria + ", correlativa=" + correlativa
                + "]";
    }

    /**
     * Calcula el código hash de la materia utilizando sus atributos.
     *
     * @return Código hash generado a partir de los datos de la materia.
     */
    @Override
    public int hashCode() {
        return Objects.hash(Long.valueOf(codigo), Boolean.valueOf(correlativa), Integer.valueOf(horasDeLaMateria),
                Long.valueOf(id), Integer.valueOf(ncreditos), nombre, Integer.valueOf(semestre));
    }

    /**
     * Compara esta materia con otro objeto para determinar si son iguales.
     *
     * La comparación verifica que ambos objetos pertenezcan a la misma clase
     * y que sus atributos tengan los mismos valores.
     *
     * @param obj Objeto con el que se desea comparar la materia.
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
        Materia other = (Materia) obj;
        return codigo == other.codigo && correlativa == other.correlativa && horasDeLaMateria == other.horasDeLaMateria
                && id == other.id && ncreditos == other.ncreditos && Objects.equals(nombre, other.nombre)
                && semestre == other.semestre;
    }

    /**
     * Calcula la cantidad de sesiones semanales de la materia.
     *
     * Cada sesión tiene una duración de dos horas, por lo que el resultado
     * se obtiene dividiendo las horas semanales entre dos.
     *
     * Este método no se almacena como una columna en la base de datos.
     *
     * @return Cantidad de sesiones semanales calculadas.
     */
    @Transient
    public int getSesionesSemanales() {
        return horasDeLaMateria / 2;
    }

}