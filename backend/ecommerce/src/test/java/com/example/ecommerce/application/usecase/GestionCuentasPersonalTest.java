package com.example.ecommerce.application.usecase;

import com.example.ecommerce.application.usecase.CrearCuentaPersonalUseCase.NuevaCuentaPersonal;
import com.example.ecommerce.application.usecase.EditarCuentaPersonalUseCase.DatosCuentaPersonal;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.ContrasenaDebilException;
import com.example.ecommerce.domain.exception.CorreoElectronicoDuplicadoException;
import com.example.ecommerce.domain.exception.CuentaNoEsPersonalException;
import com.example.ecommerce.domain.exception.RolNoPermitidoParaPersonalException;
import com.example.ecommerce.domain.exception.UltimaAdministradoraActivaException;
import com.example.ecommerce.domain.exception.UsuarioNoEncontradoException;
import com.example.ecommerce.domain.repository.SesionRepository;
import com.example.ecommerce.domain.entity.Sesion;
import com.example.ecommerce.domain.valueobject.RolUsuario;
import com.example.ecommerce.support.EntornoAutenticacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-01.2: la Administradora crea, edita y desactiva las cuentas del personal. */
class GestionCuentasPersonalTest {

    private EntornoAutenticacion entorno;
    private CrearCuentaPersonalUseCase crear;
    private EditarCuentaPersonalUseCase editar;
    private CambiarEstadoUsuarioUseCase cambiarEstado;
    private ListarPersonalUseCase listar;

    @BeforeEach
    void preparar() {
        entorno = new EntornoAutenticacion();
        crear = new CrearCuentaPersonalUseCase(entorno.usuarios, entorno.passwordHasher);
        editar = new EditarCuentaPersonalUseCase(entorno.usuarios, entorno.reglaAdministradoraActiva);
        cambiarEstado = new CambiarEstadoUsuarioUseCase(entorno.usuarios, entorno.sesiones,
                entorno.reglaAdministradoraActiva, entorno.transacciones, entorno.reloj);
        listar = new ListarPersonalUseCase(entorno.usuarios);
    }

    // --- Creación ---------------------------------------------------------

    @ParameterizedTest
    @EnumSource(value = RolUsuario.class, names = {"ASESORA_VENTAS", "ENCARGADA_INVENTARIO", "ADMINISTRADORA"})
    void creaCuentasDePersonalConElRolAsignado(RolUsuario rol) {
        // Act
        Usuario cuenta = crear.ejecutar(new NuevaCuentaPersonal(
                "Laura Gómez", "laura@lovelygirl.com", "", "Inicial2026*", rol));

        // Assert
        assertEquals(rol, cuenta.getRol());
        assertTrue(cuenta.isActivo());
        assertTrue(cuenta.getTelefono().isEmpty());
    }

    @Test
    void noPermiteCrearUnaClienteDesdeElPanelDelPersonal() {
        // Act y Assert
        assertThrows(RolNoPermitidoParaPersonalException.class, () -> crear.ejecutar(new NuevaCuentaPersonal(
                "Ana", "ana@correo.com", null, "Inicial2026*", RolUsuario.CLIENTE)));
    }

    @Test
    void laContrasenaInicialDebeCumplirLasMismasReglasDelRegistro() {
        // Act y Assert
        assertThrows(ContrasenaDebilException.class, () -> crear.ejecutar(new NuevaCuentaPersonal(
                "Laura", "laura@lovelygirl.com", null, "12345678", RolUsuario.ASESORA_VENTAS)));
    }

    @Test
    void elCorreoDelPersonalTambienDebeSerUnico() {
        // Arrange
        entorno.registrarCliente("compartido@correo.com");

        // Act y Assert
        assertThrows(CorreoElectronicoDuplicadoException.class, () -> crear.ejecutar(new NuevaCuentaPersonal(
                "Laura", "compartido@correo.com", null, "Inicial2026*", RolUsuario.ASESORA_VENTAS)));
    }

    @Test
    void elListadoSoloMuestraAlPersonal() {
        // Arrange
        entorno.registrarCliente("ana@correo.com");
        entorno.crearPersonal("asesora@lovelygirl.com", RolUsuario.ASESORA_VENTAS);

        // Act
        List<Usuario> personal = listar.ejecutar();

        // Assert
        assertEquals(1, personal.size());
        assertEquals(RolUsuario.ASESORA_VENTAS, personal.getFirst().getRol());
    }

    // --- Edición ----------------------------------------------------------

    @Test
    void editaLosDatosYElRolDeUnaCuentaDelPersonal() {
        // Arrange
        Usuario asesora = entorno.crearPersonal("asesora@lovelygirl.com", RolUsuario.ASESORA_VENTAS);

        // Act
        Usuario editada = editar.ejecutar(asesora.getId(), new DatosCuentaPersonal(
                "Valentina Torres", "valentina@lovelygirl.com", "3109876543", RolUsuario.ENCARGADA_INVENTARIO));

        // Assert
        assertEquals("Valentina Torres", editada.getNombre());
        assertEquals("valentina@lovelygirl.com", editada.getEmail().getValor());
        assertEquals("3109876543", editada.getTelefono().orElseThrow().getValor());
        assertEquals(RolUsuario.ENCARGADA_INVENTARIO, entorno.recargar(asesora).getRol());
    }

