package com.example.ecommerce.domain.entity;

import com.example.ecommerce.domain.exception.ContrasenaRequeridaException;
import com.example.ecommerce.domain.exception.CuentaNoEsPersonalException;
import com.example.ecommerce.domain.exception.HabeasDataNoAceptadoException;
import com.example.ecommerce.domain.exception.NombreUsuarioRequeridoException;
import com.example.ecommerce.domain.exception.RolNoPermitidoParaPersonalException;
import com.example.ecommerce.domain.exception.RolRequeridoException;
import com.example.ecommerce.domain.exception.TelefonoInvalidoException;
import com.example.ecommerce.domain.valueobject.Email;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.domain.valueobject.Telefono;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Cuenta de acceso al sistema (F-01). Concentra las reglas que dependen solo
 * del estado de la propia cuenta: cómo nace según quién la crea (RF-01.1 y
 * RF-01.2), su activación y el bloqueo por intentos fallidos (RF-01.4).
 *
 * No se construye con {@code new}: los métodos de fábrica dejan explícito el
 * origen de cada cuenta y garantizan que ninguna nazca en un estado inválido.
 */
public class Usuario {

    /** RF-01.4: intentos fallidos seguidos que provocan el bloqueo. */
    public static final int MAX_INTENTOS_FALLIDOS = 5;

    /** RF-01.4: duración del bloqueo temporal. */
    public static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    /** Id de una cuenta que todavía no se ha persistido; el repositorio asigna el real. */
    public static final long SIN_PERSISTIR = 0L;

    private final long id;
    private String nombre;
    private Email email;
    private Telefono telefono;
    private String contrasenaHash;
    private RolUsuario rol;
    private boolean activo;
    private int intentosFallidos;
    private Instant bloqueadoHasta;
    private final Instant habeasDataAceptadoEn;

    private Usuario(long id, String nombre, Email email, Telefono telefono, String contrasenaHash,
                    RolUsuario rol, boolean activo, int intentosFallidos, Instant bloqueadoHasta,
                    Instant habeasDataAceptadoEn) {
        validarNombre(nombre);
        validarEmail(email);
        validarContrasenaHash(contrasenaHash);
        validarRol(rol);

        this.id = id;
        this.nombre = nombre.trim();
        this.email = email;
        this.telefono = telefono;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
        this.activo = activo;
        this.intentosFallidos = intentosFallidos;
        this.bloqueadoHasta = bloqueadoHasta;
        this.habeasDataAceptadoEn = habeasDataAceptadoEn;
    }

    /**
     * RF-01.1: una clienta crea su propia cuenta. El rol Cliente se asigna
     * siempre, el teléfono es obligatorio y debe quedar constancia de cuándo
     * aceptó la política de tratamiento de datos (Ley 1581 de 2012).
     */
    public static Usuario registrarCliente(String nombre, Email email, Telefono telefono,
                                           String contrasenaHash, Instant habeasDataAceptadoEn) {
        if (telefono == null) {
            throw new TelefonoInvalidoException();
        }
        if (habeasDataAceptadoEn == null) {
            throw new HabeasDataNoAceptadoException();
        }
        return new Usuario(SIN_PERSISTIR, nombre, email, telefono, contrasenaHash,
                RolUsuario.CLIENTE, true, 0, null, habeasDataAceptadoEn);
    }

    /**
     * RF-01.2: la Administradora crea la cuenta de una persona del equipo. El
     * teléfono es opcional y el rol debe ser uno de personal, nunca Cliente.
     */
    public static Usuario crearPersonal(String nombre, Email email, Telefono telefono,
                                        String contrasenaHash, RolUsuario rol) {
        validarRolDePersonal(rol);
        return new Usuario(SIN_PERSISTIR, nombre, email, telefono, contrasenaHash,
                rol, true, 0, null, null);
    }

    /**
     * Reconstruye una cuenta ya existente a partir de lo guardado en la
     * persistencia. No aplica reglas de creación (por ejemplo, el teléfono de
     * cuentas antiguas puede no existir), solo las invariantes básicas.
     */
    public static Usuario reconstruir(long id, String nombre, Email email, Telefono telefono,
                                      String contrasenaHash, RolUsuario rol, boolean activo,
                                      int intentosFallidos, Instant bloqueadoHasta,
                                      Instant habeasDataAceptadoEn) {
        return new Usuario(id, nombre, email, telefono, contrasenaHash, rol, activo,
                intentosFallidos, bloqueadoHasta, habeasDataAceptadoEn);
    }

    /**
     * Devuelve una copia de esta cuenta con el id asignado por la persistencia.
     * Los repositorios la usan justo después de insertar una cuenta nueva.
     */
    public Usuario conId(long nuevoId) {
        return new Usuario(nuevoId, nombre, email, telefono, contrasenaHash, rol, activo,
                intentosFallidos, bloqueadoHasta, habeasDataAceptadoEn);
    }

