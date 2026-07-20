
package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    // Busca proveedores cuyo nombre o contacto coincidan parcialmente con el texto
    @Query("SELECT p FROM Proveedor p WHERE p.nombreProveedor LIKE %:keyword% OR p.contacto LIKE %:keyword%")
    List<Proveedor> buscarPorNombreOContacto(@Param("keyword") String keyword);
}
