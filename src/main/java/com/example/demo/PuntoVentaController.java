package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
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
            RedirectAttributes redirectAttributes // Nos permite mandar mensajes de error al redirigir
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
                    // Si al menos uno no tiene stock, cancelamos todo y avisamos
                    redirectAttributes.addFlashAttribute("errorStock", 
                        "¡Stock insuficiente! Solo quedan " + prod.getStockActual() + " unidades de " + prod.getNombreProducto());
                    return "redirect:/puntoVenta";
                }
            }
        }

        // =========================================================
        // PASO 2: SI TODO TIENE STOCK, RESTAMOS E INVENTARIAMOS
        // =========================================================
        for (int i = 0; i < productosIds.size(); i++) {
            Integer idProd = productosIds.get(i);
            Integer cantPedida = cantidades.get(i);

            Producto prod = productoRepository.findById(idProd).get();
            prod.setStockActual(prod.getStockActual() - cantPedida); // Restamos el stock
            productoRepository.save(prod); // Actualizamos el producto en la BD
        }

        // =========================================================
        // PASO 3: REGISTRAR LA VENTA
        // =========================================================
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(username);
        if (usuarioEncontrado.isPresent()) {
            Usuario cajeroActual = usuarioEncontrado.get();

            Venta nuevaVenta = new Venta();
            nuevaVenta.setTotal(total);
            nuevaVenta.setTipoPago(tipoPago);
            nuevaVenta.setIdUsuarioCajero(cajeroActual.getIdUsuario());

            ventaRepository.save(nuevaVenta);
        }

        redirectAttributes.addFlashAttribute("successVenta", "¡Venta registrada con éxito y stock actualizado!");
        return "redirect:/puntoVenta";
    }
}
