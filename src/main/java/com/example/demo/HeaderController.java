
package com.example.demo;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HeaderController {
    @PostMapping("/logout")
    public String procesarAccionLogout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
    @PostMapping("/header/accion")
    public String procesarAccionFormulario(@RequestParam("accion") String accion) {
        // Java lee el 'value' del botón que fue presionado
        if ("usuario".equals(accion)){
            return "redirect:/usuarios"; 
        }else if ("puntoventa".equals(accion)){
            return "redirect:/puntoVenta";
        }else if ("inventario".equals(accion)){
            return "redirect:/inventario.html";
        }else if ("proveedor".equals(accion)){
            return "redirect:/proveedores.html";
        }else if ("historial".equals(accion)){
            return "redirect:/historial.html";
        }else if ("delivery".equals(accion)){
            return "redirect:/newdelivery.html";
        }else if ("estadodelivery".equals(accion)){
            return "redirect:/estadoDelivery.html";
        }
        return "redirect:/index.html";
    }
}
