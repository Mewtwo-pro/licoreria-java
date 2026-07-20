
package com.example.demo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes_compra") // Vincula la clase con la tabla exacta de MySQL
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Integer idOrden;

    // Relación Muchos a Uno: Muchas órdenes pueden pertenecer a un mismo Proveedor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "estado", length = 50)
    private String estado; // Almacenará 'Pendiente' o 'Recibido' según tu ENUM

    @Column(name = "fecha_orden", insertable = false, updatable = false)
    private LocalDateTime fechaOrden; // 'insertable/updatable = false' deja que MySQL maneje el CURRENT_TIMESTAMP

    // ========================================================
    // CONSTRUCTORES
    // ========================================================
    public OrdenCompra() {
    }

    public OrdenCompra(Proveedor proveedor, BigDecimal montoTotal, String estado) {
        this.proveedor = proveedor;
        this.montoTotal = montoTotal;
        this.estado = estado;
    }

    // ========================================================
    // GETTERS Y SETTERS
    // ========================================================
    public Integer getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(Integer idOrden) {
        this.idOrden = idOrden;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaOrden() {
        return fechaOrden;
    }

    public void setFechaOrden(LocalDateTime fechaOrden) {
        this.fechaOrden = fechaOrden;
    }
}
