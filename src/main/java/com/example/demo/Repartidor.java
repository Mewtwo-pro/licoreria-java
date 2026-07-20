package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "repartidores")
public class Repartidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_repartidor")
    private Integer idRepartidor;

    @Column(name = "nombre_repartidor", nullable = false, length = 150)
    private String nombreRepartidor;

    @Column(name = "vehiculo", nullable = false, length = 50)
    private String vehiculo;

    @Column(name = "estado")
    private String estado; // 'Libre' o 'En Ruta'[cite: 4]

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================
    public Integer getIdRepartidor() { return idRepartidor; }
    public void setIdRepartidor(Integer idRepartidor) { this.idRepartidor = idRepartidor; }

    public String getNombreRepartidor() { return nombreRepartidor; }
    public void setNombreRepartidor(String nombreRepartidor) { this.nombreRepartidor = nombreRepartidor; }

    public String getVehiculo() { return vehiculo; }
    public void setVehiculo(String vehiculo) { this.vehiculo = vehiculo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
