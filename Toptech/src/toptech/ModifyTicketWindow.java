package toptech;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import managers.TicketManager;
import managers.UserManager;
import managers.PaymentManager;
import models.Ticket;
import models.User;
import models.Payment;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ModifyTicketWindow — corrección: añade import java.io.File para compilar correctamente.
 * Mantiene las funciones: buscar ticket, editar campos, marcar diagnóstico pagado,
 * registrar pago vía PaymentManager.procesarPago(...), ver capturas, y refrescar la lista.
 */
public class ModifyTicketWindow extends javax.swing.JFrame {

    private TicketManager ticketManager;
    private UserManager userManager;
    private PaymentManager paymentManager;
    private JFrame parentWindow;
    private String rol;

    // UI components
    private JTextPane txtidedelticket;
    private JTextPane txtdni1;
    private JTextPane txtcliente;
    private JComboBox<String> equipo;
    private JComboBox<String> estado;
    private JComboBox<String> tecnicos;
    private JComboBox<String> prioridad;
    private JTextPane txtfechadecreacion;
    private JTextPane txtfechafin;
    private JTextPane txtdescripcion;
    private JCheckBox chkDiagnosticoPagado;
    private JTextField txtMontoReparacion;
    private JCheckBox chkPagado;
    private JTextPane txtcorreo;
    private JTextPane txtcelular;
    private JButton btnBuscar;
    private JButton btnGuardar;
    private JButton btnCancelar;
    private JButton btnVerCapturas;

    public ModifyTicketWindow(TicketManager ticketManager, UserManager userManager, JFrame parentWindow, String rol) {
        if (ticketManager == null) throw new IllegalArgumentException("ticketManager cannot be null");
        if (userManager == null) throw new IllegalArgumentException("userManager cannot be null");

        this.ticketManager = ticketManager;
        this.userManager = userManager;
        this.parentWindow = parentWindow;
        this.rol = rol == null ? "" : rol;
        this.paymentManager = new PaymentManager();

        initComponents();
        inicializarTecnicos();
    }

    public ModifyTicketWindow() {
        this(new TicketManager(), new UserManager(), null, "USER");
    }

    private void initComponents() {
        setTitle("Modificar Ticket");
        setSize(980, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBackground(new Color(198, 227, 255));
        getContentPane().add(p);

        JLabel lblTitle = new JLabel("MODIFICAR TICKET");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 16f));
        lblTitle.setBounds(20, 8, 300, 24);
        p.add(lblTitle);

        JLabel lblId = new JLabel("ID TICKET");
        lblId.setBounds(20, 48, 70, 20);
        p.add(lblId);
        txtidedelticket = new JTextPane(); JScrollPane spId = new JScrollPane(txtidedelticket); spId.setBounds(100,48,140,22); p.add(spId);

        btnBuscar = new JButton("BUSCAR"); btnBuscar.setBounds(250,48,100,22); btnBuscar.addActionListener(e -> onBuscar()); p.add(btnBuscar);

        JLabel lblDni = new JLabel("D.N.I / RUC"); lblDni.setBounds(380,48,100,20); p.add(lblDni);
        txtdni1 = new JTextPane(); JScrollPane spDni = new JScrollPane(txtdni1); spDni.setBounds(480,48,140,22); p.add(spDni);

        JLabel lblCliente = new JLabel("CLIENTE"); lblCliente.setBounds(20,88,70,20); p.add(lblCliente);
        txtcliente = new JTextPane(); JScrollPane spCliente = new JScrollPane(txtcliente); spCliente.setBounds(100,88,300,22); p.add(spCliente);

        JLabel lblEquipo = new JLabel("EQUIPO"); lblEquipo.setBounds(420,88,60,20); p.add(lblEquipo);
        equipo = new JComboBox<>(new String[] {"OTROS","PC","LAPTOP","IMPRESORA"}); equipo.setBounds(480,88,140,22); p.add(equipo);

