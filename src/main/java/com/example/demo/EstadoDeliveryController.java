package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.transaction.annotation.Transactional;
    
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;   
import java.util.ArrayList;   
    
@Controller
public class EstadoDeliveryController {
    
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private RepartidorRepository repartidorRepository;
    @Autowired private DetalleVentaRepository detalleVentaRepository;
        
@GetMapping("/estadoDelivery") 
public String mostrarInventario(HttpSession session, Model model){
    String username = (String) session.getAttribute("userUsername");
    if (username == null) return "redirect:/?error=no-session";
    
    Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(username);
    usuarioEncontrado.ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
    
    // 1. Traemos la información fresca de la Base de Datos
    List<Delivery> todosLosDeliveries = deliveryRepository.findAll();
    List<Repartidor> todosLosRepartidores = repartidorRepository.findAll();
    
    // 2. Calcular Pedidos en Ruta Activa ('En Camino')
    long enCamino = todosLosDeliveries.stream()
            .filter(d -> "En Camino".equals(d.getEstadoDelivery()))
            .count();
    model.addAttribute("cantEnCamino", enCamino);
    
    // 3. Calcular Total Entregas Hoy ('Entregado')
    long entregadosHoy = todosLosDeliveries.stream()
            .filter(d -> "Entregado".equals(d.getEstadoDelivery()))
            // Opcional: si quieres filtrar estrictamente por fecha, puedes comparar d.getFechaPedido()
            .count();
    model.addAttribute("cantEntregados", entregadosHoy);
    
    // 4. Calcular Pedidos por despachar ('En Preparación')
    long enPreparacion = todosLosDeliveries.stream()
            .filter(d -> "En Preparación".equals(d.getEstadoDelivery()))
            .count();
    model.addAttribute("cantEnPreparacion", enPreparacion);
    
    // 5. Calcular Tiempo Promedio (Tomando el valor base configurado en tu script SQL)
    double tiempoPromedio = todosLosDeliveries.stream()
            .filter(d -> d.getTiempoPromedioMin() != null)
            .mapToInt(Delivery::getTiempoPromedioMin)
            .average()
            .orElse(28); // 28 minutos por defecto si no hay nada
    model.addAttribute("tiempoPromedio", (int) tiempoPromedio);
    
    // 6. Resumen rápido de la Flota (Contar Libres vs En Ruta)
    long motosLibres = todosLosRepartidores.stream().filter(r -> "Libre".equals(r.getEstado())).count();
    long motosEnRuta = todosLosRepartidores.stream().filter(r -> "En Ruta".equals(r.getEstado())).count();
    model.addAttribute("resumenFlota", "Motos Libres: " + motosLibres + " | En Ruta: " + motosEnRuta);
    
    // Para el selector de filtros que ya tenías
    model.addAttribute("repartidores", todosLosRepartidores);
    
    return "estadoDelivery";
}
    
        
    // =========================================================================
    // ENDPOINT API: RETORNA LOS DELIVERIES EN JSON FILTRADOS
    // =========================================================================
@GetMapping("/api/deliveries/filtrar")
@ResponseBody
public List<Map<String, Object>> filtrarDeliveries(
        @RequestParam(required = false, defaultValue = "") String buscar,
        @RequestParam(required = false, defaultValue = "") String estado) {
    
    List<Delivery> todos = deliveryRepository.findAll();

    return todos.stream()
        .filter(d -> {
            if (buscar.isEmpty()) return true;
            String b = buscar.toLowerCase();
            return d.getClienteNombre().toLowerCase().contains(b) || 
                   d.getDireccionEnvio().toLowerCase().contains(b) || 
                   String.valueOf(d.getIdDelivery()).contains(b);
        })
        .filter(d -> {
            if (estado.isEmpty()) return true;
            String estBD = d.getEstadoDelivery().toLowerCase();
            if (estado.equals("preparacion")) return estBD.contains("preparación");
            if (estado.equals("camino")) return estBD.contains("camino");
            if (estado.equals("entregado")) return estBD.contains("entregado");
            return true;
        })
        .map(d -> {
            Map<String, Object> map = new HashMap<>(); // Ahora sí encontrará la clase
            map.put("idDelivery", d.getIdDelivery());
            map.put("clienteNombre", d.getClienteNombre());
            map.put("clienteTelefono", d.getClienteTelefono());
            map.put("direccionEnvio", d.getDireccionEnvio());
            map.put("total", d.getVenta() != null ? d.getVenta().getTotal() : 0.0);
            map.put("estado", d.getEstadoDelivery());
            map.put("repartidor", d.getRepartidor() != null ? 
                    d.getRepartidor().getNombreRepartidor() + " (" + d.getRepartidor().getVehiculo() + ")" : "N/A (Por Asignar)");
            map.put("fecha", d.getFechaPedido() != null ? d.getFechaPedido().toString() : "N/A"); // Ahora sí encontrará el método
            return map;
        })
        .collect(Collectors.toList());
}       
    

@GetMapping("/api/deliveries/detalle")
@ResponseBody
public Map<String, Object> obtenerDetalleDelivery(@RequestParam Integer idDelivery) {
    Map<String, Object> respuesta = new HashMap<>();

    Optional<Delivery> deliveryOpt = deliveryRepository.findById(idDelivery);
    if (deliveryOpt.isEmpty()) {
        respuesta.put("success", false);
        respuesta.put("message", "El pedido no existe.");
        return respuesta;
    }

    Delivery d = deliveryOpt.get();
    Venta v = d.getVenta(); // Relación directa con ventas[cite: 7]

    // 1. Datos básicos del cliente y entrega
    respuesta.put("success", true);
    respuesta.put("idDelivery", d.getIdDelivery());
    respuesta.put("clienteNombre", d.getClienteNombre());
    respuesta.put("clienteTelefono", d.getClienteTelefono());
    respuesta.put("direccionEnvio", d.getDireccionEnvio());
    respuesta.put("referencia", d.getReferencia() != null ? d.getReferencia() : "Ninguna");
    
    if (v != null) {
        respuesta.put("tipoPago", v.getTipoPago());
        respuesta.put("total", v.getTotal());
        
        // 2. Traer los licores y productos de este detalle de venta[cite: 7]
        List<DetalleVenta> detalles = detalleVentaRepository.findAll().stream()
                .filter(det -> det.getVenta().getIdVenta().equals(v.getIdVenta()))
                .toList();

        double sumaProductos = 0.0;
        List<Map<String, Object>> listaProductos = new ArrayList<>();
        
        for (DetalleVenta det : detalles) {
            Map<String, Object> pMap = new HashMap<>();
            pMap.put("cantidad", det.getCantidad());
            pMap.put("nombre", det.getProducto().getNombreProducto());
            pMap.put("precioUnitario", det.getPrecioUnitario());
            listaProductos.add(pMap);
            
            sumaProductos += det.getCantidad() * det.getPrecioUnitario().doubleValue();
        }
        
        respuesta.put("productos", listaProductos);
        respuesta.put("subtotalProductos", sumaProductos);
        // El costo de envío es la diferencia matemática entre el total cobrado y los productos
        respuesta.put("costoEnvio", v.getTotal().doubleValue() - sumaProductos);
    }

    return respuesta;
}
    
// =========================================================================
// ENDPOINT POST: MARCAR COMO ENTREGADO Y LIBERAR MOTORIZADO
// =========================================================================
@PostMapping("/api/deliveries/completar")
@ResponseBody
@Transactional // Garantiza la integridad de ambos estados en la BD
public Map<String, Object> completarDelivery(@RequestBody Map<String, Integer> payload) {
    Map<String, Object> respuesta = new HashMap<>();
    Integer idDelivery = payload.get("idDelivery");

    if (idDelivery == null) {
        respuesta.put("success", false);
        respuesta.put("message", "ID de pedido no recibido.");
        return respuesta;
    }

    try {
        // 1. Buscar el delivery
        Delivery delivery = deliveryRepository.findById(idDelivery)
                .orElseThrow(() -> new RuntimeException("El pedido no existe."));

        // Validar que no haya sido entregado antes
        if ("Entregado".equals(delivery.getEstadoDelivery())) {
            respuesta.put("success", false);
            respuesta.put("message", "Este pedido ya figura como entregado.");
            return respuesta;
        }

        // 2. Actualizar el estado del Delivery a 'Entregado'
        delivery.setEstadoDelivery("Entregado");
        deliveryRepository.save(delivery);

        // 3. Si tiene un repartidor asignado, lo liberamos cambiando su estado a 'Libre'
        if (delivery.getRepartidor() != null) {
            Repartidor rep = delivery.getRepartidor();
            rep.setEstado("Libre");
            repartidorRepository.save(rep);
        }

        respuesta.put("success", true);
        respuesta.put("message", "¡Pedido completado con éxito! El motorizado ya está libre.");
    } catch (Exception e) {
        respuesta.put("success", false);
        respuesta.put("message", "Error al procesar la entrega: " + e.getMessage());
    }

    return respuesta;
}

}
