package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que guarda un valor configurable del sistema como pareja clave y
 * valor (RF9). Tambien guarda el estado "publicado" del horario con la clave
 * horario.publicado (RF12). Las claves posibles estan en ClaveParametro.
 */
@Entity
@Table(name = "parametro")
public class Parametro {

	/**
	 * Clave unica del parametro (por ejemplo, pausa.inicio).
	 */
	@Id
	private String clave;

	/**
	 * Valor del parametro, guardado como texto.
	 */
	@Column(nullable = false)
	private String valor;

	public Parametro() {
		super();
	}

	public Parametro(String clave, String valor) {
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
		return "Parametro [clave=" + clave + ", valor=" + valor + "]";
	}

	/**
	 * La identidad del parametro es su clave; el valor cambia con el tiempo.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(clave);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parametro other = (Parametro) obj;
		return Objects.equals(clave, other.clave);
	}

}
