package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa un parámetro configurable del sistema mediante
 * una pareja de clave y valor.
 *
 * Permite almacenar configuraciones relacionadas con el funcionamiento
 * del sistema, como los horarios, las pausas y los cupos.
 *
 * También almacena el estado de publicación del horario mediante
 * la clave correspondiente.
 *
 * Corresponde a los requerimientos funcionales RF9 y RF12.
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see ClaveParametro
 */
@Entity
@Table(name = "parametro")
public class Parametro {

    /**
     * Clave única que identifica el parámetro dentro del sistema.
     * Se utiliza como identificador principal de la entidad.
     */
    @Id
    private String clave;

    /**
     * Valor asociado a la clave del parámetro.
     * Se almacena como texto y es obligatorio en la base de datos.
     */
    @Column(nullable = false)
    private String valor;

    /**
     * Constructor vacío de la entidad Parametro.
     * Se utiliza para permitir la creación de instancias por parte de JPA.
     */
    public Parametro() {
        super();
    }

    /**
     * Construye un parámetro con su clave y valor correspondientes.
     *
     * @param clave Nombre único que identifica el parámetro.
     * @param valor Valor que se almacenará para el parámetro.
     */
    public Parametro(String clave, String valor) {
        super();
        this.clave = clave;
        this.valor = valor;
    }

    /**
     * Obtiene la clave que identifica el parámetro.
     *
     * @return Clave del parámetro.
     */
    public String getClave() {
        return clave;
    }

    /**
     * Establece la clave del parámetro.
     *
     * @param clave Clave que se asignará al parámetro.
     */
    public void setClave(String clave) {
        this.clave = clave;
    }

    /**
     * Obtiene el valor asociado al parámetro.
     *
     * @return Valor del parámetro.
     */
    public String getValor() {
        return valor;
    }

    /**
     * Establece el valor asociado al parámetro.
     *
     * @param valor Valor que se asignará al parámetro.
     */
    public void setValor(String valor) {
        this.valor = valor;
    }

    /**
     * Genera una representación en texto del parámetro.
     *
     * @return Cadena de texto con la clave y el valor del parámetro.
     */
    @Override
    public String toString() {
        return "Parametro [clave=" + clave + ", valor=" + valor + "]";
    }

    /**
     * Calcula el código hash del parámetro utilizando su clave y valor.
     *
     * @return Código hash generado a partir de los atributos del parámetro.
     */
    @Override
    public int hashCode() {
        return Objects.hash(clave, valor);
    }

    /**
     * Compara este parámetro con otro objeto para determinar si son iguales.
     *
     * La comparación verifica que ambos objetos pertenezcan a la misma clase
     * y que tengan la misma clave y el mismo valor.
     *
     * @param obj Objeto con el que se desea comparar el parámetro.
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
        Parametro other = (Parametro) obj;
        return Objects.equals(clave, other.clave) && Objects.equals(valor, other.valor);
    }

}