    // ---------------------------------------------------------------------
    // Gestión de la cuenta por la Administradora (RF-01.2)
    // ---------------------------------------------------------------------

    public void actualizarDatosDePersonal(String nuevoNombre, Email nuevoEmail,
                                          Telefono nuevoTelefono, RolUsuario nuevoRol) {
        verificarQueEsPersonal();
        validarNombre(nuevoNombre);
        validarEmail(nuevoEmail);
        validarRolDePersonal(nuevoRol);

        this.nombre = nuevoNombre.trim();
        this.email = nuevoEmail;
        this.telefono = nuevoTelefono;
        this.rol = nuevoRol;
    }

    public void verificarQueEsPersonal() {
        if (!rol.esPersonal()) {
            throw new CuentaNoEsPersonalException();
        }
    }

    public void desactivar() {
        this.activo = false;
    }

    public void activar() {
        this.activo = true;
    }

    public boolean esAdministradoraActiva() {
        return activo && rol == RolUsuario.ADMINISTRADORA;
    }

    // ---------------------------------------------------------------------
    // Bloqueo por intentos fallidos (RF-01.4)
    // ---------------------------------------------------------------------

    public boolean estaBloqueada(Instant ahora) {
        return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }

    /** Minutos que faltan para el desbloqueo, redondeados hacia arriba. */
    public long minutosRestantesDeBloqueo(Instant ahora) {
        if (!estaBloqueada(ahora)) {
            return 0;
        }
        long segundos = Duration.between(ahora, bloqueadoHasta).toSeconds();
        return (segundos + 59) / 60;
    }

    /**
     * Cuenta un intento fallido y, al llegar al máximo, bloquea la cuenta. Si
     * un bloqueo anterior ya venció, el conteo empieza de nuevo: tras esperar
     * los 15 minutos la usuaria vuelve a tener sus cinco intentos.
     */
    public void registrarIntentoFallido(Instant ahora) {
        if (bloqueadoHasta != null && !estaBloqueada(ahora)) {
            reiniciarIntentos();
        }

        intentosFallidos++;

        if (intentosFallidos >= MAX_INTENTOS_FALLIDOS) {
            bloqueadoHasta = ahora.plus(DURACION_BLOQUEO);
        }
    }

    /** RF-01.4: un ingreso exitoso devuelve el contador a cero. */
    public void registrarIngresoExitoso() {
        reiniciarIntentos();
    }

    // ---------------------------------------------------------------------
    // Contraseña (RF-01.5 / RF-01.8)
    // ---------------------------------------------------------------------

    /**
     * Reemplaza el hash de la contraseña. Quien ya demostró conocer la nueva
     * contraseña (o tener el enlace de recuperación) no debe seguir bloqueada.
     */
    public void cambiarContrasena(String nuevoHash) {
        validarContrasenaHash(nuevoHash);
        this.contrasenaHash = nuevoHash;
        reiniciarIntentos();
    }

    private void reiniciarIntentos() {
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
    }

    // ---------------------------------------------------------------------
    // Validaciones
    // ---------------------------------------------------------------------

    private static void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new NombreUsuarioRequeridoException();
        }
    }

    private static void validarEmail(Email email) {
        Objects.requireNonNull(email, "El correo del usuario es obligatorio.");
    }

    private static void validarContrasenaHash(String contrasenaHash) {
        if (contrasenaHash == null || contrasenaHash.trim().isEmpty()) {
            throw new ContrasenaRequeridaException();
        }
    }

    private static void validarRol(RolUsuario rol) {
        if (rol == null) {
            throw new RolRequeridoException();
        }
    }

    private static void validarRolDePersonal(RolUsuario rol) {
        validarRol(rol);
        if (!rol.esPersonal()) {
            throw new RolNoPermitidoParaPersonalException();
        }
    }

    // ---------------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------------

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public Email getEmail() { return email; }
    public Optional<Telefono> getTelefono() { return Optional.ofNullable(telefono); }
    public String getContrasenaHash() { return contrasenaHash; }
    public RolUsuario getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public Optional<Instant> getBloqueadoHasta() { return Optional.ofNullable(bloqueadoHasta); }
    public Optional<Instant> getHabeasDataAceptadoEn() { return Optional.ofNullable(habeasDataAceptadoEn); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario otro)) return false;
        return id == otro.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** Nunca incluye el hash de la contraseña (RF-01.5): es seguro de registrar en logs. */
    @Override
    public String toString() {
        return "Usuario{id=" + id + ", email=" + email.getValor() + ", rol=" + rol + ", activo=" + activo + "}";
    }
}
