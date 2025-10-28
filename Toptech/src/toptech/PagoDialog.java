package toptech;

import managers.EvidenceManager;
import managers.PaymentManager;
import managers.TicketManager;
import models.Ticket;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PagoDialog extends JDialog {
    private String ticketId;
    private File imagenSeleccionada;

    public PagoDialog(JFrame parent, String ticketId) {
        super(parent, "Pago de Ticket", true);
        this.ticketId = ticketId;
        setSize(500, 400);
        setLayout(new BorderLayout());

        JLabel qrLabel = new JLabel(new ImageIcon("utils/qr_placeholder.png")); // imagen QR
        JButton subirBtn = new JButton("Subir captura");
        JButton enviarBtn = new JButton("Enviar evidencia");

        subirBtn.addActionListener(e -> seleccionarImagen());
        enviarBtn.addActionListener(e -> enviarEvidencia());

        JPanel botones = new JPanel();
        botones.add(subirBtn);
        botones.add(enviarBtn);

        add(qrLabel, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void seleccionarImagen() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            imagenSeleccionada = chooser.getSelectedFile();
            JOptionPane.showMessageDialog(this, "Imagen seleccionada.");
        }
    }

    private void enviarEvidencia() {
        if (imagenSeleccionada != null) {
            new EvidenceManager().guardarEvidencia(ticketId, imagenSeleccionada);

            Ticket ticket = new TicketManager().findTicketById(ticketId);
            double monto = ticket.getMontoReparacion();
            new PaymentManager().registrarPago(ticketId, monto);

            JOptionPane.showMessageDialog(this, "✅ Evidencia enviada y pago registrado.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Selecciona una imagen primero.");
        }
    }
}