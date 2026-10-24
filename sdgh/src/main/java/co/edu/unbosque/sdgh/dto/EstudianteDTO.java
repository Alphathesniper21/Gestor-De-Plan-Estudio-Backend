package co.edu.unbosque.sdgh.dto;

import java.util.Objects;

public class EstudianteDTO extends UsuarioDTO {

	private int semestreActual;

	private boolean estado;

	private String programaAcademico;

	private String modalidad;

	private double promedioAcumulado;

	private String tipoDocumento;

	private String numeroDocumento;

	private String resultadoSaber11;

	private boolean tieneTituloBachiller;

	private String diplomaOActaBachiller;

	private String afiliacionEPS;

	private boolean esquemaVacunacionCompleto;

	private boolean esExtranjero;

	private boolean tieneVisaVigente;

	private String resolucionConvalidacion;

	public EstudianteDTO() {
		super();
	}

	public EstudianteDTO(String nombre, int codigo, String correo,
			String facultad, int semestreActual, boolean estado,
			String programaAcademico, String modalidad, double promedioAcumulado,
			String tipoDocumento, String numeroDocumento, String resultadoSaber11,
			boolean tieneTituloBachiller, String diplomaOActaBachiller, String afiliacionEPS,
			boolean esquemaVacunacionCompleto, boolean esExtranjero, boolean tieneVisaVigente,
			String resolucionConvalidacion) {
		super(nombre, codigo, correo, facultad);
		this.semestreActual = semestreActual;
		this.estado = estado;
		this.programaAcademico = programaAcademico;
		this.modalidad = modalidad;
		this.promedioAcumulado = promedioAcumulado;
		this.tipoDocumento = tipoDocumento;
		this.numeroDocumento = numeroDocumento;
		this.resultadoSaber11 = resultadoSaber11;
		this.tieneTituloBachiller = tieneTituloBachiller;
		this.diplomaOActaBachiller = diplomaOActaBachiller;
		this.afiliacionEPS = afiliacionEPS;
		this.esquemaVacunacionCompleto = esquemaVacunacionCompleto;
		this.esExtranjero = esExtranjero;
		this.tieneVisaVigente = tieneVisaVigente;
		this.resolucionConvalidacion = resolucionConvalidacion;
	}

	public int getSemestreActual() {
		return semestreActual;
	}

	public void setSemestreActual(int semestreActual) {
		this.semestreActual = semestreActual;
	}

	public boolean isEstado() {
		return estado;
	}

	public void setEstado(boolean estado) {
		this.estado = estado;
	}

	public String getProgramaAcademico() {
		return programaAcademico;
	}

	public void setProgramaAcademico(String programaAcademico) {
		this.programaAcademico = programaAcademico;
	}

	public String getModalidad() {
		return modalidad;
	}

	public void setModalidad(String modalidad) {
		this.modalidad = modalidad;
	}

	public double getPromedioAcumulado() {
		return promedioAcumulado;
	}

	public void setPromedioAcumulado(double promedioAcumulado) {
		this.promedioAcumulado = promedioAcumulado;
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

	public String getResultadoSaber11() {
		return resultadoSaber11;
	}

	public void setResultadoSaber11(String resultadoSaber11) {
		this.resultadoSaber11 = resultadoSaber11;
	}

	public boolean isTieneTituloBachiller() {
		return tieneTituloBachiller;
	}

	public void setTieneTituloBachiller(boolean tieneTituloBachiller) {
		this.tieneTituloBachiller = tieneTituloBachiller;
	}

	public String getDiplomaOActaBachiller() {
		return diplomaOActaBachiller;
	}

	public void setDiplomaOActaBachiller(String diplomaOActaBachiller) {
		this.diplomaOActaBachiller = diplomaOActaBachiller;
	}

	public String getAfiliacionEPS() {
		return afiliacionEPS;
	}

	public void setAfiliacionEPS(String afiliacionEPS) {
		this.afiliacionEPS = afiliacionEPS;
	}

	public boolean isEsquemaVacunacionCompleto() {
		return esquemaVacunacionCompleto;
	}

	public void setEsquemaVacunacionCompleto(boolean esquemaVacunacionCompleto) {
		this.esquemaVacunacionCompleto = esquemaVacunacionCompleto;
	}

	public boolean isEsExtranjero() {
		return esExtranjero;
	}

	public void setEsExtranjero(boolean esExtranjero) {
		this.esExtranjero = esExtranjero;
	}

	public boolean isTieneVisaVigente() {
		return tieneVisaVigente;
	}

	public void setTieneVisaVigente(boolean tieneVisaVigente) {
		this.tieneVisaVigente = tieneVisaVigente;
	}

	public String getResolucionConvalidacion() {
		return resolucionConvalidacion;
	}

	public void setResolucionConvalidacion(String resolucionConvalidacion) {
		this.resolucionConvalidacion = resolucionConvalidacion;
	}

	@Override
	public String toString() {
		return "EstudianteDTO [" + super.toString() + ", semestreActual=" + semestreActual + ", estado="
			+ estado + ", programaAcademico=" + programaAcademico + ", modalidad=" + modalidad
			+ ", promedioAcumulado=" + promedioAcumulado + ", tipoDocumento=" + tipoDocumento
			+ ", numeroDocumento=" + numeroDocumento + ", resultadoSaber11=" + resultadoSaber11
			+ ", tieneTituloBachiller=" + tieneTituloBachiller + ", diplomaOActaBachiller="
			+ diplomaOActaBachiller + ", afiliacionEPS=" + afiliacionEPS + ", esquemaVacunacionCompleto="
			+ esquemaVacunacionCompleto + ", esExtranjero=" + esExtranjero + ", tieneVisaVigente="
			+ tieneVisaVigente + ", resolucionConvalidacion=" + resolucionConvalidacion + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(afiliacionEPS, diplomaOActaBachiller, esExtranjero, esquemaVacunacionCompleto, estado, modalidad, numeroDocumento, programaAcademico, promedioAcumulado, resolucionConvalidacion, resultadoSaber11, semestreActual, tieneTituloBachiller, tieneVisaVigente, tipoDocumento);
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
		EstudianteDTO other = (EstudianteDTO) obj;
		return semestreActual == other.semestreActual && estado == other.estado
				&& Objects.equals(programaAcademico, other.programaAcademico) && Objects.equals(modalidad, other.modalidad)
				&& Double.doubleToLongBits(promedioAcumulado) == Double.doubleToLongBits(other.promedioAcumulado) && Objects.equals(tipoDocumento, other.tipoDocumento)
				&& Objects.equals(numeroDocumento, other.numeroDocumento) && Objects.equals(resultadoSaber11, other.resultadoSaber11)
				&& tieneTituloBachiller == other.tieneTituloBachiller && Objects.equals(diplomaOActaBachiller, other.diplomaOActaBachiller)
				&& Objects.equals(afiliacionEPS, other.afiliacionEPS) && esquemaVacunacionCompleto == other.esquemaVacunacionCompleto
				&& esExtranjero == other.esExtranjero && tieneVisaVigente == other.tieneVisaVigente
				&& Objects.equals(resolucionConvalidacion, other.resolucionConvalidacion);
	}

}