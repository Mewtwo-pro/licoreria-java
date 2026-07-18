package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller 
public class ProveedorController {

    @Autowired 
    private CategoriaRepository categoriaRepository;
    
    @Autowired 
    private ProductoRepository productoRepository;
    
    @Autowired 
    private UsuarioRepository usuarioRepository;

    @Autowired 
    private ProveedorRepository proveedorRepository;

    @Autowired 
    private OrdenCompraRepository ordenCompraRepository;
    
    @GetMapping("/proveedor") 
    public String mostrarProveedor(
            @RequestParam(name = "buscar", required = false) String buscar,
            @RequestParam(name = "categoriaId", required = false) Integer categoriaId,
            HttpSession session, 
            Model model) {
            
        String username = (String) session.getAttribute("userUsername");
        if (username == null) return "redirect:/?error=no-session";
        
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(username);
        usuarioEncontrado.ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
        
        // Lógica de filtrado de la tabla de proveedores
        List<Proveedor> listaProveedores;
        if (buscar != null && !buscar.trim().isEmpty()) {
            listaProveedores = proveedorRepository.buscarPorNombreOContacto(buscar);
        } else {
            listaProveedores = proveedorRepository.findAll();
        }
        
        model.addAttribute("proveedores", listaProveedores);
        model.addAttribute("totalProveedores", listaProveedores.size());
        
        // Lista de categorías para el select del filtro y del modal
        List<Categoria> listaCategoria = categoriaRepository.findAll();
        model.addAttribute("categorias", listaCategoria);
        
        return "proveedor";
    }

    @PostMapping("/proveedor/guardar-orden-detallada")
    public String guardarOrdenDetallada(
            @RequestParam("idProveedor") Integer idProveedor,
            @RequestParam("nombresProductos[]") List<String> nombres,
            @RequestParam("categoriasProductos[]") List<Integer> categoriasIds,
            @RequestParam("preciosCompra[]") List<Double> preciosCompra, // Cambiado a List<Double>
            @RequestParam("cantidadesStock[]") List<Integer> cantidadesStock,
            RedirectAttributes redirectAttributes) {

        Optional<Proveedor> provOpt = proveedorRepository.findById(idProveedor);
        if (!provOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Proveedor no válido.");
            return "redirect:/proveedor";
        }
        
        Proveedor proveedor = provOpt.get();
        double montoTotalOrden = 0.0; // Cambiado a double para acumular la orden

        // Recorrer los productos del formulario dinámico usando tu entidad exacta
        for (int i = 0; i < nombres.size(); i++) {
            Producto nuevoProducto = new Producto();
            
            // Seteamos usando tus métodos exactos
            nuevoProducto.setNombreProducto(nombres.get(i));
            nuevoProducto.setPrecioCompraProveedor(preciosCompra.get(i)); // <-- Tu setter exacto
            nuevoProducto.setStockActual(cantidadesStock.get(i));
            nuevoProducto.setStockCritico(cantidadesStock.get(i)); // Regla del stock crítico
            nuevoProducto.setProveedorPrincipal(proveedor); // <-- Tu setter exacto
            
            Optional<Categoria> catOpt = categoriaRepository.findById(categoriasIds.get(i));
            if (catOpt.isPresent()) {
                nuevoProducto.setCategoriaProducto(catOpt.get()); // <-- Tu setter exacto
            }

            // Atributos automáticos constantes que definiste en tu entidad
            nuevoProducto.setSku("SKU-" + System.currentTimeMillis() + "-" + i);
            nuevoProducto.setUbicacionAlmacen("General"); 
            
            // Margen del 30%: Multiplicamos tu Double de compra por 1.30
            nuevoProducto.setPrecioVenta(preciosCompra.get(i) * 1.30); 
            nuevoProducto.setImagenUrl("/images/puntoVenta/whisky.png"); 

            // Guardamos el nuevo producto en la tabla 'productos'
            productoRepository.save(nuevoProducto);

            // Calcular costo acumulado de la orden de compra en double
            double costoBloque = preciosCompra.get(i) * cantidadesStock.get(i);
            montoTotalOrden += costoBloque;
        }

        // Registrar la orden de compra principal
        // Nota: Como OrdenCompra usa BigDecimal en BD, convertimos el double total acumulado aquí
        BigDecimal totalFinalBD = new BigDecimal(montoTotalOrden).setScale(2, java.math.RoundingMode.HALF_UP);
        OrdenCompra orden = new OrdenCompra(proveedor, totalFinalBD, "Pendiente");
        ordenCompraRepository.save(orden);

        redirectAttributes.addFlashAttribute("mensaje", "¡Órden de compra registrada con éxito!");
        return "redirect:/proveedor";
    }
    @PostMapping("/proveedor/guardar")
    public String guardarNuevoProveedor(
            @RequestParam("nombreProveedor") String nombreProveedor,
            @RequestParam(value = "ruc", required = false) String ruc,
            @RequestParam(value = "contacto", required = false) String contacto,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "email", required = false) String email,
            RedirectAttributes redirectAttributes) {

        try {
            // Creamos la instancia usando el constructor que definiste en tu Proveedor.java
            Proveedor nuevoProveedor = new Proveedor(nombreProveedor, ruc, contacto, telefono, email);
            
            // Guardamos en la base de datos MySQL
            proveedorRepository.save(nuevoProveedor);
            
            redirectAttributes.addFlashAttribute("mensaje", "¡Proveedor '" + nombreProveedor + "' agregado exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el proveedor. Verifique que el RUC no esté duplicado.");
        }

        // Redirecciona de vuelta para actualizar la tabla con el nuevo registro
        return "redirect:/proveedor";
    }
    // Este método devuelve datos puros (JSON) en lugar de una vista HTML
    @GetMapping("/proveedor/api/facturas")
    @ResponseBody
    public List<OrdenCompra> obtenerFacturasPorProveedor(@RequestParam("idProveedor") Integer idProveedor) {
        // Buscamos el proveedor y retornamos su lista interna de órdenes de compra
        return proveedorRepository.findById(idProveedor)
                .map(Proveedor::getOrdenesCompra)
                .orElse(List.of());
    }
}
