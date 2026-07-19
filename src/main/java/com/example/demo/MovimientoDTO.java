package com.example.demo;

public class MovimientoDTO {
    private String fecha;
    private String tipo; // ENTRADA (COMPRA) o SALIDA (VENTA)
    private Integer cantidad;
    private Double precioUnitario;
    private String detalle;
    private long timestamp; // Útil para ordenar las fechas en JS

    public MovimientoDTO(String fecha, String tipo, Integer cantidad, Double precioUnitario, String detalle, long timestamp) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.detalle = detalle;
        this.timestamp = timestamp;
    }

    // Getters indispensables para que Spring Boot los transforme a JSON automáticamente
    public String getFecha() { return fecha; }
    public String getTipo() { return tipo; }
    public Integer getCantidad() { return cantidad; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public String getDetalle() { return detalle; }
    public long getTimestamp() { return timestamp; }
}
