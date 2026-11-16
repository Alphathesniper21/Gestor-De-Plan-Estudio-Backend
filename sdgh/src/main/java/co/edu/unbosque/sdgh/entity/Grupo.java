package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Entidad que representa un grupo perteneciente a una materia del plan
 * de estudios.
 *
 * El número de grupo permite identificar el bloque al que pertenece
 * dentro de la organización académica. Los grupos con el mismo número
 * de diferentes materias de un semestre conforman un horario sin cruces.
 *
 * Los días y las horas de clase se administran mediante las sesiones
 * asociadas al grupo.
 *
 * Corresponde a los requerimientos funcionales RF2 y RF6.
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see Sesion
 * @see Materia
 */
@Entity
@Table(name = "grupos", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "materiaId", "numeroGrupo" }) })
public class Grupo {

    /**
     * Identificador único del grupo. Se genera automáticamente
     * al registrar un nuevo grupo en la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Código identificador de la materia a la que pertenece el grupo.
     */
    private long materiaId;

    /**
     * Número asignado al grupo dentro de una materia.
     * Permite identificar el bloque correspondiente.
     */
    private int numeroGrupo;

    /**
     * Cantidad de estudiantes planeada para el grupo.
     * El cupo contempla un máximo de 40 estudiantes más un 10 %
     * de tolerancia, según el requerimiento funcional RF2.
     */
    private int cupoPlaneado;

    /**
     * Constructor vacío de la entidad Grupo.
     * Se utiliza para permitir la creación de instancias
     * por parte de JPA.
     */
    public Grupo() {
        super();
    }

    /**
     * Construye un grupo con la información de la materia,
     * el número de grupo y el cupo planeado.
     *
     * @param materiaId Código de la materia a la que pertenece el grupo.
     * @param numeroGrupo Número asignado al grupo.
     * @param cupoPlaneado Cantidad de estudiantes planeada para el grupo.
     */
    public Grupo(long materiaId, int numeroGrupo, int cupoPlaneado) {
        super();
        this.materiaId = materiaId;
        this.numeroGrupo = numeroGrupo;
        this.cupoPlaneado = cupoPlaneado;
    }

    /**
     * Obtiene el identificador único del grupo.
     *
     * @return Identificador del grupo.
     */
    public long getId() {
        return id;
    }

    /**
     * Establece el identificador del grupo.
     *
     * @param id Identificador que se asignará al grupo.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Obtiene el código de la materia asociada al grupo.
     *
     * @return Código de la materia.
     */
    public long getMateriaId() {
        return materiaId;
    }

    /**
     * Establece el código de la materia asociada al grupo.
     *
     * @param materiaId Código de la materia.
     */
    public void setMateriaId(long materiaId) {
        this.materiaId = materiaId;
    }

    /**
     * Obtiene el número asignado al grupo.
     *
     * @return Número del grupo.
     */
    public int getNumeroGrupo() {
        return numeroGrupo;
    }

    /**
     * Establece el número del grupo.
     *
     * @param numeroGrupo Número que se asignará al grupo.
     */
    public void setNumeroGrupo(int numeroGrupo) {
        this.numeroGrupo = numeroGrupo;
    }

    /**
     * Obtiene el cupo planeado para el grupo.
     *
     * @return Cantidad de estudiantes planeada.
     */
    public int getCupoPlaneado() {
        return cupoPlaneado;
    }

    /**
     * Establece el cupo planeado para el grupo.
     *
     * @param cupoPlaneado Cantidad de estudiantes que se establecerá como cupo.
     */
    public void setCupoPlaneado(int cupoPlaneado) {
        this.cupoPlaneado = cupoPlaneado;
    }

    /**
     * Genera una representación en texto del grupo con sus atributos.
     *
     * @return Cadena de texto con la información del grupo.
     */
    @Override
    public String toString() {
        return "Grupo [id=" + id + ", materiaId=" + materiaId + ", numeroGrupo=" + numeroGrupo + ", cupoPlaneado="
                + cupoPlaneado + "]";
    }

    /**
     * Calcula el código hash del grupo utilizando sus atributos.
     *
     * @return Código hash generado a partir de los datos del grupo.
     */
    @Override
    public int hashCode() {
        return Objects.hash(Integer.valueOf(cupoPlaneado), id, Long.valueOf(materiaId), Integer.valueOf(numeroGrupo));
    }

    /**
     * Compara este grupo con otro objeto para determinar si son iguales.
     *
     * La comparación se realiza verificando que ambos objetos sean de la
     * misma clase y que sus atributos tengan los mismos valores.
     *
     * @param obj Objeto con el que se desea comparar el grupo.
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
        Grupo other = (Grupo) obj;
        return cupoPlaneado == other.cupoPlaneado && Objects.equals(id, other.id) && materiaId == other.materiaId
                && numeroGrupo == other.numeroGrupo;
    }

}