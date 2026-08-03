package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import jakarta.servlet.http.HttpSession; // Corregido
import java.util.Optional;               // Corregido

@Controller
public class UsuariosController {
    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
@GetMapping("/usuarios")
public String mostrarUsuarios(HttpSession session, Model model) {
    
    String username = (String) session.getAttribute("userUsername");
    if (username == null) return "redirect:/?error=no-session";
    
    Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(username);
    usuarioEncontrado.ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
    
    // 1. Cargar lista de empleados una sola vez
    List<Usuario> listaUsuarios = usuarioRepository.findAll();
    model.addAttribute("usuarios", listaUsuarios);

    // 2. Calcular ingresos totales del mes actual
    double totalMes = ventaRepository.findAll().stream()
        .mapToDouble(v -> v.getTotal().doubleValue())
        .sum();

    // 3. Calcular ingresos específicos de HOY en Efectivo (Corregido a .equals)
    java.time.LocalDate hoy = java.time.LocalDate.now();
    double efectivoHoy = ventaRepository.findAll().stream()
        .filter(v -> v.getFechaVenta().toLocalDate().equals(hoy))
        .filter(v -> "Efectivo".equals(v.getTipoPago())) // <-- Corrección técnica aquí: comparación directa de String
        .mapToDouble(v -> v.getTotal().doubleValue())
        .sum();

    // 4. Pasar los datos financieros dinámicos a la vista
    model.addAttribute("ingresosMes", totalMes);
    model.addAttribute("efectivoDiario", efectivoHoy);       
    
    return "usuarios";
}       

    @PostMapping("/usuarios/accion")
    public String procesarAccionFormulario(@RequestParam("accion") String accion) {
        if ("go-puntoVenta".equals(accion)){
            return "redirect:/puntoVenta.html"; 
        } else if ("tarjeta".equals(accion)){
            return "redirect:/inventario.html";
        }
        return "redirect:/index.html";
    }

    @PostMapping("/usuarios/registrar")
    public String registrarNuevoEmpleado(
            @RequestParam("nombre") String nombre,
            @RequestParam("contraseña") String password,
            @RequestParam("username") String username,
            @RequestParam("rol") Integer idRol,
            @RequestParam("turno") String turno) {
        
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreCompleto(nombre);
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPassword(password);
        nuevoUsuario.setIdRol(idRol);
        nuevoUsuario.setTurno(turno);
        nuevoUsuario.setActivo(true);

        usuarioRepository.save(nuevoUsuario);

        return "redirect:/usuarios"; 
    }
        
    @PostMapping("/usuarios/actualizar")
    public String actualizarEmpleado(
            @RequestParam("idUsuario") Integer idUsuario,
            @RequestParam("nombre") String nombre,
            @RequestParam("username") String username,
            @RequestParam("rol") Integer idRol,
            @RequestParam("turno") String turno,
            @RequestParam("activo") Boolean activo) {
        
        // Buscamos el usuario actual en la base de datos
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuarioExistente = usuarioOpt.get();
            
            // Seteamos los nuevos datos recolectados por el modal
            usuarioExistente.setNombreCompleto(nombre);
            usuarioExistente.setUsername(username);
            usuarioExistente.setIdRol(idRol);
            usuarioExistente.setTurno(turno);
            usuarioExistente.setActivo(activo);
            
            // Guardamos la entidad modificada conservando su contraseña previa intacta
            usuarioRepository.save(usuarioExistente);
        }

        // Redireccionamos a la pantalla de gestión de usuarios para actualizar la tabla
        return "redirect:/usuarios"; 
    }
        
@PostMapping("/usuarios/eliminar")
public String eliminarEmpleado(@RequestParam("idUsuario") Integer idUsuario) {
    
    // 1. Buscamos al usuario en la base de datos por su ID
    Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
    
    if (usuarioOpt.isPresent()) {
        Usuario u = usuarioOpt.get();
        
        // REEMPLAZO AQUÍ: En lugar de usar usuarioRepository.delete(u);
        // Le aplicamos el borrado lógico cambiando su estado a false (Inactivo)
        u.setActivo(false); 
        
        // Guardamos los cambios en MySQL
        usuarioRepository.save(u);
    }

    // Redireccionamos a la vista para refrescar la tabla al instante
    return "redirect:/usuarios";
}
// ... Tus otros métodos previos en UsuariosController.java[cite: 14]

    @GetMapping("/usuarios/api/lista")
    @ResponseBody // Convierte la respuesta directamente a formato JSON estructurado
    public List<Usuario> obtenerListaUsuariosParaPdf() {
        // Retornamos todos los empleados de la tabla 'usuarios' de MySQL
        return usuarioRepository.findAll();
    }
        
}
