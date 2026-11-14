package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

/**
 * DTO de un parametro configurable del sistema (RF9).
 */
public class ParametroDTO {

	/**
	 * Clave del parametro.
	 */
	private String clave;

	/**
	 * Valor del parametro.
	 */
	private String valor;

	public ParametroDTO() {
	}

	public ParametroDTO(String clave, String valor) {
		super();
		this.clave = clave;
		this.valor = valor;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	@Override
	public String toString() {
		return "ParametroDTO [clave=" + clave + ", valor=" + valor + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(clave, valor);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParametroDTO other = (ParametroDTO) obj;
		return Objects.equals(clave, other.clave) && Objects.equals(valor, other.valor);
	}
}