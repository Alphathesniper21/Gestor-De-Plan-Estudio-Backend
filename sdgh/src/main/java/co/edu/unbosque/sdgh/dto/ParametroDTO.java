
package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

/**
 * DTO encargado de transportar la informacion de un parametro
 * configurable del sistema.
 * <p>
 * Permite almacenar y transferir la clave y el valor de los
 * parametros utilizados para configurar el comportamiento
 * de la aplicacion, de acuerdo con el requerimiento funcional RF9.
 * </p>
 *
 * @author Laura Gomez
 * @version 1.0
 * @since 1.0
 * @see java.util.Objects
 */
public class ParametroDTO {

    /**
     * Clave que identifica de manera unica el parametro configurable.
     */
    private String clave;

    /**
     * Valor asociado a la clave del parametro dentro del sistema.
     */
    private String valor;

    /**
     * Constructor por defecto de la clase.
     * Permite crear una instancia de ParametroDTO sin asignar valores iniciales.
     */
    public ParametroDTO() {

    }

    /**
     * Constructor que permite inicializar un parametro con su clave y valor.
     *
     * @param clave clave que identifica el parametro configurable.
     * @param valor valor asociado al parametro.
     */
    public ParametroDTO(String clave, String valor) {
        super();
        this.clave = clave;
        this.valor = valor;
    }

    /**
     * Obtiene la clave del parametro configurable.
     *
     * @return clave que identifica el parametro.
     */
    public String getClave() {
        return clave;
    }

    /**
     * Establece la clave del parametro configurable.
     *
     * @param clave nueva clave que identificara el parametro.
     */
    public void setClave(String clave) {
        this.clave = clave;
    }

    /**
     * Obtiene el valor asociado al parametro.
     *
     * @return valor actual del parametro.
     */
    public String getValor() {
        return valor;
    }

    /**
     * Establece el valor asociado al parametro.
     *
     * @param valor nuevo valor que se asignara al parametro.
     */
    public void setValor(String valor) {
        this.valor = valor;
    }

    /**
     * Genera una representacion en texto del objeto ParametroDTO.
     *
     * @return cadena de texto con la clave y el valor del parametro.
     */
    @Override
    public String toString() {
        return "ParametroDTO [clave=" + clave + ", valor=" + valor + "]";
    }

    /**
     * Calcula el codigo hash del objeto utilizando sus atributos clave y valor.
     *
     * @return codigo hash generado a partir de la clave y el valor.
     */
    @Override
    public int hashCode() {
        return Objects.hash(clave, valor);
    }

    /**
     * Compara este objeto con otro para determinar si ambos son iguales.
     * <p>
     * La comparacion se realiza verificando que los objetos pertenezcan
     * a la misma clase y que sus atributos clave y valor sean iguales.
     * </p>
     *
     * @param obj objeto con el que se desea realizar la comparacion.
     * @return true si ambos objetos tienen la misma clase, clave y valor;
     *         false en caso contrario.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        ParametroDTO other = (ParametroDTO) obj;

        return Objects.equals(clave, other.clave)
                && Objects.equals(valor, other.valor);
    }

}
