package managers;

import dao.TicketDAO;
import models.Ticket;
import utils.EmailSender;
import utils.WhatsAppSender;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TicketManager {
    private TicketDAO dao;
    private static int nextId = 1;

    // ✅ CONSTRUCTOR ORIGINAL (para tu app desktop)
    public TicketManager() {
        this.dao = new TicketDAO();
    }

    // ✅ CONSTRUCTOR para la app web
    public TicketManager(Connection connection) {
        this.dao = new TicketDAO(connection);
    }

    // ✅ MÉTODO CORREGIDO - Usa el método que SÍ existe en TicketDAO
    public List<Ticket> getTicketsByUserDni(String dni) {
        try {
            // ✅ CORREGIDO: Usar getTicketsByDNI() que SÍ existe en TicketDAO
            return dao.getTicketsByDNI(dni);
        } catch (SQLException e) { 
            System.err.println("❌ Error en getTicketsByUserDni: " + e.getMessage());
            e.printStackTrace();
            return null; 
        }
    }

    // ✅ MÉTODO ELIMINADO - No existe en TicketDAO
    // public List<Ticket> getTicketsByUser(int userId) {
    //     // ❌ ELIMINADO: Este método no existe en TicketDAO
    // }

    // ✅ TODOS TUS MÉTODOS ORIGINALES - MANTENIDOS 100%
    public String generarNuevoIdTicket() { 
        return String.format("TK%03d", nextId++); 
    }

    public void addTicket(Ticket ticket) {
        try { 
            dao.addTicket(ticket); 
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    public void updateTicket(Ticket ticket) {
        try {
            dao.updateTicket(ticket);
            // Notifica cambios de estado - FUNCIONALIDAD ORIGINAL MANTENIDA
            EmailSender.send(ticket.getCorreo(), "Estado de su ticket", "Su ticket " + ticket.getId() + " está en estado: " + ticket.getEstado());
            WhatsAppSender.send(ticket.getCelular(), "Su ticket " + ticket.getId() + " está en estado: " + ticket.getEstado());
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
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
}