package com.techservice.config;

import com.techservice.entity.Cliente;
import com.techservice.entity.Tecnico;
import com.techservice.entity.Usuario;
import com.techservice.enums.EstadoDisponibilidad;
import com.techservice.enums.EstadoUsuario;
import com.techservice.enums.RolUsuario;
import com.techservice.repository.ClienteRepository;
import com.techservice.repository.TecnicoRepository;
import com.techservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final TecnicoRepository tecnicoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        Usuario admin = ensureUser("admin", "admin@techservice.local", "Admin123!", "Administrador", "Sistema", RolUsuario.ADMIN);
        Usuario tecnicoUser = ensureUser("tecnico", "tecnico@techservice.local", "Tecnico123!", "Carlos", "Técnico", RolUsuario.TECNICO);
        Usuario clienteUser = ensureUser("cliente", "cliente@techservice.local", "Cliente123!", "Ana", "Cliente", RolUsuario.CLIENTE);

        // Migrar password del usuario admin Fase 1 (SHA-256) si existe con otro hash
        rehashIfNeeded(admin, "Admin123!");
        rehashIfNeeded(tecnicoUser, "Tecnico123!");
        rehashIfNeeded(clienteUser, "Cliente123!");

        if (tecnicoRepository.findByUsuarioId(tecnicoUser.getId()).isEmpty()) {
            tecnicoRepository.save(Tecnico.builder()
                    .usuario(tecnicoUser)
                    .especialidad("Hardware y redes")
                    .disponibilidad(EstadoDisponibilidad.DISPONIBLE)
                    .telefono("3001112233")
                    .build());
        }

        if (clienteRepository.findByUsuarioId(clienteUser.getId()).isEmpty()) {
            clienteRepository.save(Cliente.builder()
                    .usuario(clienteUser)
                    .razonSocial("Cliente Demo SAS")
                    .documento("900123456")
                    .telefono("3109876543")
                    .direccion("Calle 1 # 2-3")
                    .ciudad("Bogotá")
                    .build());
        }

        log.info("Seed demo listo: admin / tecnico / cliente (ver README)");
    }

    private Usuario ensureUser(String username, String email, String rawPassword,
                               String nombre, String apellido, RolUsuario rol) {
        return usuarioRepository.findByUsername(username).orElseGet(() ->
                usuarioRepository.save(Usuario.builder()
                        .username(username)
                        .email(email)
                        .passwordHash(passwordEncoder.encode(rawPassword))
                        .nombre(nombre)
                        .apellido(apellido)
                        .rol(rol)
                        .estado(EstadoUsuario.ACTIVO)
                        .build()));
    }

    private void rehashIfNeeded(Usuario usuario, String rawPassword) {
        String hash = usuario.getPasswordHash();
        if (hash == null || !passwordEncoder.matches(rawPassword, hash)) {
            // Con seed activo, alinear siempre la clave demo (incluye BCrypt desfasado)
            usuario.setPasswordHash(passwordEncoder.encode(rawPassword));
            usuario.setEstado(EstadoUsuario.ACTIVO);
            usuarioRepository.save(usuario);
            log.info("Password demo alineado (BCrypt) para usuario {}", usuario.getUsername());
        }
    }
}
