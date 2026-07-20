package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

@Controller 
public class NewDeliveryController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private VentaRepository ventaRepository;
    @Autowired private DetalleVentaRepository detalleVentaRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    
    // 1. NUEVO: Necesitamos acceder a los motorizados
    @Autowired private RepartidorRepository repartidorRepository;
    
    @GetMapping("/newdelivery") 
    public String mostrarInventario(HttpSession session, Model model){
        String username = (String) session.getAttribute("userUsername");
        if (username == null) return "redirect:/?error=no-session";
        
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(username);
        usuarioEncontrado.ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
        
        List<Producto> listaProductos = productoRepository.findAll();
        model.addAttribute("productos", listaProductos);

        // 2. NUEVO: Enviamos los pedidos que están en mostrador esperando repartidor
        // (Asumiendo que tienes un método derivado o usas findAll filtrando en tu lógica)
        List<Delivery> pendientes = deliveryRepository.findAll().stream()
                .filter(d -> "En Preparación".equals(d.getEstadoDelivery()))
                .toList();
        model.addAttribute("deliveriesPendientes", pendientes);

        // 3. NUEVO: Enviamos los motorizados que están Libres para el select
        List<Repartidor> repartidoresLibres = repartidorRepository.findAll().stream()
                .filter(r -> "Libre".equals(r.getEstado()))
                .toList();
        model.addAttribute("repartidoresLibres", repartidoresLibres);
        
        // 4. NUEVO: Enviamos toda la flota completa para la lista de abajo
        model.addAttribute("todosLosRepartidores", repartidorRepository.findAll());
        
        return "newdelivery";
    }

    // ... Tu @PostMapping("/api/delivery/guardar") se queda exactamente igual como lo tienes[cite: 5]
    @PostMapping("/api/delivery/guardar")
    @ResponseBody
    @Transactional
    public Map<String, Object> guardarPedido(@RequestBody PedidoDeliveryDTO dto, HttpSession session) {
        Map<String, Object> respuesta = new HashMap<>();
        
        String username = (String) session.getAttribute("userUsername");
        if (username == null) {
            respuesta.put("success", false);
            respuesta.put("message", "Sesión no válida o expirada.");
            return respuesta;
        }
        
        Usuario cajero = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Cajero no encontrado"));

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            respuesta.put("success", false);
            respuesta.put("message", "El carrito de compras está vacío.");
            return respuesta;
        }

        try {
            double subtotal = 0.0;
            
            for (PedidoDeliveryDTO.ItemCarrito item : dto.getItems()) {
                Producto prod = productoRepository.findById(item.getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + item.getIdProducto()));
                
                if (prod.getStockActual() < item.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + prod.getNombreProducto());
                }
                subtotal += prod.getPrecioVenta() * item.getCantidad();
            }

            // --- SOLUCIÓN ERROR BIGDECIMAL EN VENTAS ---
            Venta venta = new Venta();
            venta.setTipoPago(dto.getTipoPago());
            
            // Convertimos el double primitivo a BigDecimal de forma segura
            BigDecimal totalFinal = BigDecimal.valueOf(subtotal + dto.getEnvio());
            venta.setTotal(totalFinal); 
            
            venta.setUsuarioCajero(cajero);
            venta = ventaRepository.save(venta);

            // Registrar DETALLE_VENTAS y actualizar stock
            for (PedidoDeliveryDTO.ItemCarrito item : dto.getItems()) {
                Producto prod = productoRepository.findById(item.getIdProducto()).get();
                
                DetalleVenta detalle = new DetalleVenta();
                detalle.setVenta(venta);
                detalle.setProducto(prod);
                detalle.setCantidad(item.getCantidad());
                
                // --- SOLUCIÓN ERROR BIGDECIMAL EN DETALLE ---
                // Como precioVenta en Producto.java es Double, lo pasamos a BigDecimal
                detalle.setPrecioUnitario(BigDecimal.valueOf(prod.getPrecioVenta()));
                
                detalleVentaRepository.save(detalle);

                prod.setStockActual(prod.getStockActual() - item.getCantidad());
                productoRepository.save(prod);
            }

            // Registrar en la tabla DELIVERIES
            Delivery delivery = new Delivery();
            delivery.setVenta(venta);
            delivery.setClienteNombre(dto.getNombreCliente());
            delivery.setClienteTelefono(dto.getTelefono());
            delivery.setDireccionEnvio(dto.getDireccion());
            delivery.setReferencia(dto.getReferencia());
            delivery.setEstadoDelivery("En Preparación");
            delivery.setTiempoPromedioMin(28);
            deliveryRepository.save(delivery);

            respuesta.put("success", true);
            respuesta.put("message", "¡Pedido registrado correctamente en base de datos!");
        } catch (Exception e) {
            respuesta.put("success", false);
            respuesta.put("message", "Error transaccional: " + e.getMessage());
        }

        return respuesta;
    }
          
// =========================================================================
// ENDPOINT POST: ASIGNAR REPARTIDOR Y CAMBIAR ESTADOS
// =========================================================================
@PostMapping("/api/delivery/asignar")
@ResponseBody
@Transactional // Garantiza que ambos estados cambien juntos o ninguno
public Map<String, Object> asignarRepartidor(@RequestBody Map<String, Integer> payload) {
    Map<String, Object> respuesta = new HashMap<>();
    
    Integer idDelivery = payload.get("idDelivery");
    Integer idRepartidor = payload.get("idRepartidor");

    if (idDelivery == null || idRepartidor == null) {
        respuesta.put("success", false);
        respuesta.put("message", "Datos de asignación incompletos.");
        return respuesta;
    }

    try {
        // 1. Buscar el pedido delivery
        Delivery delivery = deliveryRepository.findById(idDelivery)
                .orElseThrow(() -> new RuntimeException("Pedido de delivery no encontrado."));

        // 2. Buscar al repartidor
        Repartidor repartidor = repartidorRepository.findById(idRepartidor)
                .orElseThrow(() -> new RuntimeException("Repartidor no encontrado."));

        // Verificar que el repartidor siga libre
        if (!"Libre".equals(repartidor.getEstado())) {
            respuesta.put("success", false);
            respuesta.put("message", "El repartidor seleccionado ya se encuentra en ruta.");
            return respuesta;
        }

        // 3. Actualizar el pedido
        delivery.setRepartidor(repartidor);
        delivery.setEstadoDelivery("En Camino"); // Cambia de 'En Preparación' a 'En Camino'
        deliveryRepository.save(delivery);

        // 4. Actualizar al motorizado
        repartidor.setEstado("En Ruta"); // Cambia de 'Libre' a 'En Ruta'
        repartidorRepository.save(repartidor);

        respuesta.put("success", true);
        respuesta.put("message", "¡Despacho exitoso! Repartidor asignado correctamente.");
    } catch (Exception e) {
        respuesta.put("success", false);
        respuesta.put("message", "Error al procesar el despacho: " + e.getMessage());
    }

    return respuesta;
}
}
