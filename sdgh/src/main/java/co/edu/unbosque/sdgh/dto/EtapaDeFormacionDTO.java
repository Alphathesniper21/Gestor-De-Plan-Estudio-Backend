package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

public class EtapaDeFormacionDTO {

	private int numero;

	private String nombre;

	private String meta;

	private int semestreInicio;

	private int semestreFin;

	public EtapaDeFormacionDTO() {
	}

	public EtapaDeFormacionDTO(int numero, String nombre, String meta,
			int semestreInicio, int semestreFin) {
		super();
		this.numero = numero;
		this.nombre = nombre;
		this.meta = meta;
		this.semestreInicio = semestreInicio;
		this.semestreFin = semestreFin;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getMeta() {
		return meta;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}

	public int getSemestreInicio() {
		return semestreInicio;
	}

	public void setSemestreInicio(int semestreInicio) {
		this.semestreInicio = semestreInicio;
	}

	public int getSemestreFin() {
		return semestreFin;
	}

	public void setSemestreFin(int semestreFin) {
		this.semestreFin = semestreFin;
	}

	@Override
	public String toString() {
		return "EtapaFormacionDTO [" + "numero=" + numero + ", nombre=" + nombre + ", meta=" + meta
			+ ", semestreInicio=" + semestreInicio + ", semestreFin=" + semestreFin + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(meta, nombre, numero, semestreFin, semestreInicio);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		EtapaDeFormacionDTO other = (EtapaDeFormacionDTO) obj;
		return numero == other.numero && Objects.equals(nombre, other.nombre)
				&& Objects.equals(meta, other.meta) && semestreInicio == other.semestreInicio
				&& semestreFin == other.semestreFin;
	}

}