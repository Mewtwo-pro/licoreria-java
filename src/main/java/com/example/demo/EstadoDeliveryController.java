package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;   
    
@Controller
public class EstadoDeliveryController {
    
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private RepartidorRepository repartidorRepository;
        
    @GetMapping("/estadoDelivery") 
    public String mostrarInventario(HttpSession session, Model model){
        String username = (String) session.getAttribute("userUsername");
        if (username == null) return "redirect:/?error=no-session";
        
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(username);
        usuarioEncontrado.ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
        
        // Cargamos los repartidores en el select de filtros al cargar la pantalla
        model.addAttribute("repartidores", repartidorRepository.findAll());
        
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

}
