package ar.federicoegas.venceya.domain;

import java.util.List;
import java.util.Optional;

/**
 * Decide si hay que mandar un aviso hoy y de qué umbral.
 * Umbrales: 30, 15, 7 y 1 día antes, y el día del vencimiento (0).
 * Un vencimiento recibe un solo aviso por umbral: si el último aviso fue el de 15 días,
 * el próximo será el de 7, aunque el job corra todos los días.
 * Pasada la fecha se manda un aviso diario de "vencido".
 */
public final class PoliticaDeAlertas {

    public static final List<Integer> UMBRALES = List.of(30, 15, 7, 1, 0);

    private PoliticaDeAlertas() {
    }

    /**
     * @param diasRestantes  días entre hoy y la fecha de vencimiento (negativo si ya venció)
     * @param ultimoUmbralAvisado último umbral notificado, o null si nunca se avisó
     * @return el umbral a notificar hoy, o vacío si no corresponde avisar
     */
    public static Optional<Integer> umbralAAvisar(long diasRestantes, Integer ultimoUmbralAvisado) {
        if (diasRestantes < 0) {
            return Optional.of(-1); // vencido: aviso diario
        }
        for (int umbral : UMBRALES) {
            boolean dentroDelUmbral = diasRestantes <= umbral;
            boolean yaAvisado = ultimoUmbralAvisado != null && ultimoUmbralAvisado <= umbral;
            if (dentroDelUmbral && !yaAvisado && esElUmbralMasChico(diasRestantes, umbral)) {
                return Optional.of(umbral);
            }
        }
        return Optional.empty();
    }

    /** Si faltan 5 días corresponde el umbral de 7, no el de 30 ni el de 15. */
    private static boolean esElUmbralMasChico(long diasRestantes, int umbral) {
        int i = UMBRALES.indexOf(umbral);
        return i == UMBRALES.size() - 1 || diasRestantes > UMBRALES.get(i + 1);
    }
}
