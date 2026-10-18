package com.example.ecommerce.application.usecase;

import com.example.ecommerce.domain.entity.Pedido;
import com.example.ecommerce.domain.repository.PedidoRepository;
import com.example.ecommerce.domain.repository.UsuarioRepository;
import com.example.ecommerce.domain.valueobject.EstadoPedido;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RF-07.1 (IU-04): lista de pedidos para el personal (Administradora y
 * Asesora de ventas), del más reciente al más antiguo, con número, clienta,
 * fecha, total y estado. Se puede filtrar por estado y buscar por número de
 * pedido o por nombre de la clienta.
 *
 * <p>La búsqueda acepta el número completo ("LG-000012"), solo los dígitos
 * ("12" o "000012") o parte del nombre, sin importar mayúsculas ni tildes.</p>
 */
public class ListarPedidosUseCase {

    /** "LG-000012", "lg12", "000012" o "12". */
    private static final Pattern NUMERO = Pattern.compile("^(?:LG-?)?0*(\\d{1,18})$", Pattern.CASE_INSENSITIVE);

    private final PedidoRepository pedidoRepository;
    private final NombresDeUsuarios nombres;

    public ListarPedidosUseCase(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.nombres = new NombresDeUsuarios(usuarioRepository);
    }

    /**
     * @param estado   vacío para ver todos los estados
     * @param busqueda número de pedido o nombre de la clienta; null o vacío para no buscar
     */
    public List<VistaPedido> ejecutar(Optional<EstadoPedido> estado, String busqueda) {
        List<Pedido> pedidos = pedidoRepository.listar(estado);
        Map<Long, String> nombresUsuarios = nombres.de(pedidos);
        String texto = busqueda == null ? "" : busqueda.strip();

        return pedidos.stream()
                .map(pedido -> new VistaPedido(pedido, nombresUsuarios))
                .filter(vista -> texto.isEmpty() || coincide(vista, texto))
                .toList();
    }

    private static boolean coincide(VistaPedido vista, String texto) {
        Matcher numero = NUMERO.matcher(texto);
        if (numero.matches() && String.valueOf(vista.pedido().getId()).equals(numero.group(1))) {
            return true;
        }
        return normalizar(vista.nombreClienta()).contains(normalizar(texto));
    }

    /** Minúsculas y sin tildes, para que "maria" encuentre a "María". */
    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
