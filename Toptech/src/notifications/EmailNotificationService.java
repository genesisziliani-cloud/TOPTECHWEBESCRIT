package notifications;

import models.Ticket;
import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailNotificationService implements NotificationService {

    private final Session session;
    private final String from;

    public EmailNotificationService(Properties smtpProps) {
        this.from = smtpProps.getProperty("mail.from", smtpProps.getProperty("mail.user"));
        this.session = Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        smtpProps.getProperty("mail.user"),
                        smtpProps.getProperty("mail.password")
                );
            }
        });
        this.session.setDebug(false);
    }

    // Metodo privado existente
    private void send(String to, String subject, String body) {
        if (to == null || to.isEmpty()) {
            return;
        }
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            msg.setText(body);
            Transport.send(msg);
            System.out.println("Correo enviado exitosamente a: " + to);
        } catch (MessagingException e) {
            System.err.println("Error enviando correo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // NUEVO: Metodo publico para enviar correos desde cualquier parte
    public void enviarCorreo(String to, String subject, String body) {
        send(to, subject, body);
    }

    // NUEVO: Metodo estatico para facilitar el envio
    public static void enviarCorreoEstatico(String to, String subject, String body) {
        try {
            Properties smtp = new Properties();
            smtp.load(EmailNotificationService.class.getResourceAsStream("/smtp.properties"));
            EmailNotificationService service = new EmailNotificationService(smtp);
            service.send(to, subject, body);
        } catch (Exception e) {
            System.err.println("Error enviando correo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String s(String v) {
        return v == null ? "" : v;
    }

    @Override
    public void onTicketCreated(Ticket t) {
        String subject = "[TOP TECH] Ticket #" + s(t.getId()) + " creado";
        String body
                = "Hola " + s(t.getCliente()) + ",\n\n"
                + "Tu ticket #" + s(t.getId()) + " fue creado.\n"
                + "Equipo/Asunto: " + s(t.getEquipo()) + "\n"
                + "Prioridad: " + s(t.getPrioridad()) + "\n"
                + "Estado: " + s(t.getEstado()) + "\n"
                + "Descripcion: " + s(t.getDescripcion()) + "\n\n"
                + "Gracias por contactarnos.\n— TOP TECH Soporte";
        send(t.getCorreo(), subject, body);
    }

    @Override
    public void onTicketUpdated(Ticket t, String oldEstado) {
        String subject = "[TOP TECH] Ticket #" + s(t.getId()) + " actualizado";
        String body
                = "Hola " + s(t.getCliente()) + ",\n\n"
                + "Tu ticket #" + s(t.getId()) + " cambio de estado:\n"
                + "  " + s(oldEstado) + " → " + s(t.getEstado()) + "\n\n"
                + "Equipo/Asunto: " + s(t.getEquipo()) + "\n"
                + "Prioridad: " + s(t.getPrioridad()) + "\n"
                + "Descripcion: " + s(t.getDescripcion()) + "\n\n"
                + "— TOP TECH Soporte";
        send(t.getCorreo(), subject, body);
    }
}
