package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class PuntoVentaController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/puntoVenta")
    public String mostrarPuntoVenta(HttpSession session, Model model) {
        String username = (String) session.getAttribute("userUsername");
        if (username == null) return "redirect:/?error=no-session";

        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(username);
        usuarioEncontrado.ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));

        List<Producto> listaProductos = productoRepository.findAll();
        model.addAttribute("productos", listaProductos);

        return "puntoVenta";
    }

    @PostMapping("/puntoVenta/pagar")
    public String procesarPago(
            @RequestParam("total") Double total,
            @RequestParam("tipoPago") String tipoPago,
            @RequestParam(value = "productosIds", required = false) List<Integer> productosIds,
            @RequestParam(value = "cantidades", required = false) List<Integer> cantidades,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String username = (String) session.getAttribute("userUsername");
        if (username == null) return "redirect:/?error=no-session";

        if (productosIds == null || productosIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorStock", "El carrito está vacío.");
            return "redirect:/puntoVenta";
        }

        // =========================================================
        // PASO 1: VERIFICAR EL STOCK DE TODOS LOS PRODUCTOS PRIMERO
        // =========================================================
        for (int i = 0; i < productosIds.size(); i++) {
            Integer idProd = productosIds.get(i);
            Integer cantPedida = cantidades.get(i);

            Optional<Producto> optProd = productoRepository.findById(idProd);
            if (optProd.isPresent()) {
                Producto prod = optProd.get();
                if (prod.getStockActual() < cantPedida) {
                    redirectAttributes.addFlashAttribute("errorStock", 
                        "¡Stock insuficiente! Solo quedan " + prod.getStockActual() + " unidades de " + prod.getNombreProducto());
                    return "redirect:/puntoVenta";
                }
            }
        }

        // Buscamos al usuario de la sesión para asociarlo a la venta
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(username);
        if (!usuarioEncontrado.isPresent()) {
            return "redirect:/?error=no-session";
        }
        Usuario cajeroActual = usuarioEncontrado.get();

        // =========================================================
        // PASO 2: CREAR LA CABECERA DE LA VENTA
        // =========================================================
        Venta nuevaVenta = new Venta();
        nuevaVenta.setTotal(BigDecimal.valueOf(total)); // Convertimos Double a BigDecimal para JPA
        nuevaVenta.setTipoPago(tipoPago);
        nuevaVenta.setUsuarioCajero(cajeroActual); // Ahora pasamos el OBJETO Usuario completo

        // Preparamos la lista donde guardaremos los detalles correlacionados
        List<DetalleVenta> detallesLista = new ArrayList<>();

        // =========================================================
        // PASO 3: DESCONTAR STOCK Y ARMAR LOS DETALLES
        // =========================================================
        for (int i = 0; i < productosIds.size(); i++) {
            Integer idProd = productosIds.get(i);
            Integer cantPedida = cantidades.get(i);

            Producto prod = productoRepository.findById(idProd).get();
            
            // Restamos el stock y actualizamos en BD
            prod.setStockActual(prod.getStockActual() - cantPedida);
            productoRepository.save(prod);

            // Creamos el detalle correspondiente para la tabla 'detalle_ventas'
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(nuevaVenta); // Lo vinculamos a la venta actual
            detalle.setProducto(prod);
            detalle.setCantidad(cantPedida);
            detalle.setPrecioUnitario(BigDecimal.valueOf(prod.getPrecioVenta())); // Tomamos el precio del producto

            detallesLista.add(detalle);
        }

        // Vinculamos la lista de detalles a la venta principal
        nuevaVenta.setDetalles(detallesLista);

        // =========================================================
        // PASO 4: GUARDAR TODO (Cabecera y Detalles en Cascada)
        // =========================================================
        // Gracias al CascadeType.ALL que pusimos en Venta.java, al salvar la venta se guardan los detalles automáticamente.
        ventaRepository.save(nuevaVenta);

        redirectAttributes.addFlashAttribute("successVenta", "¡Venta registrada con éxito y stock actualizado!");
        return "redirect:/puntoVenta";
    }
}
