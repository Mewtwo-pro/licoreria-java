package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_delivery")
    private Integer idDelivery;

    @OneToOne
    @JoinColumn(name = "id_venta", nullable = false, unique = true)
    private Venta venta;

    @Column(name = "cliente_nombre", nullable = false, length = 150)
    private String clienteNombre;

    @Column(name = "cliente_telefono", nullable = false, length = 20)
    private String clienteTelefono;

    @Column(name = "direccion_envio", nullable = false, length = 255)
    private String direccionEnvio;

    @Column(name = "referencia", length = 255)
    private String referencia;

    @Column(name = "estado_delivery")
    private String estadoDelivery;

    @Column(name = "tiempo_promedio_min")
    private Integer tiempoPromedioMin;

    // =========================================================================
    // ⚠️ ESTO ES LO QUE TE FALTA AGREGAR: Relación con Repartidor
    // =========================================================================
    @ManyToOne
    @JoinColumn(name = "id_repartidor") // Hace match con tu columna en MySQL[cite: 4]
    private Repartidor repartidor; 

// 1. Añade la propiedad arriba con las otras columnas
@Column(name = "fecha_pedido", insertable = false, updatable = false)
private java.time.LocalDateTime fechaPedido;

// 2. Añade sus Getters y Setters abajo
public java.time.LocalDateTime getFechaPedido() { 
    return fechaPedido; 
}
public void setFechaPedido(java.time.LocalDateTime fechaPedido) { 
    this.fechaPedido = fechaPedido; 
}
    
    // El método que el controlador no encontraba:
    public Repartidor getRepartidor() { return repartidor; }
    public void setRepartidor(Repartidor repartidor) { this.repartidor = repartidor; }

    public Integer getIdDelivery() { return idDelivery; }
    public void setIdDelivery(Integer idDelivery) { this.idDelivery = idDelivery; }
    
    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }
    
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    
    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }
    
    public String getDireccionEnvio() { return direccionEnvio; }
    public void setDireccionEnvio(String direccionEnvio) { this.direccionEnvio = direccionEnvio; }
    
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    
    public String getEstadoDelivery() { return estadoDelivery; }
    public void setEstadoDelivery(String estadoDelivery) { this.estadoDelivery = estadoDelivery; }
    
    public Integer getTiempoPromedioMin() { return tiempoPromedioMin; }
    public void setTiempoPromedioMin(Integer tiempoPromedioMin) { this.tiempoPromedioMin = tiempoPromedioMin; }
}