    @Test
    void noPermiteEditarUnaCuentaConElCorreoDeOtra() {
        // Arrange
        entorno.crearPersonal("ocupado@lovelygirl.com", RolUsuario.ENCARGADA_INVENTARIO);
        Usuario asesora = entorno.crearPersonal("asesora@lovelygirl.com", RolUsuario.ASESORA_VENTAS);

        // Act y Assert
        assertThrows(CorreoElectronicoDuplicadoException.class, () -> editar.ejecutar(asesora.getId(),
                new DatosCuentaPersonal("Asesora", "ocupado@lovelygirl.com", null, RolUsuario.ASESORA_VENTAS)));
    }

    @Test
    void noPermiteEditarLaCuentaDeUnaClienteDesdeElPanelDelPersonal() {
        // Arrange
        Usuario cliente = entorno.registrarCliente("ana@correo.com");

        // Act y Assert
        assertThrows(CuentaNoEsPersonalException.class, () -> editar.ejecutar(cliente.getId(),
                new DatosCuentaPersonal("Ana", "ana@correo.com", null, RolUsuario.ASESORA_VENTAS)));
    }

    @Test
    void noPermiteQuitarleElRolALaUnicaAdministradoraActiva() {
        // Arrange
        Usuario administradora = entorno.crearPersonal("admin@lovelygirl.com", RolUsuario.ADMINISTRADORA);

        // Act y Assert
        assertThrows(UltimaAdministradoraActivaException.class, () -> editar.ejecutar(administradora.getId(),
                new DatosCuentaPersonal("Admin", "admin@lovelygirl.com", null, RolUsuario.ASESORA_VENTAS)));
        assertEquals(RolUsuario.ADMINISTRADORA, entorno.recargar(administradora).getRol());
    }

    // --- Activación y desactivación -----------------------------------------

    @Test
    void desactivaUnaCuentaDelPersonalSinEliminarlaYCierraSusSesiones() {
        // Arrange
        Usuario asesora = entorno.crearPersonal("asesora@lovelygirl.com", RolUsuario.ASESORA_VENTAS);
        SesionRepository sesiones = entorno.sesiones;
        sesiones.guardar(Sesion.iniciar("sesion-asesora", asesora.getId(), entorno.reloj.instant()));

        // Act
        Usuario desactivada = cambiarEstado.ejecutar(asesora.getId(), false);

        // Assert
        assertFalse(desactivada.isActivo());
        assertTrue(entorno.usuarios.obtenerPorId(asesora.getId()).isPresent());
        assertTrue(entorno.sesiones.abiertasDe(asesora.getId()).isEmpty());
    }

    @Test
    void reactivaUnaCuentaDesactivada() {
        // Arrange
        Usuario asesora = entorno.crearPersonal("asesora@lovelygirl.com", RolUsuario.ASESORA_VENTAS);
        cambiarEstado.ejecutar(asesora.getId(), false);

        // Act
        Usuario reactivada = cambiarEstado.ejecutar(asesora.getId(), true);

        // Assert
        assertTrue(reactivada.isActivo());
    }

    @Test
    void noPermiteDesactivarALaUnicaAdministradoraActiva() {
        // Arrange
        Usuario administradora = entorno.crearPersonal("admin@lovelygirl.com", RolUsuario.ADMINISTRADORA);

        // Act y Assert
        assertThrows(UltimaAdministradoraActivaException.class,
                () -> cambiarEstado.ejecutar(administradora.getId(), false));
        assertTrue(entorno.recargar(administradora).isActivo());
    }

    @Test
    void permiteDesactivarAUnaAdministradoraSiQuedaOtraActiva() {
        // Arrange
        Usuario primera = entorno.crearPersonal("admin1@lovelygirl.com", RolUsuario.ADMINISTRADORA);
        entorno.crearPersonal("admin2@lovelygirl.com", RolUsuario.ADMINISTRADORA);

        // Act
        Usuario desactivada = cambiarEstado.ejecutar(primera.getId(), false);

        // Assert
        assertFalse(desactivada.isActivo());
    }

    @Test
    void noPermiteDesactivarUnaClienteDesdeElPanelDelPersonal() {
        // Arrange
        Usuario cliente = entorno.registrarCliente("ana@correo.com");

        // Act y Assert
        assertThrows(CuentaNoEsPersonalException.class, () -> cambiarEstado.ejecutar(cliente.getId(), false));
    }

    @Test
    void fallaSiLaCuentaNoExiste() {
        // Act y Assert
        assertThrows(UsuarioNoEncontradoException.class, () -> cambiarEstado.ejecutar(999L, false));
    }
}
