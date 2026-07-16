package com.example.demo;
    
import jakarta.persistence.*;
import java.lang.Double;
    
@Entity
@Table(name = "proveedores") 
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Integer idProveedor; 
        
    @Column(name = "nombre_proveedor" , nullable = false)
    private String nombreProveedor; 
       
    
    @Column(name = "email" , nullable = false)
    private String emailProveedor;
    
    public Integer getIdProveedor() { return idProveedor;}
    public void setIdProducto(Integer idProveedor ){ this.idProveedor = idProveedor;}
   
    public String getNombreProveedor(){ return nombreProveedor;}
    public void setNombreProveedor(String nombreProveedor ){this.nombreProveedor = nombreProveedor;}
    
    public String getEmailProveedor() { return emailProveedor;}
    public void setEmailProveedor(String emailProveedor){this.emailProveedor = emailProveedor;}
    
}
