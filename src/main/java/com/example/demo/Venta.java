package com.example.demo;

import jakarta.persistence.*;
import java.lang.Double;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Integer idVenta;

    @Column(name = "tipo_pago", nullable = false)
    private String tipoPago;

    @Column(nullable = false)
    private Double total;

    @Column(name = "id_usuario_cajero")
    private Integer idUsuarioCajero;

    // Getters y Setters básicos
    public Integer getIdVenta() { return idVenta; }
    public void setIdVenta(Integer idVenta) { this.idVenta = idVenta; }
    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public Integer getIdUsuarioCajero() { return idUsuarioCajero; }
    public void setIdUsuarioCajero(Integer idUsuarioCajero) { this.idUsuarioCajero = idUsuarioCajero; }
}
