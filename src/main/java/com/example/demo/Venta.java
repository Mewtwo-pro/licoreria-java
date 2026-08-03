package com.example.demo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Integer idVenta;

    @Column(name = "fecha_venta", insertable = false, updatable = false)
    private LocalDateTime fechaVenta;

    @Column(name = "tipo_pago")
    private String tipoPago; // O puedes usar un Enum (Efectivo, Tarjeta)

    private BigDecimal total;

    @ManyToOne
    @JoinColumn(name = "id_usuario_cajero", nullable = false)
    private Usuario usuarioCajero;

    // Relación inversa para jalar todos los items de esta venta
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleVenta> detalles;

    // --- MÉTODO AUXILIAR PARA EL RECUADRO "Categoría/Items Principales" ---
    public String getResumenItems() {
        if (detalles == null || detalles.isEmpty()) {
            return "Sin items"; //[cite: 1]
        }
        
        // Obtenemos el nombre del primer producto vendido en esta venta
        String primerProducto = detalles.get(0).getProducto().getNombreProducto();
        
        // Si hay más de un detalle/producto diferente, le concatenamos el "..."
        if (detalles.size() > 1) {
            return primerProducto + "...";
        }
        
        return primerProducto;
    }
    
        
    // --- GETTERS Y SETTERS ---
    public Integer getIdVenta() { return idVenta; }
    public void setIdVenta(Integer idVenta) { this.idVenta = idVenta; }

    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDateTime fechaVenta) { this.fechaVenta = fechaVenta; }

    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public Usuario getUsuarioCajero() { return usuarioCajero; }
    public void setUsuarioCajero(Usuario usuarioCajero) { this.usuarioCajero = usuarioCajero; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
}
