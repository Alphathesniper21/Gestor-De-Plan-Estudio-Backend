package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "materia")
public class Materia {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(unique = true, nullable = false)
	private String codigo;

	private String nombre;
	private int semestre;
	private int ncreditos;
	private boolean correlativa;

	public Materia() {
		super();
	}
	
	public Materia(String codigo, String nombre, int semestre, int ncreditos, boolean correlativa) {
		super();
		this.codigo = codigo;
		this.nombre = nombre;
		this.semestre = semestre;
		this.ncreditos = ncreditos;
		this.correlativa = correlativa;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getSemestre() {
		return semestre;
	}

	public void setSemestre(int semestre) {
		this.semestre = semestre;
	}

	public int getNcreditos() {
		return ncreditos;
	}

	public void setNcreditos(int ncreditos) {
		this.ncreditos = ncreditos;
	}

	public boolean isCorrelativa() {
		return correlativa;
	}

	public void setCorrelativa(boolean correlativa) {
		this.correlativa = correlativa;
	}

	/**
	 * Cantidad de sesiones semanales del curso. Se calcula a partir de las horas
	 * de la materia, porque cada sesion dura 2 horas (RF3, RF7).
	 * @return Retorna el numero de sesiones semanales.
	 */
	@Transient
	public int getHorasDeLaMateria() {
		int horasPorCredito = 1;
		return ncreditos * horasPorCredito;
	}
	
	@Transient
	public int getSesionesSemanales() {
		
		return (getHorasDeLaMateria() + 1) / 2;
	}

	@Override
	public int hashCode() {
		return Objects.hash(codigo, Boolean.valueOf(correlativa), Long.valueOf(id), Integer.valueOf(ncreditos), nombre,
				Integer.valueOf(semestre));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Materia other = (Materia) obj;
		return Objects.equals(codigo, other.codigo) && correlativa == other.correlativa && id == other.id
				&& ncreditos == other.ncreditos && Objects.equals(nombre, other.nombre) && semestre == other.semestre;
	}
}