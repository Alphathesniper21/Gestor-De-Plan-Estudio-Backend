package co.edu.unbosque.sdgh.entity;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "espacios_conocimiento")
public class EspacioConocimiento {

    @Id
    private int id;

    private String nombre;              // "Personas", "Software", "Información", etc.
    private String descripcion;

    public EspacioConocimiento() {
    }

    public EspacioConocimiento(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return "EspacioConocimiento [id=" + id + ", nombre=" + nombre + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EspacioConocimiento other = (EspacioConocimiento) obj;
        return id == other.id && Objects.equals(nombre, other.nombre);
    }
}
