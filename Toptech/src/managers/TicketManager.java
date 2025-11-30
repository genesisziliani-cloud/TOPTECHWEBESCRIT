package managers;

import dao.TicketDAO;
import models.Ticket;
import utils.EmailSender;
import utils.WhatsAppSender;
import notifications.NotificationService;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class TicketManager {

    private TicketDAO dao;
    private static int nextId = 1;
    private final List<NotificationService> notifiers = new ArrayList<>(); // ✅ NUEVO

    public TicketManager() {
        this.dao = new TicketDAO();
    }

    public TicketManager(Connection connection) {
        this.dao = new TicketDAO(connection);
    }

    // ✅ NUEVO: Método para agregar notificadores
    public void addNotifier(NotificationService notifier) {
        if (notifier != null) {
            notifiers.add(notifier);
        }
    }

    // ✅ NUEVO: Método para notificar creación
    private void notificarCreacion(Ticket ticket) {
        for (NotificationService notifier : notifiers) {
            notifier.onTicketCreated(ticket);
        }
    }

    // ✅ NUEVO: Método para notificar actualización
    private void notificarActualizacion(Ticket ticket, String estadoAnterior) {
        for (NotificationService notifier : notifiers) {
            notifier.onTicketUpdated(ticket, estadoAnterior);
        }
    }

    // ✅ MODIFICADO: addTicket con notificaciones
    public void addTicket(Ticket ticket) {
        try {
            dao.addTicket(ticket);
            notificarCreacion(ticket); // ✅ NUEVA NOTIFICACIÓN
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ MODIFICADO: updateTicket con notificaciones mejoradas
    public void updateTicket(Ticket ticket) {
        try {
            // Obtener estado anterior antes de actualizar
            Ticket ticketAnterior = dao.findById(ticket.getId());
            String estadoAnterior = (ticketAnterior != null) ? ticketAnterior.getEstado() : "DESCONOCIDO";

            dao.updateTicket(ticket);

            // ✅ NUEVO: Sistema de notificaciones mejorado
            notificarActualizacion(ticket, estadoAnterior);

            // ✅ MANTENER: Notificaciones existentes (opcional - puedes eliminarlas)
            EmailSender.send(ticket.getCorreo(), "Estado de su ticket",
                    "Su ticket " + ticket.getId() + " está en estado: " + ticket.getEstado());
            WhatsAppSender.send(ticket.getCelular(),
                    "Su ticket " + ticket.getId() + " está en estado: " + ticket.getEstado());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Los demás métodos se mantienen igual...
    public List<Ticket> getTicketsByUserDni(String dni) {
        try {
            return dao.getTicketsByDNI(dni);
        } catch (SQLException e) {
            System.err.println("❌ Error en getTicketsByUserDni: " + e.getMessage());
            return null;
        }
    }

    public String generarNuevoIdTicket() {
        return String.format("TK%03d", nextId++);
    }

    public Ticket findTicketById(String id) {
        try {
            return dao.findById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getNombreByDni(String dni) {
        try {
            List<Ticket> tickets = dao.getTicketsByUserDni(dni);
            return (tickets != null && !tickets.isEmpty()) ? tickets.get(0).getCliente() : null;
        } catch (SQLException e) {
            return null;
        }
    }

    public Ticket[] getAllTickets() {
        try {
            List<Ticket> list = dao.getAllTickets();
            return list.toArray(new Ticket[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Ticket[0];
        }
    }

    public Ticket[] getTicketsByDNI(String dni) {
        try {
            List<Ticket> list = dao.getTicketsByDNI(dni);
            return list.toArray(new Ticket[0]);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Ticket[0];
        }
    }
}
