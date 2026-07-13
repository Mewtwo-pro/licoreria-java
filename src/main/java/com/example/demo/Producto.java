package com.example.demo;

import jakarta.persistence.*;
import java.lang.Double;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Column(name = "id_categoria", nullable = false)
    private Integer idCategoria;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;

    @Column(name = "precio_venta", nullable = false)
    private Double precioVenta;

    @Column(name = "precio_compra_proveedor", nullable = false)
    private Double precioCompraProveedor;

    @Column(name = "id_proveedor_principal", nullable = false)
    private Integer idProveedorPrincipal;

    @Column(name = "imagen_url")
    private String imagenUrl;

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public Integer getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Integer idCategoria) { this.idCategoria = idCategoria; }

    public Integer getStockActual() { return stockActual; }
    public void setStockActual(Integer stockActual) { this.stockActual = stockActual; }

    public Double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(Double precioVenta) { this.precioVenta = precioVenta; }

    public Double getPrecioCompraProveedor() { return precioCompraProveedor; }
    public void setPrecioCompraProveedor(Double precioCompraProveedor) { this.precioCompraProveedor = precioCompraProveedor; }

    public Integer getIdProveedorPrincipal() { return idProveedorPrincipal; }
    public void setIdProveedorPrincipal(Integer idProveedorPrincipal) { this.idProveedorPrincipal = idProveedorPrincipal; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}
