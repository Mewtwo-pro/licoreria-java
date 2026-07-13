package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@SpringBootApplication
@Controller
public class DemoApplication {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam("username") String usuario, 
            @RequestParam("password") String contrasena,
            HttpSession session
    ) {
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(usuario);

        if (usuarioEncontrado.isPresent()) {
            Usuario user = usuarioEncontrado.get();

            if (user.getPassword().equals(contrasena) && Boolean.TRUE.equals(user.getActivo())) {
                String rolTexto = "empleado"; 
                if (user.getIdRol() == 1) { 
                    rolTexto = "admin";
                }
                
                session.setAttribute("userRol", rolTexto);
                session.setAttribute("userUsername", user.getUsername()); 

                if (user.getIdRol() == 1) {
                    return "redirect:/usuarios"; 
                } else {
                    return "redirect:/puntoVenta"; 
                }
            }
        }
        return "redirect:/?error=true"; 
    }

    @GetMapping("/api/current-header")
    public String obtenerHeaderPorRol(HttpSession session, Model model) {
        String username = (String) session.getAttribute("userUsername"); 
        if (username != null) {
            Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(username);
            usuarioEncontrado.ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
        }
        
        String rol = (String) session.getAttribute("userRol");
        if ("admin".equals(rol)) {
            return "header-admin"; 
        } else {
            return "header-user"; 
        }
    }

    @GetMapping("/usuarios")
    public String mostrarGestionUsuarios(HttpSession session, Model model) {
        String username = (String) session.getAttribute("userUsername");
        String rol = (String) session.getAttribute("userRol");
        
        if (username == null || !"admin".equals(rol)) {
            return "redirect:/?error=no-access";
        }

        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(username);
        usuarioEncontrado.ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));

        java.util.List<Usuario> listaUsuarios = usuarioRepository.findAll(); 
        model.addAttribute("usuarios", listaUsuarios); 

        return "usuarios"; 
    }
}
