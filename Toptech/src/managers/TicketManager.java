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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TicketManager actualizado: - notifiers usa CopyOnWriteArrayList para
 * seguridad en concurrencia - addNotifier() evita duplicados - removeNotifier()
 * permite desregistrar listeners (evita fugas y notificaciones duplicadas)
 *
 * Mantiene todos los métodos públicos originales (getAllTickets, addTicket,
 * updateTicket, etc.)
 */
public class TicketManager {

    private TicketDAO dao;
    private static int nextId = 1;
    // Uso CopyOnWriteArrayList para iterar/añadir/remover sin ConcurrentModificationException
    private final List<NotificationService> notifiers = new CopyOnWriteArrayList<>();

    public TicketManager() {
        this.dao = new TicketDAO();
    }

    public TicketManager(Connection connection) {
        this.dao = new TicketDAO(connection);
    }

    // Registro de notificador (evita duplicados)
    public void addNotifier(NotificationService notifier) {
        if (notifier == null) {
            return;
        }
        if (!notifiers.contains(notifier)) {
            notifiers.add(notifier);
        }
    }

    // Nuevo: quitar notificador cuando la UI se cierre
    public void removeNotifier(NotificationService notifier) {
        if (notifier == null) {
            return;
        }
        notifiers.remove(notifier);
    }

    // Notificar creación
    private void notificarCreacion(Ticket ticket) {
        for (NotificationService notifier : notifiers) {
            try {
                notifier.onTicketCreated(ticket);
            } catch (Exception e) {
                // Protegemos para que un fallo en un listener no rompa el flujo
                e.printStackTrace();
            }
        }
    }

    // Notificar actualización
    private void notificarActualizacion(Ticket ticket, String estadoAnterior) {
        for (NotificationService notifier : notifiers) {
            try {
                notifier.onTicketUpdated(ticket, estadoAnterior);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Métodos visibles (mantengo la funcionalidad que ya tienes)
    public void addTicket(Ticket ticket) {
        try {
            dao.addTicket(ticket);
            // notificar creación
            notificarCreacion(ticket);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTicket(Ticket ticket) {
        try {
            // Obtener estado anterior
            Ticket ticketAnterior = dao.findById(ticket.getId());
            String estadoAnterior = (ticketAnterior != null) ? ticketAnterior.getEstado() : "DESCONOCIDO";

            dao.updateTicket(ticket);

            // notificar a listeners
            notificarActualizacion(ticket, estadoAnterior);

            // Mantener notificaciones existentes (email / whatsapp)
            EmailSender.send(ticket.getCorreo(), "Estado de su ticket",
                    "Su ticket " + ticket.getId() + " está en estado: " + ticket.getEstado());
            WhatsAppSender.send(ticket.getCelular(),
                    "Su ticket " + ticket.getId() + " está en estado: " + ticket.getEstado());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Ticket> getTicketsByUserDni(String dni) {
        try {
            return dao.getTicketsByDNI(dni);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
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

    //NUEVOS METODOS
    public String getNombreByDni(String dni) {
        try {
            List<Ticket> tickets = dao.getTicketsByUserDni(dni);
            return (tickets != null && !tickets.isEmpty()) ? tickets.get(0).getCliente() : null;
        } catch (SQLException e) {
            return null;
        }
    }

    public int updateNombreByDni(String dni, String nuevoNombre) {
        try {
            return dao.updateNombreByDni(dni, nuevoNombre);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
