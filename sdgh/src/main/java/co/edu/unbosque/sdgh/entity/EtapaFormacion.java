package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

public class EtapaFormacion {

    private int numero;                 // 1, 2 o 3
    private String nombre;              // "Inmersión a la vida universitaria", etc.
    private String meta;                // La meta declarada en el PEP
    private int semestreInicio;
    private int semestreFin;

    public EtapaFormacion() {
    }

    public EtapaFormacion(int numero, String nombre, String meta, int semestreInicio, int semestreFin) {
        this.numero = numero;
        this.nombre = nombre;
        this.meta = meta;
        this.semestreInicio = semestreInicio;
        this.semestreFin = semestreFin;
    }

    // Getters y Setters
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMeta() { return meta; }
    public void setMeta(String meta) { this.meta = meta; }

    public int getSemestreInicio() { return semestreInicio; }
    public void setSemestreInicio(int semestreInicio) { this.semestreInicio = semestreInicio; }

    public int getSemestreFin() { return semestreFin; }
    public void setSemestreFin(int semestreFin) { this.semestreFin = semestreFin; }

    @Override
    public String toString() {
        return "EtapaFormacion [numero=" + numero + ", nombre=" + nombre + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero, nombre);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EtapaFormacion other = (EtapaFormacion) obj;
        return numero == other.numero && Objects.equals(nombre, other.nombre);
    }
}