package dao;

import models.Payment;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;

/**
 * PaymentDAO — adapta la app desktop a la estructura de la BD MySQL (tabla Pagos).
 *
 * - map: ticketRef ("TK001") -> Tickets.id_ticket (idTicket)
 * - operaciones: addPayment, findByTicketRef, existePago, updatePayment
 *
 * Usa reflexión para construir/leer models.Payment en distintas firmas.
 */
public class PaymentDAO {

    private final Connection externalConnection;

    public PaymentDAO() {
        this.externalConnection = null;
    }

    public PaymentDAO(Connection connection) {
        this.externalConnection = connection;
    }

    private Connection getConnection() throws SQLException {
        if (this.externalConnection != null) return this.externalConnection;
        return DBConnection.getConnection();
    }

    /**
     * Agrega un pago. El objeto payment debe contener al menos el ticketRef y monto y estado.
     */
    public void addPayment(Payment payment) throws SQLException {
        String ticketRef = null;
        Double monto = null;
        String estado = null;
        String metodo = null;
        String fechaStr = null;

        try {
            ticketRef = invokeString(payment, "getTicketRef", "getTicket", "getCodigoTicket", "getCodigo");
            monto = invokeDouble(payment, "getMonto", "getAmount", "getAmountPaid");
            estado = invokeString(payment, "getStatus", "getEstado");
            metodo = invokeString(payment, "getMetodo", "getMetodoPago", "getMethod");
            Object dateObj = invokeAny(payment, "getFechaPago", "getDate", "getFecha");
            if (dateObj != null) {
                fechaStr = dateObj.toString();
            }
        } catch (Exception e) {
            // ignore; use defaults
        }

        if (ticketRef == null) throw new SQLException("Payment object lacks ticket reference");

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            // map ticketRef to id_ticket
            String sqlFindTicket = "SELECT id_ticket FROM Tickets WHERE codigo = ?";
            ps = conn.prepareStatement(sqlFindTicket);
            ps.setString(1, ticketRef);
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Ticket not found for code: " + ticketRef);
            }
            int idTicket = rs.getInt("id_ticket");
            rs.close(); ps.close();

