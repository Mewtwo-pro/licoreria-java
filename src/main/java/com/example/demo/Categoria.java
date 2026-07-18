package com.example.demo;

import jakarta.persistence.*;
import java.lang.Double;

@Entity
@Table(name = "categorias")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Integer idCategoria;
    
    @Column(name = "nombre_categoria" , nullable = false)
    private String nombreCategoria;
    
    public Integer getIdCategoria() {return idCategoria;}
    public void setIdCategoria(Integer idCategoria){this.idCategoria = idCategoria;}
    
    public String getNombreCategoria(){return nombreCategoria;}
    public void setNombreCategoria(String nombreCategoria){this.nombreCategoria = nombreCategoria;}
}

