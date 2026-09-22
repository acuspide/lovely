package com.example.ecommerce.infrastructure.web;

import com.example.ecommerce.application.usecase.AutenticarUsuarioUseCase;
import com.example.ecommerce.application.usecase.RegistrarUsuarioUseCase;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ReglaDominioException;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Front web de F-01 "Autenticación y gestión de rol" (RF-01).
 * Cubre: registro de usuario con rol (Cliente/Vendedor/Administrador),
 * inicio de sesión, sesión activa por HttpSession y cierre de sesión.
 */
@Controller
public class AuthController {

    private static final String SESSION_USUARIO_ID = "usuarioId";
    private static final String SESSION_USUARIO_NOMBRE = "usuarioNombre";
    private static final String SESSION_USUARIO_EMAIL = "usuarioEmail";
    private static final String SESSION_USUARIO_ROL = "usuarioRol";

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final AtomicLong usuarioIdGenerator;

    public AuthController(RegistrarUsuarioUseCase registrarUsuarioUseCase,
                           AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                           AtomicLong usuarioIdGenerator) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
        this.usuarioIdGenerator = usuarioIdGenerator;
    }

    @GetMapping({"/", "/login"})
    public String mostrarLogin(HttpSession session,
                                @RequestParam(value = "perfil", required = false, defaultValue = "cliente") String perfil,
                                @RequestParam(value = "tab", required = false, defaultValue = "login") String tab,
                                Model model) {
        if (session.getAttribute(SESSION_USUARIO_ID) != null) {
            return "redirect:/dashboard";
        }
        if (!model.containsAttribute("perfilActivo")) {
            model.addAttribute("perfilActivo", perfil);
        }
        if (!model.containsAttribute("tabActiva")) {
            model.addAttribute("tabActiva", tab);
        }
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String email,
                                 @RequestParam String contrasena,
                                 @RequestParam(required = false, defaultValue = "cliente") String perfil,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = autenticarUsuarioUseCase.ejecutar(email, contrasena);
            iniciarSesion(session, usuario);
            return "redirect:/dashboard";
        } catch (ReglaDominioException ex) {
            redirectAttributes.addFlashAttribute("loginError", ex.getMessage());
            redirectAttributes.addFlashAttribute("perfilActivo", perfil);
            redirectAttributes.addFlashAttribute("tabActiva", "login");
            redirectAttributes.addFlashAttribute("emailIntentado", email);
            return "redirect:/login";
        }
    }

    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam String nombre,
                                    @RequestParam String email,
                                    @RequestParam String contrasena,
                                    @RequestParam String confirmarContrasena,
                                    @RequestParam(required = false, defaultValue = "cliente") String perfil,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        try {
            if (contrasena == null || !contrasena.equals(confirmarContrasena)) {
                throw new ReglaDominioException("Las contraseñas no coinciden.");
            }

            RolUsuario rol = mapearPerfilARol(perfil);
            long id = usuarioIdGenerator.incrementAndGet();

            Usuario usuario = registrarUsuarioUseCase.ejecutar(id, nombre, email, contrasena, rol);
            iniciarSesion(session, usuario);
            return "redirect:/dashboard";
        } catch (ReglaDominioException ex) {
            redirectAttributes.addFlashAttribute("registroError", ex.getMessage());
            redirectAttributes.addFlashAttribute("perfilActivo", perfil);
            redirectAttributes.addFlashAttribute("tabActiva", "registro");
            redirectAttributes.addFlashAttribute("nombreIntentado", nombre);
            redirectAttributes.addFlashAttribute("emailIntentadoRegistro", email);
            return "redirect:/login";
        }
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(HttpSession session, Model model) {
        Object usuarioId = session.getAttribute(SESSION_USUARIO_ID);
        if (usuarioId == null) {
            return "redirect:/login";
        }
        model.addAttribute("nombre", session.getAttribute(SESSION_USUARIO_NOMBRE));
        model.addAttribute("email", session.getAttribute(SESSION_USUARIO_EMAIL));
        model.addAttribute("rol", session.getAttribute(SESSION_USUARIO_ROL));
        return "dashboard";
    }

    @PostMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private void iniciarSesion(HttpSession session, Usuario usuario) {
        session.setAttribute(SESSION_USUARIO_ID, usuario.getId());
        session.setAttribute(SESSION_USUARIO_NOMBRE, usuario.getNombre());
        session.setAttribute(SESSION_USUARIO_EMAIL, usuario.getEmail().getValor());
        session.setAttribute(SESSION_USUARIO_ROL, usuario.getRol());
    }

    private RolUsuario mapearPerfilARol(String perfil) {
        if (perfil == null) {
            return RolUsuario.CLIENTE;
        }
        return switch (perfil) {
            case "vendedor" -> RolUsuario.VENDEDOR;
            case "admin" -> RolUsuario.ADMINISTRADOR;
            default -> RolUsuario.CLIENTE;
        };
    }
}
