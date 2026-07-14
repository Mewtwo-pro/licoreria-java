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
    private InventarioRepository inventarioRepository;
    
    @GetMapping("/inventario") 
    public String mostrarPuntoVenta( Model model){
        List<Producto> listaProveedor = inventarioRepository.findAll();
        model.addAttribute("productos", listaProveedor);
        return "/inventario";
    }
}