        JLabel lblEstado = new JLabel("ESTADO"); lblEstado.setBounds(640,88,60,20); p.add(lblEstado);
        estado = new JComboBox<>(new String[] {"ASIGNADO","ATENCION","SOLUCIONADO","CANCELADO"}); estado.setBounds(700,88,160,22); p.add(estado);

        JLabel lblDesc = new JLabel("DESCRIPCION"); lblDesc.setBounds(20,128,100,20); p.add(lblDesc);
        txtdescripcion = new JTextPane(); JScrollPane spDesc = new JScrollPane(txtdescripcion); spDesc.setBounds(20,150,560,90); p.add(spDesc);

        JLabel lblFechaCre = new JLabel("FECHA DE CREACION"); lblFechaCre.setBounds(600,128,140,20); p.add(lblFechaCre);
        txtfechadecreacion = new JTextPane(); JScrollPane spFc = new JScrollPane(txtfechadecreacion); spFc.setBounds(600,150,260,22); p.add(spFc);

        JLabel lblFechaFin = new JLabel("FECHA FIN"); lblFechaFin.setBounds(600,185,80,20); p.add(lblFechaFin);
        txtfechafin = new JTextPane(); JScrollPane spFf = new JScrollPane(txtfechafin); spFf.setBounds(600,205,260,22); p.add(spFf);

        JLabel lblCorreo = new JLabel("CORREO"); lblCorreo.setBounds(600,238,60,20); p.add(lblCorreo);
        txtcorreo = new JTextPane(); JScrollPane spCorreo = new JScrollPane(txtcorreo); spCorreo.setBounds(660,238,200,22); p.add(spCorreo);

        JLabel lblCel = new JLabel("CELULAR"); lblCel.setBounds(600,271,60,20); p.add(lblCel);
        txtcelular = new JTextPane(); JScrollPane spCel = new JScrollPane(txtcelular); spCel.setBounds(660,271,200,22); p.add(spCel);

        chkDiagnosticoPagado = new JCheckBox("Diagnóstico pagado"); chkDiagnosticoPagado.setBounds(20,260,180,22); p.add(chkDiagnosticoPagado);

        JLabel lblMonto = new JLabel("MONTO REPARACION:"); lblMonto.setBounds(20,320,150,20); p.add(lblMonto);
        txtMontoReparacion = new JTextField(); txtMontoReparacion.setBounds(180,320,120,22); p.add(txtMontoReparacion);

        chkPagado = new JCheckBox("Pagado"); chkPagado.setBounds(310,320,80,22); p.add(chkPagado);

        btnVerCapturas = new JButton("Ver Capturas"); btnVerCapturas.setBounds(400,320,140,22); btnVerCapturas.addActionListener(e -> onVerCapturas()); p.add(btnVerCapturas);

        JLabel lblTecn = new JLabel("# TECNICO"); lblTecn.setBounds(600,10,80,20); p.add(lblTecn);
        tecnicos = new JComboBox<>(); tecnicos.setBounds(680,10,180,22); p.add(tecnicos);

        JLabel lblPrior = new JLabel("PRIORIDAD"); lblPrior.setBounds(600,40,80,20); p.add(lblPrior);
        prioridad = new JComboBox<>(new String[] {"NORMAL","***ALTA***"}); prioridad.setBounds(680,40,120,22); p.add(prioridad);

