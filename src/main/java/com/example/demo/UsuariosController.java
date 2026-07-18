package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import jakarta.servlet.http.HttpSession; // Corregido
import java.util.Optional;               // Corregido

@Controller
public class UsuariosController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/usuarios")
    public String mostrarUsuarios(HttpSession session, Model model) {
        
        String username = (String) session.getAttribute("userUsername");
        if (username == null) return "redirect:/?error=no-session";
        
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(username);
        usuarioEncontrado.ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
        
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "usuarios"; // Tip: es mejor sin la barra "/" inicial
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
}
