package com.example.ecommerce.support;

import com.example.ecommerce.application.TokenProvider;
import com.example.ecommerce.domain.entity.Sesion;
import com.example.ecommerce.domain.entity.Usuario;
import com.example.ecommerce.domain.exception.TokenInvalidoException;

/**
 * Doble del puerto TokenProvider que aísla las pruebas de JJWT. El token es
 * simplemente "idSesion|idUsuario"; JwtTokenProvider se prueba aparte.
 */
public class TokenProviderFalso implements TokenProvider {

    private static final String SEPARADOR = "|";

    @Override
    public String generarToken(Usuario usuario, Sesion sesion) {
        return sesion.getId() + SEPARADOR + usuario.getId();
    }

    @Override
    public DatosToken leer(String token) {
        String[] partes = token.split("\\" + SEPARADOR);
        if (partes.length != 2) {
            throw new TokenInvalidoException();
        }
        return new DatosToken(partes[0], Long.parseLong(partes[1]));
    }
}