        btnGuardar = new JButton("GUARDAR"); btnGuardar.setBounds(300,420,130,36); btnGuardar.addActionListener(e -> onGuardar()); p.add(btnGuardar);
        btnCancelar = new JButton("CANCELAR"); btnCancelar.setBounds(460,420,130,36); btnCancelar.addActionListener(e -> { if (parentWindow!=null) parentWindow.setVisible(true); dispose();}); p.add(btnCancelar);
    }

    private void inicializarTecnicos() {
        tecnicos.removeAllItems();
        if (userManager != null) {
            for (User u : userManager.getAllUsers()) {
                if ("TECNICO".equalsIgnoreCase(u.getRol())) {
                    tecnicos.addItem(u.getNombre() + " " + u.getApellido());
                }
            }
        }
        if (tecnicos.getItemCount() == 0) tecnicos.addItem("SIN TECNICO");
    }

    private void onBuscar() {
        String id = txtidedelticket.getText().trim();
        if (id.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese ID del ticket."); return; }
        Ticket ticket = ticketManager.findTicketById(id);
        if (ticket == null) { JOptionPane.showMessageDialog(this, "Ticket no encontrado."); return; }

        txtcliente.setText(ticket.getCliente());
        txtdni1.setText(ticket.getDni());
        equipo.setSelectedItem(ticket.getEquipo());
        txtdescripcion.setText(ticket.getDescripcion());
        estado.setSelectedItem(ticket.getEstado());
        prioridad.setSelectedItem(ticket.getPrioridad());
        txtfechadecreacion.setText(ticket.getFechaCreacion());
        txtfechafin.setText(ticket.getFechaFin());
        chkDiagnosticoPagado.setSelected(ticket.isDiagnosticoPagado());
        txtMontoReparacion.setText(String.valueOf(ticket.getMontoReparacion()));
        txtcorreo.setText(ticket.getCorreo());
        txtcelular.setText(ticket.getCelular());

        String tecnicoActual = ticket.getTecnico();
        if (tecnicoActual != null && !tecnicoActual.trim().isEmpty()) {
            ensureTecnicoInCombo(tecnicoActual);
            tecnicos.setSelectedItem(tecnicoActual);
        } else if (tecnicos.getItemCount() > 0) {
            tecnicos.setSelectedIndex(0);
        }
    }

    private void ensureTecnicoInCombo(String tecnicoNombre) {
        if (tecnicoNombre == null) return;
        for (int i = 0; i < tecnicos.getItemCount(); i++) {
            String it = tecnicos.getItemAt(i);
            if (tecnicoNombre.equals(it)) return;
        }
        tecnicos.addItem(tecnicoNombre);
    }

    private void onGuardar() {
        String id = txtidedelticket.getText().trim();
        if (id.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese ID del ticket."); return; }
        Ticket ticket = ticketManager.findTicketById(id);
        if (ticket == null) { JOptionPane.showMessageDialog(this, "Ticket no encontrado."); return; }

        String cliente = txtcliente.getText().trim();
        String dni = txtdni1.getText().trim();
        String equipoSel = (String) equipo.getSelectedItem();
        String descripcion = txtdescripcion.getText().trim();
        String estadoSel = (String) estado.getSelectedItem();
        String tecnicoSel = (String) tecnicos.getSelectedItem();
        String prioridadSel = (String) prioridad.getSelectedItem();

        boolean esAdmin = "ADMIN".equalsIgnoreCase(rol);
        if (!esAdmin) { cliente = ticket.getCliente(); dni = ticket.getDni(); }

        if (cliente.isEmpty() || !cliente.matches("[A-Za-z]+ [A-Za-z]+")) {
            JOptionPane.showMessageDialog(this, "Cliente: solo 1 nombre y 1 apellido, letras."); return;
        }
        if (!(dni.length() == 8 || dni.length() == 11) || !dni.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "DNI/RUC: solo 8 o 11 dígitos numéricos."); return;
        }
        if (descripcion.isEmpty()) { JOptionPane.showMessageDialog(this, "Descripción requerida."); return; }
        boolean finEstado = "SOLUCIONADO".equalsIgnoreCase(estadoSel) || "CANCELADO".equalsIgnoreCase(estadoSel);
        if (finEstado && !esAdmin) { JOptionPane.showMessageDialog(this, "Solo ADMIN puede modificar tickets en estado SOLUCIONADO o CANCELADO."); return; }

        if (tecnicoSel == null || tecnicoSel.trim().isEmpty()) {
            tecnicoSel = ticket.getTecnico();
            if (tecnicoSel == null || tecnicoSel.trim().isEmpty()) tecnicoSel = "SIN TECNICO";
            ensureTecnicoInCombo(tecnicoSel);
            tecnicos.setSelectedItem(tecnicoSel);
        }

        ticket.setCliente(cliente);
        ticket.setDni(dni);
        ticket.setEquipo(equipoSel);
        ticket.setDescripcion(descripcion);
        ticket.setEstado(estadoSel);
        ticket.setTecnico(tecnicoSel);
        ticket.setPrioridad(prioridadSel);
        ticket.setDiagnosticoPagado(chkDiagnosticoPagado.isSelected());

        ticket.setCorreo(txtcorreo.getText().trim());
        ticket.setCelular(txtcelular.getText().trim());

        double monto = 0.0;
        String montoText = txtMontoReparacion.getText() == null ? "" : txtMontoReparacion.getText().trim();
        if (!montoText.isEmpty()) {
            montoText = montoText.replace(",", ".");
            try { monto = Double.parseDouble(montoText); } catch (NumberFormatException ex) { monto = 0.0; }
        }
        ticket.setMontoReparacion(monto);

        if (finEstado) ticket.setFechaFin(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        else ticket.setFechaFin("");

        // If "Pagado" checkbox is selected, register payment using PaymentManager
        if (chkPagado.isSelected()) {
            try {
                int numericId = Integer.parseInt(ticket.getId().replaceAll("[^0-9]", ""));
                boolean pagoOk = paymentManager.procesarPago(numericId, monto);
                if (!pagoOk) {
                    JOptionPane.showMessageDialog(this, "No se registró pago (posible pago duplicado).");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error registrando pago: " + ex.getMessage());
            }
        }

        ticketManager.updateTicket(ticket);

        for (Window w : Window.getWindows()) {
            if (w instanceof ListTicketsWindow) {
                ((ListTicketsWindow) w).refresh();
            }
        }

        JOptionPane.showMessageDialog(this, "Ticket modificado correctamente.");
        if (parentWindow != null) parentWindow.setVisible(true);
        dispose();
    }

    private void onVerCapturas() {
        String id = txtidedelticket.getText().trim();
        if (id.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese ID del ticket."); return; }
        try {
            Payment pago = paymentManager.obtenerPagoPorTicket(id);
            if (pago == null) {
                JOptionPane.showMessageDialog(this, "No hay pago registrado ni capturas para este ticket.");
                return;
            }

            String[] candidateMethods = { "getVoucherPath", "getVoucher", "getFilePath", "getFilename", "getImagePath", "getVoucherFilename", "getReceiptPath" };
            String path = null;
            for (String mname : candidateMethods) {
                try {
                    java.lang.reflect.Method m = pago.getClass().getMethod(mname);
                    Object res = m.invoke(pago);
                    if (res instanceof String && ((String)res).trim().length() > 0) {
                        path = (String) res;
                        break;
                    }
                } catch (NoSuchMethodException ignore) {}
            }

            if (path == null) {
                JOptionPane.showMessageDialog(this, "No se encontró ruta de captura en el registro de pago.");
                return;
            }

            File f = new File(path);
            if (!f.exists()) {
                JOptionPane.showMessageDialog(this, "Archivo no encontrado: " + path);
                return;
            }

            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage();
            Image scaled = img.getScaledInstance(600, -1, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaled);
            JLabel lbl = new JLabel(scaledIcon);
            JScrollPane sp = new JScrollPane(lbl);
            sp.setPreferredSize(new Dimension(620, 420));

            JOptionPane.showMessageDialog(this, sp, "Captura de pago: " + f.getName(), JOptionPane.PLAIN_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener captura: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}