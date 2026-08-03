package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {
    // Agregamos este método para poder buscar los detalles por producto
    List<DetalleVenta> findByProducto(Producto producto);
}
