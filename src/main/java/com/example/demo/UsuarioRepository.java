package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // NUEVO MÉTODO: Busca un usuario en la BD a través de su 'username'
    Optional<Usuario> findByUsername(String username);
}