            // Insert into Pagos (idTicket, monto, fechaPago, metodoPago, estado)
            String insert = "INSERT INTO Pagos (idTicket, monto, fechaPago, metodoPago, estado) VALUES (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(insert);
            ps.setInt(1, idTicket);
            ps.setDouble(2, monto == null ? 0.0 : monto);
            ps.setString(3, fechaStr == null ? LocalDate.now().toString() : fechaStr);
            ps.setString(4, metodo == null ? "YAPE" : metodo);
            ps.setString(5, estado == null ? "COMPLETADO" : estado);
            ps.executeUpdate();
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
            if (this.externalConnection == null && conn != null) try { conn.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * Actualiza el pago más reciente del ticket (si existe).
     * Usa el ticketRef para mapear al idTicket y actualiza el registro Pagos más reciente.
     */
    public void updatePayment(Payment payment) throws SQLException {
        String ticketRef = invokeString(payment, "getTicketRef", "getTicket", "getCodigoTicket", "getCodigo");
        if (ticketRef == null) throw new SQLException("Payment object lacks ticket reference for update");

        Double monto = invokeDouble(payment, "getMonto", "getAmount", "getAmountPaid");
        String estado = invokeString(payment, "getStatus", "getEstado");
        String metodo = invokeString(payment, "getMetodo", "getMetodoPago", "getMethod");
        Object dateObj = invokeAny(payment, "getFechaPago", "getDate", "getFecha");
        String fechaStr = dateObj == null ? LocalDate.now().toString() : dateObj.toString();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            // obtener id_ticket
            String sqlTicket = "SELECT id_ticket FROM Tickets WHERE codigo = ?";
            ps = conn.prepareStatement(sqlTicket);
            ps.setString(1, ticketRef);
            rs = ps.executeQuery();
            if (!rs.next()) throw new SQLException("Ticket not found: " + ticketRef);
            int idTicket = rs.getInt("id_ticket");
            rs.close(); ps.close();

            // obtener idPago más reciente
            String sqlFindPago = "SELECT idPago FROM Pagos WHERE idTicket = ? ORDER BY idPago DESC LIMIT 1";
            ps = conn.prepareStatement(sqlFindPago);
            ps.setInt(1, idTicket);
            rs = ps.executeQuery();
            if (!rs.next()) {
                // no hay pago para actualizar; insertar uno nuevo
                rs.close(); ps.close();
                addPayment(payment);
                return;
            }
            int idPago = rs.getInt("idPago");
            rs.close(); ps.close();

            // update Pagos set ...
            String update = "UPDATE Pagos SET monto = ?, fechaPago = ?, metodoPago = ?, estado = ? WHERE idPago = ?";
            ps = conn.prepareStatement(update);
            ps.setDouble(1, monto == null ? 0.0 : monto);
            ps.setString(2, fechaStr);
            ps.setString(3, metodo == null ? "YAPE" : metodo);
            ps.setString(4, estado == null ? "COMPLETADO" : estado);
            ps.setInt(5, idPago);
            ps.executeUpdate();

        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
            if (this.externalConnection == null && conn != null) try { conn.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * Busca el pago (último) por ticketRef ("TK001"). Devuelve null si no existe.
     */
    public Payment findByTicketRef(String ticketRef) throws SQLException {
        if (ticketRef == null) return null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            // map code to id_ticket
            String sqlTicket = "SELECT id_ticket FROM Tickets WHERE codigo = ?";
            ps = conn.prepareStatement(sqlTicket);
            ps.setString(1, ticketRef);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            int idTicket = rs.getInt("id_ticket");
            rs.close(); ps.close();

            String sql = "SELECT * FROM Pagos WHERE idTicket = ? ORDER BY idPago DESC LIMIT 1";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idTicket);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }

            double monto = rs.getDouble("monto");
            String fechaPago = rs.getString("fechaPago");
            String metodoPago = rs.getString("metodoPago");
            String estado = rs.getString("estado");

            try {
                try {
                    java.lang.reflect.Constructor<?> c = Payment.class.getConstructor(String.class, double.class, java.time.LocalDate.class, String.class);
                    java.time.LocalDate ld = parseToLocalDate(fechaPago);
                    Object obj = c.newInstance(ticketRef, monto, ld, estado);
                    tryInvokeSetter(obj, "setMetodo", metodoPago);
                    return (Payment) obj;
                } catch (NoSuchMethodException ignore) {}

                try {
                    java.lang.reflect.Constructor<?> c2 = Payment.class.getConstructor(String.class, double.class, String.class, String.class);
                    Object obj = c2.newInstance(ticketRef, monto, fechaPago, estado);
                    tryInvokeSetter(obj, "setMetodo", metodoPago);
                    return (Payment) obj;
                } catch (NoSuchMethodException ignore) {}

                Payment p = Payment.class.getDeclaredConstructor().newInstance();
                tryInvokeSetter(p, "setTicketRef", ticketRef);
                tryInvokeSetter(p, "setMonto", monto);
                tryInvokeSetter(p, "setFechaPago", fechaPago);
                tryInvokeSetter(p, "setStatus", estado);
                tryInvokeSetter(p, "setEstado", estado);
                tryInvokeSetter(p, "setMetodo", metodoPago);
                return p;

            } catch (Exception ex) {
                return null;
            }

        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
            if (this.externalConnection == null && conn != null) try { conn.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * Comprueba si existe un pago para el ticketRef.
     */
    public boolean existePago(String ticketRef) throws SQLException {
        return findByTicketRef(ticketRef) != null;
    }

    // ---------- Helpers reflection/util ----------

    private static String invokeString(Object obj, String... methods) {
        for (String m : methods) {
            try {
                java.lang.reflect.Method method = obj.getClass().getMethod(m);
                Object res = method.invoke(obj);
                if (res instanceof String) return (String) res;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static Double invokeDouble(Object obj, String... methods) {
        for (String m : methods) {
            try {
                java.lang.reflect.Method method = obj.getClass().getMethod(m);
                Object res = method.invoke(obj);
                if (res instanceof Number) return ((Number) res).doubleValue();
                if (res instanceof String) return Double.parseDouble((String) res);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static Object invokeAny(Object obj, String... methods) {
        for (String m : methods) {
            try {
                java.lang.reflect.Method method = obj.getClass().getMethod(m);
                Object res = method.invoke(obj);
                if (res != null) return res;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static LocalDate parseToLocalDate(String s) {
        if (s == null) return LocalDate.now();
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            try {
                String datePart = s.split(" ")[0];
                return LocalDate.parse(datePart);
            } catch (Exception ex) {
                return LocalDate.now();
            }
        }
    }

    private static void tryInvokeSetter(Object obj, String setterName, Object value) {
        try {
            for (java.lang.reflect.Method m : obj.getClass().getMethods()) {
                if (m.getName().equalsIgnoreCase(setterName) && m.getParameterCount() == 1) {
                    Class<?> param = m.getParameterTypes()[0];
                    if (param == String.class) m.invoke(obj, String.valueOf(value));
                    else if (param == double.class || param == Double.class) m.invoke(obj, ((Number) value).doubleValue());
                    else m.invoke(obj, value);
                    return;
                }
            }
        } catch (Exception ignored) {}
    }
}