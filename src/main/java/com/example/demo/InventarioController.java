package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
    
@Controller 
public class InventarioController {

    @Autowired 
    private CategoriaRepository categoriaRepository;
    
    @Autowired 
    private ProductoRepository productoRepository;
    
    @Autowired 
    private UsuarioRepository usuarioRepository;
        
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;
    
    @GetMapping("/inventario") 
    public String mostrarInventario(HttpSession session ,  Model model){
        String username = (String) session.getAttribute("userUsername");
        if (username == null) return "redirect:/?error=no-session";
        
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(username);
        usuarioEncontrado.ifPresent(usuario -> model.addAttribute("usuarioLogueado" , usuario));
        
        List<Producto> listaProductos = productoRepository.findAll();
        model.addAttribute("productos", listaProductos);
        
        List<Categoria> listaCategoria = categoriaRepository.findAll();
        model.addAttribute("categorias", listaCategoria);
        
        return "/inventario";
    }
        
// ... Tus otros @Autowired e imports permanentes del archivo

    @PostMapping("/inventario/editar")
    public String modificarProducto(
            @RequestParam("idProducto") Integer idProducto,
            @RequestParam("sku") String sku, // <-- AGREGADO
            @RequestParam("precioVenta") Double precioVenta,
            @RequestParam("stockCritico") Integer stockCritico,
            @RequestParam("ubicacionAlmacen") String ubicacionAlmacen,
            @RequestParam(value = "imagenUrl", required = false) String imagenUrl,
            RedirectAttributes redirectAttributes) {

        // 1. Buscamos el producto en la base de datos por su ID único
        Optional<Producto> productoOpt = productoRepository.findById(idProducto);

        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();

            // 2. Modificamos únicamente los campos solicitados por el requerimiento
            producto.setSku(sku); // <-- AGREGADO
            producto.setPrecioVenta(precioVenta);
            producto.setStockCritico(stockCritico);
            producto.setUbicacionAlmacen(ubicacionAlmacen);
            
            if (imagenUrl != null && !imagenUrl.trim().isEmpty()) {
                producto.setImagenUrl(imagenUrl);
            } else {
                producto.setImagenUrl("/images/puntoVenta/whisky.png"); // Imagen por defecto si viene vacía[cite: 2]
            }

            // 3. Guardamos los cambios efectuados directamente en MySQL
            productoRepository.save(producto);

            // Mensaje de éxito mediante Flash Attributes para persistir tras la redirección
            redirectAttributes.addFlashAttribute("mensaje", "¡Producto '" + producto.getNombreProducto() + "' actualizado correctamente!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error: El producto seleccionado no existe.");
        }

        // Redireccionamos de vuelta al almacén para que refleje los datos actualizados inmediatamente[cite: 5]
        return "redirect:/inventario";
    }
 // ... Tus métodos previos del controlador[cite: 5]

    @PostMapping("/inventario/eliminar")
    public String eliminarProducto(@RequestParam("idProducto") Integer idProducto, RedirectAttributes redirectAttributes) {
        try {
            // 1. Verificamos si el producto existe antes de intentar removerlo
            Optional<Producto> productoOpt = productoRepository.findById(idProducto);

            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();
                String nombreProducto = producto.getNombreProducto();

                // 2. Ejecutamos la eliminación en la base de datos
                productoRepository.delete(producto);

                // Mensaje de éxito si todo marcha bien
                redirectAttributes.addFlashAttribute("mensaje", "El producto '" + nombreProducto + "' ha sido eliminado con éxito.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Error: El producto seleccionado ya no existe en el sistema.");
            }
        } catch (Exception e) {
            // Manejo de restricciones de clave foránea (Por ejemplo, si el producto ya está amarrado a una venta existente)
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar este producto porque cuenta con historial de movimientos o ventas registradas.");
        }

        // Redireccionamos a la vista de inventario para refrescar la tabla sin el elemento borrado[cite: 5]
        return "redirect:/inventario";
    }       
        
            
@GetMapping("/inventario/api/movimientos")
@ResponseBody
public List<MovimientoDTO> obtenerHistorialMovimientos(@RequestParam("idProducto") Integer idProducto) {
    List<MovimientoDTO> historial = new ArrayList<>();
    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    productoRepository.findById(idProducto).ifPresent(producto -> {
        
        // 1. SALIDAS: Buscamos todas las ventas que incluyan este producto
        List<DetalleVenta> ventasDelProducto = detalleVentaRepository.findByProducto(producto);
        for (DetalleVenta dv : ventasDelProducto) {
            Venta ventaPadre = dv.getVenta(); 
            java.time.LocalDateTime fechaVenta = ventaPadre.getFechaVenta(); 
            String fechaFormateada = fechaVenta.format(formatter);
            
            long timestampMili = fechaVenta.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            
            historial.add(new MovimientoDTO(
                fechaFormateada,
                "SALIDA (VENTA)",
                dv.getCantidad(),
                dv.getPrecioUnitario().doubleValue(), // Convertimos el BigDecimal de tu entidad a Double
                "Venta registrada. Cajero: " + ventaPadre.getUsuarioCajero().getNombreCompleto() + " [Pago: " + ventaPadre.getTipoPago() + "]",
                timestampMili
            ));
        }

        // 2. ENTRADAS: Usamos el precio de compra y stock de tu entidad Producto
        if (producto.getPrecioCompraProveedor() != null) {
            java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
            String fechaEntrada = ahora.format(formatter);
            long timestampAhora = ahora.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            
            String proveedorNombre = (producto.getProveedorPrincipal() != null) ? producto.getProveedorPrincipal().getNombreProveedor() : "Carga del Sistema";
            
            historial.add(new MovimientoDTO(
                fechaEntrada,
                "ENTRADA (COMPRA)",
                producto.getStockActual(), 
                producto.getPrecioCompraProveedor(),
                "Ingreso de mercadería / Stock inicial del Proveedor: " + proveedorNombre,
                timestampAhora
            ));
        }
    });

    return historial;
}

@GetMapping("/inventario/api/bajo-stock")
    @ResponseBody // Indica que la respuesta se enviará estructurada en formato JSON puro[cite: 3]
    public List<Producto> obtenerProductosBajoStock() {
        // 1. Traemos la lista completa de productos desde la base de datos[cite: 5]
        List<Producto> todosLosProductos = productoRepository.findAll();

        // 2. Filtramos usando la regla del negocio: Stock Actual <= Stock Crítico[cite: 2, 8]
        return todosLosProductos.stream()
                .filter(p -> p.getStockActual() <= p.getStockCritico())
                .toList(); // Retornamos únicamente los licores en alerta desabastecimiento
    }   

}
