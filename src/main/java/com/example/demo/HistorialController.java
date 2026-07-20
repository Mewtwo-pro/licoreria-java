package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
    
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
@Controller 
public class HistorialController {

    @Autowired 
    private UsuarioRepository usuarioRepository;

    @Autowired 
    private VentaRepository ventaRepository;
    
    @Autowired 
    private ProductoRepository productoRepository;

@GetMapping("/historial") 
public String mostrarInventario(
        @RequestParam(value = "idVentaFiltro", required = false) Integer idVentaFiltro,
        @RequestParam(value = "idUsuario", required = false) Integer idUsuario,
        @RequestParam(value = "fechaInicio", required = false) String fechaInicioStr,
        @RequestParam(value = "fechaFin", required = false) String fechaFinStr,
        @RequestParam(value = "horaInicio", required = false) String horaInicioStr,
        @RequestParam(value = "horaFin", required = false) String horaFinStr,
        @RequestParam(value = "idSeleccionado", required = false) Integer idSeleccionado,
        HttpSession session, 
        Model model) {
        
    String username = (String) session.getAttribute("userUsername");
    if (username == null) return "redirect:/?error=no-session";
    
    Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(username);
    usuarioEncontrado.ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
    
    model.addAttribute("usuarios", usuarioRepository.findAll());

// 1. OBTENER TODAS LAS VENTAS
    List<Venta> listaVentas = ventaRepository.findAll();

    // Variable para construir el texto dinámico del filtro activo
    StringBuilder descripcionFiltro = new StringBuilder();

    // FILTRO 1: Por ID de Venta
    if (idVentaFiltro != null) {
        listaVentas = listaVentas.stream()
                .filter(v -> v.getIdVenta().equals(idVentaFiltro))
                .collect(Collectors.toList());
        descripcionFiltro.append("ID Venta: ").append(idVentaFiltro).append(" | ");
    }

    // FILTRO 2: Por Usuario
    if (idUsuario != null) {
        listaVentas = listaVentas.stream()
                .filter(v -> v.getUsuarioCajero().getIdUsuario().equals(idUsuario))
                .collect(Collectors.toList());
        
        // Buscamos el nombre del usuario para mostrarlo en la etiqueta
        usuarioRepository.findById(idUsuario).ifPresent(u -> {
            descripcionFiltro.append("Usuario: ").append(u.getNombreCompleto()).append(" | ");
        });
    }

    // FILTRO 3: Rango de Días
    if (fechaInicioStr != null && !fechaInicioStr.isEmpty() && fechaFinStr != null && !fechaFinStr.isEmpty()) {
        LocalDate inicio = LocalDate.parse(fechaInicioStr);
        LocalDate fin = LocalDate.parse(fechaFinStr);
        
        listaVentas = listaVentas.stream()
                .filter(v -> {
                    LocalDate fechaVenta = v.getFechaVenta().toLocalDate();
                    return (!fechaVenta.isBefore(inicio) && !fechaVenta.isAfter(fin));
                })
                .collect(Collectors.toList());
        
        descripcionFiltro.append("Días: ").append(fechaInicioStr).append(" a ").append(fechaFinStr).append(" | ");
    }

    // FILTRO 4: Rango de Horas
    if (horaInicioStr != null && !horaInicioStr.isEmpty() && horaFinStr != null && !horaFinStr.isEmpty()) {
        LocalTime hInicio = LocalTime.parse(horaInicioStr);
        LocalTime hFin = LocalTime.parse(horaFinStr);
        
        listaVentas = listaVentas.stream()
                .filter(v -> {
                    LocalTime horaVenta = v.getFechaVenta().toLocalTime();
                    return (!horaVenta.isBefore(hInicio) && !horaVenta.isAfter(hFin));
                })
                .collect(Collectors.toList());
        
        descripcionFiltro.append("Horas: ").append(horaInicioStr).append(" a ").append(horaFinStr).append(" | ");
    }

    // MANDAR EL TEXTO DEL FILTRO AL MODELO
    String filtroFinal = descripcionFiltro.toString();
    if (!filtroFinal.isEmpty()) {
        // Quitamos el último separador " | " para que quede limpio
        filtroFinal = filtroFinal.substring(0, filtroFinal.length() - 3);
        model.addAttribute("filtroActivo", filtroFinal);
    } else {
        model.addAttribute("filtroActivo", "Ninguno");
    }

    // Enviar lista filtrada al HTML
    model.addAttribute("ventas", listaVentas);
    // 2. CALCULAR MÉTRICAS EN BASE A LOS FILTROS SELECCIONADOS
    long totalVentasCount = listaVentas.size();
    BigDecimal totalIngresosSum = listaVentas.stream()
            .map(Venta::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    model.addAttribute("totalVentasCount", totalVentasCount);
    model.addAttribute("totalIngresosSum", totalIngresosSum);

    // 3. ENVIAR SELECCIÓN PARA EL TICKET DETALLE
    if (idSeleccionado != null) {
        Optional<Venta> seleccionada = ventaRepository.findById(idSeleccionado);
        seleccionada.ifPresent(venta -> model.addAttribute("ventaSeleccionada", venta));
    } else if (!listaVentas.isEmpty()) {
        model.addAttribute("ventaSeleccionada", listaVentas.get(0));
    }
    
    return "historial";
}
        
    @GetMapping("/historial/api/detalle")
    @org.springframework.web.bind.annotation.ResponseBody // Permite devolver JSON directamente
    public java.util.Map<String, Object> obtenerDetalleJson(@RequestParam("id") Integer id) {
        java.util.Map<String, Object> respuesta = new java.util.HashMap<>();
        
        Optional<Venta> optVenta = ventaRepository.findById(id);
        if (optVenta.isPresent()) {
            Venta v = optVenta.get();
            respuesta.put("idVenta", v.getIdVenta());
            respuesta.put("cajero", v.getUsuarioCajero().getNombreCompleto());
            respuesta.put("total", v.getTotal());
            respuesta.put("tipoPago", v.getTipoPago());
            
            // Mapeamos los detalles usando .put() correctamente
            java.util.List<java.util.Map<String, Object>> detallesJson = v.getDetalles().stream().map(d -> {
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                
                // CORREGIDO: Ahora sí usamos .put para los Map de Java
                item.put("producto", d.getProducto().getNombreProducto());
                item.put("cantidad", d.getCantidad());
                item.put("precioUnitario", d.getPrecioUnitario());
                item.put("subtotal", d.getPrecioUnitario().multiply(java.math.BigDecimal.valueOf(d.getCantidad())));
                
                return item;
            }).collect(java.util.stream.Collectors.toList());
            
            respuesta.put("detalles", detallesJson);
        }
        return respuesta;
    }
        
    @GetMapping("/historial/comprobante/imprimir")
    public String imprimirComprobante(@RequestParam("id") Integer id, Model model) {
        Optional<Venta> optVenta = ventaRepository.findById(id);
        
        if (optVenta.isPresent()) {
            model.addAttribute("venta", optVenta.get());
            return "ticket-imprimir"; // Creas una vista HTML pequeña solo con el formato del ticket
        }
        
        return "redirect:/historial?error=venta-no-encontrada";
    }
        

    @PostMapping("/historial/api/devolucion")
    @ResponseBody
    @Transactional // Garantiza que si falla la devolución de un producto, no se altere nada en la BD
    public ResponseEntity<String> procesarDevolucion(@RequestParam("id") Integer idVenta) {
        
        // 1. Buscamos la venta
        Optional<Venta> optVenta = ventaRepository.findById(idVenta);
        if (!optVenta.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Venta venta = optVenta.get();

        // 2. REGRESAR EL STOCK: Recorremos los detalles de los licores vendidos
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            // Sumamos la cantidad devuelta al stock actual
            producto.setStockActual(producto.getStockActual() + detalle.getCantidad());
            // Guardamos el stock actualizado del producto en la base de datos
            productoRepository.save(producto);
        }

        // 3. ELIMINAR O ANULAR LA VENTA
        // Al eliminar la venta, la base de datos borrará en cascada sus registros de 'detalle_ventas'[cite: 1]
        ventaRepository.delete(venta);

        return ResponseEntity.ok("Devolución completada");
    }
}
