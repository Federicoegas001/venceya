package ar.federicoegas.venceya.domain;

/** Estado derivado de los días que faltan. No se persiste: se calcula siempre a partir de la fecha. */
public enum EstadoVencimiento {
    VENCIDO,
    CRITICO,   // 0 a 7 días
    PROXIMO,   // 8 a 30 días
    EN_REGLA;  // más de 30 días

    public static EstadoVencimiento desdeDiasRestantes(long dias) {
        if (dias < 0) return VENCIDO;
        if (dias <= 7) return CRITICO;
        if (dias <= 30) return PROXIMO;
        return EN_REGLA;
    }
}
