package dao;

import models.Payment;
import utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    private Connection connection;

    // ✅ CONSTRUCTOR ORIGINAL (para tu app desktop) - MANTENIDO
    public PaymentDAO() {
        // Constructor sin parámetros - tu app desktop lo usa así
    }

    // ✅ NUEVO CONSTRUCTOR (para la app web) - AGREGADO
    public PaymentDAO(Connection connection) {
        this.connection = connection;
    }

    // ✅ MÉTODO AUXILIAR para obtener conexión
    private Connection getConnection() throws SQLException {
        if (this.connection != null) {
            return this.connection; // Usa la conexión proporcionada (web)
        } else {
            return DBConnection.getConnection(); // Usa conexión original (desktop)
        }
    }

    // ✅ TODOS TUS MÉTODOS ORIGINALES - MANTENIDOS 100%
    public void addPayment(Payment p) throws SQLException {
        String sql = "INSERT INTO payments (ticket_ref, amount, date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillStatement(ps, p);
            ps.executeUpdate();
        }
    }

    public void updatePayment(Payment p) throws SQLException {
        String sql = "UPDATE payments SET amount=?, date=?, status=? WHERE ticket_ref=?";
        try (Connection conn = getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, p.getAmount());
            ps.setDate(2, Date.valueOf(p.getDate()));
            ps.setString(3, p.getStatus());
            ps.setString(4, p.getTicketRef());
            ps.executeUpdate();
        }
    }

    public Payment findByTicketRef(String ticketRef) throws SQLException {
        String sql = "SELECT * FROM payments WHERE ticket_ref = ?";
        try (Connection conn = getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticketRef);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return buildPayment(rs);
        }
        return null;
    }

    public boolean existePago(String ticketRef) throws SQLException {
        String sql = "SELECT COUNT(*) FROM payments WHERE ticket_ref = ?";
        try (Connection conn = getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticketRef);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> out = new ArrayList<>();
        String sql = "SELECT * FROM payments";
        try (Connection conn = getConnection(); 
             Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) out.add(buildPayment(rs));
        }
        return out;
    }

    // ✅ MÉTODOS AUXILIARES ORIGINALES - MANTENIDOS
    private void fillStatement(PreparedStatement ps, Payment p) throws SQLException {
        ps.setString(1, p.getTicketRef());
        ps.setDouble(2, p.getAmount());
        ps.setDate(3, Date.valueOf(p.getDate()));
        ps.setString(4, p.getStatus());
    }

    private Payment buildPayment(ResultSet rs) throws SQLException {
        return new Payment(
            rs.getString("ticket_ref"),
            rs.getDouble("amount"),
            rs.getDate("date").toLocalDate(),
            rs.getString("status")
        );
    }
}