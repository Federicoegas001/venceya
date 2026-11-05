package ar.federicoegas.venceya.notify;

/** Canal de aviso. Hoy es mail (AWS SES por SMTP); mañana puede ser WhatsApp o Slack. */
public interface Notificador {
    void enviar(String destinatario, String asunto, String cuerpo);
}
