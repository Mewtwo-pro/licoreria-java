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
public class InventarioController {

    @Autowired 
    private CategoriaRepository categoriaRepository;
    
    @Autowired 
    private ProductoRepository productoRepository;
    
    @Autowired 
    private UsuarioRepository usuarioRepository;
    
    @GetMapping("/inventario") 
    public String mostrarInventario(HttpSession session ,  Model model){
        String username = (String) session.getAttribute("userUsername");
        if (username == null) return "redirect:/?error=no-session";
        
        Optional<Usuario> usuarioEncontrado = usuarioRepository.findByUsername(username);
        usuarioEncontrado.ifPresent(usuario -> model.addAttribute("usuarioLogueado" , usuario));
        
        List<Producto> listaProductos = productoRepository.findAll();
        model.addAttribute("productos", listaProductos);
        
        List<Categoria> listaCategoria = categoriaRepository.findAll();
        model.addAttribute("categorias", listaCategoria);
        
        return "/inventario";
    }
}
