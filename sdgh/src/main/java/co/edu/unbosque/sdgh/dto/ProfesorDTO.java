package co.edu.unbosque.sdgh.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfesorDTO extends UsuarioDTO {

	private String tituloPregrado;

	private String actaGradoPregrado;

	private String tituloPosgrado;

	private String nivelPosgrado;

	private boolean tituloExtranjero;

	private String resolucionConvalidacion;

	private int horasFormacionPedagogica;

	private String maximoNivelFormacion;

	private String tipoDocumento;

	private String numeroDocumento;

	private String tarjetaProfesional;

	private String rut;

	private String eps;

	private String fondoPensiones;

	private boolean tieneVisaTrabajo;

	private List<String> especialidades;

	private int aniosExperiencia;

	private List<String> publicaciones;

	private String nivelIngles;

	private String examenIngles;

	private double puntajeIngles;

	private boolean hojaVidaActualizada;

	public ProfesorDTO() {
		super();
		this.especialidades = new ArrayList<>();
		this.publicaciones = new ArrayList<>();
	}

	public ProfesorDTO(String nombre, int codigo, String correo, String facultad) {
		super(nombre, codigo, correo, facultad);
		this.especialidades = new ArrayList<>();
		this.publicaciones = new ArrayList<>();
	}

	public ProfesorDTO(String nombre, int codigo, String correo, String facultad,
			String tituloPregrado, String actaGradoPregrado, String tituloPosgrado,
			String nivelPosgrado, boolean tituloExtranjero, String resolucionConvalidacion,
			int horasFormacionPedagogica, String maximoNivelFormacion, String tipoDocumento,
			String numeroDocumento, String tarjetaProfesional, String rut, String eps,
			String fondoPensiones, boolean tieneVisaTrabajo, List<String> especialidades,
			int aniosExperiencia, List<String> publicaciones, String nivelIngles,
			String examenIngles, double puntajeIngles, boolean hojaVidaActualizada) {
		super(nombre, codigo, correo, facultad);
		this.tituloPregrado = tituloPregrado;
		this.actaGradoPregrado = actaGradoPregrado;
		this.tituloPosgrado = tituloPosgrado;
		this.nivelPosgrado = nivelPosgrado;
		this.tituloExtranjero = tituloExtranjero;
		this.resolucionConvalidacion = resolucionConvalidacion;
		this.horasFormacionPedagogica = horasFormacionPedagogica;
		this.maximoNivelFormacion = maximoNivelFormacion;
		this.tipoDocumento = tipoDocumento;
		this.numeroDocumento = numeroDocumento;
		this.tarjetaProfesional = tarjetaProfesional;
		this.rut = rut;
		this.eps = eps;
		this.fondoPensiones = fondoPensiones;
		this.tieneVisaTrabajo = tieneVisaTrabajo;
		this.especialidades = especialidades != null ? new ArrayList<>(especialidades) : new ArrayList<>();
		this.aniosExperiencia = aniosExperiencia;
		this.publicaciones = publicaciones != null ? new ArrayList<>(publicaciones) : new ArrayList<>();
		this.nivelIngles = nivelIngles;
		this.examenIngles = examenIngles;
		this.puntajeIngles = puntajeIngles;
		this.hojaVidaActualizada = hojaVidaActualizada;
	}

	public String getTituloPregrado() {
		return tituloPregrado;
	}

	public void setTituloPregrado(String tituloPregrado) {
		this.tituloPregrado = tituloPregrado;
	}

	public String getActaGradoPregrado() {
		return actaGradoPregrado;
	}

	public void setActaGradoPregrado(String actaGradoPregrado) {
		this.actaGradoPregrado = actaGradoPregrado;
	}

	public String getTituloPosgrado() {
		return tituloPosgrado;
	}

	public void setTituloPosgrado(String tituloPosgrado) {
		this.tituloPosgrado = tituloPosgrado;
	}

	public String getNivelPosgrado() {
		return nivelPosgrado;
	}

	public void setNivelPosgrado(String nivelPosgrado) {
		this.nivelPosgrado = nivelPosgrado;
	}

	public boolean isTituloExtranjero() {
		return tituloExtranjero;
	}

	public void setTituloExtranjero(boolean tituloExtranjero) {
		this.tituloExtranjero = tituloExtranjero;
	}

	public String getResolucionConvalidacion() {
		return resolucionConvalidacion;
	}

	public void setResolucionConvalidacion(String resolucionConvalidacion) {
		this.resolucionConvalidacion = resolucionConvalidacion;
	}

	public int getHorasFormacionPedagogica() {
		return horasFormacionPedagogica;
	}

	public void setHorasFormacionPedagogica(int horasFormacionPedagogica) {
		this.horasFormacionPedagogica = horasFormacionPedagogica;
	}

	public String getMaximoNivelFormacion() {
		return maximoNivelFormacion;
	}

	public void setMaximoNivelFormacion(String maximoNivelFormacion) {
		this.maximoNivelFormacion = maximoNivelFormacion;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public String getTarjetaProfesional() {
		return tarjetaProfesional;
	}

	public void setTarjetaProfesional(String tarjetaProfesional) {
		this.tarjetaProfesional = tarjetaProfesional;
	}

	public String getRut() {
		return rut;
	}

	public void setRut(String rut) {
		this.rut = rut;
	}

	public String getEps() {
		return eps;
	}

	public void setEps(String eps) {
		this.eps = eps;
	}

	public String getFondoPensiones() {
		return fondoPensiones;
	}

	public void setFondoPensiones(String fondoPensiones) {
		this.fondoPensiones = fondoPensiones;
	}

	public boolean isTieneVisaTrabajo() {
		return tieneVisaTrabajo;
	}

	public void setTieneVisaTrabajo(boolean tieneVisaTrabajo) {
		this.tieneVisaTrabajo = tieneVisaTrabajo;
	}

	public List<String> getEspecialidades() {
		return especialidades;
	}

	public void setEspecialidades(List<String> especialidades) {
		this.especialidades = especialidades;
	}

	public int getAniosExperiencia() {
		return aniosExperiencia;
	}

	public void setAniosExperiencia(int aniosExperiencia) {
		this.aniosExperiencia = aniosExperiencia;
	}

	public List<String> getPublicaciones() {
		return publicaciones;
	}

	public void setPublicaciones(List<String> publicaciones) {
		this.publicaciones = publicaciones;
	}

	public String getNivelIngles() {
		return nivelIngles;
	}

	public void setNivelIngles(String nivelIngles) {
		this.nivelIngles = nivelIngles;
	}

	public String getExamenIngles() {
		return examenIngles;
	}

	public void setExamenIngles(String examenIngles) {
		this.examenIngles = examenIngles;
	}

	public double getPuntajeIngles() {
		return puntajeIngles;
	}

	public void setPuntajeIngles(double puntajeIngles) {
		this.puntajeIngles = puntajeIngles;
	}

	public boolean isHojaVidaActualizada() {
		return hojaVidaActualizada;
	}

	public void setHojaVidaActualizada(boolean hojaVidaActualizada) {
		this.hojaVidaActualizada = hojaVidaActualizada;
	}

	@Override
	public String toString() {
		return "ProfesorDTO [" + super.toString() + ", tituloPregrado=" + tituloPregrado
			+ ", actaGradoPregrado=" + actaGradoPregrado + ", tituloPosgrado=" + tituloPosgrado
			+ ", nivelPosgrado=" + nivelPosgrado + ", tituloExtranjero=" + tituloExtranjero
			+ ", resolucionConvalidacion=" + resolucionConvalidacion + ", horasFormacionPedagogica="
			+ horasFormacionPedagogica + ", maximoNivelFormacion=" + maximoNivelFormacion
			+ ", tipoDocumento=" + tipoDocumento + ", numeroDocumento=" + numeroDocumento
			+ ", tarjetaProfesional=" + tarjetaProfesional + ", rut=" + rut + ", eps=" + eps
			+ ", fondoPensiones=" + fondoPensiones + ", tieneVisaTrabajo=" + tieneVisaTrabajo
			+ ", especialidades=" + especialidades + ", aniosExperiencia=" + aniosExperiencia
			+ ", publicaciones=" + publicaciones + ", nivelIngles=" + nivelIngles + ", examenIngles="
			+ examenIngles + ", puntajeIngles=" + puntajeIngles + ", hojaVidaActualizada="
			+ hojaVidaActualizada + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(actaGradoPregrado, aniosExperiencia, eps, especialidades,
				examenIngles, fondoPensiones, hojaVidaActualizada, horasFormacionPedagogica,
				maximoNivelFormacion, nivelIngles, nivelPosgrado, numeroDocumento, publicaciones,
				puntajeIngles, resolucionConvalidacion, rut, tarjetaProfesional, tieneVisaTrabajo,
				tipoDocumento, tituloExtranjero, tituloPosgrado, tituloPregrado);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProfesorDTO other = (ProfesorDTO) obj;
		return aniosExperiencia == other.aniosExperiencia
				&& hojaVidaActualizada == other.hojaVidaActualizada
				&& horasFormacionPedagogica == other.horasFormacionPedagogica
				&& Double.doubleToLongBits(puntajeIngles) == Double.doubleToLongBits(other.puntajeIngles)
				&& tieneVisaTrabajo == other.tieneVisaTrabajo
				&& tituloExtranjero == other.tituloExtranjero
				&& Objects.equals(actaGradoPregrado, other.actaGradoPregrado)
				&& Objects.equals(eps, other.eps)
				&& Objects.equals(especialidades, other.especialidades)
				&& Objects.equals(examenIngles, other.examenIngles)
				&& Objects.equals(fondoPensiones, other.fondoPensiones)
				&& Objects.equals(maximoNivelFormacion, other.maximoNivelFormacion)
				&& Objects.equals(nivelIngles, other.nivelIngles)
				&& Objects.equals(nivelPosgrado, other.nivelPosgrado)
				&& Objects.equals(numeroDocumento, other.numeroDocumento)
				&& Objects.equals(publicaciones, other.publicaciones)
				&& Objects.equals(resolucionConvalidacion, other.resolucionConvalidacion)
				&& Objects.equals(rut, other.rut)
				&& Objects.equals(tarjetaProfesional, other.tarjetaProfesional)
				&& Objects.equals(tipoDocumento, other.tipoDocumento)
				&& Objects.equals(tituloPosgrado, other.tituloPosgrado)
				&& Objects.equals(tituloPregrado, other.tituloPregrado);
	}

}
