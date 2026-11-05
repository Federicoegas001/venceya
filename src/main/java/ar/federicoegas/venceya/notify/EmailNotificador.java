package ar.federicoegas.venceya.notify;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Envía por SMTP. En AWS se usa el endpoint SMTP de SES (email-smtp.us-east-1.amazonaws.com). */
@Component
public class EmailNotificador implements Notificador {

    private final JavaMailSender mailSender;
    private final String remitente;

    public EmailNotificador(JavaMailSender mailSender, @Value("${venceya.mail.remitente}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(remitente);
        msg.setTo(destinatario);
        msg.setSubject(asunto);
        msg.setText(cuerpo);
        mailSender.send(msg);
    }
}
