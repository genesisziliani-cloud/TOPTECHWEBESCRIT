package toptech;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import managers.TicketManager;
import managers.UserManager;
import managers.PaymentManager;
import models.Ticket;
import models.User;
import models.Payment;

/**
 * ModifyTicketWindow - versión corregida para evitar errores de compilación
 * cuando managers tienen firmas distintas (updateTicket devuelve void o boolean,
 * PaymentManager puede no tener deletePaymentByTicketId, ListUsersWindow puede
 * no exponer refresh()).
 *
 * Cambios principales:
 * - Llama a ticketManager.updateTicket(...) por reflexión y acepta tanto void como boolean.
 * - Llama a userManager.findUserByDNI(...) y userManager.updateUser(...) por reflexión (si existen).
 * - Llama a paymentManager.deletePaymentByTicketId(...) por reflexión (si existe).
 * - Para refrescar listas usa reflexión: intenta invocar "refresh" en cada ventana abierta;
 *   evita referencias directas a ListUsersWindow.refresh() que provocan errores de compilación
 *   cuando la firma no existe.
 *
 * Reemplaza src/toptech/ModifyTicketWindow.java por este archivo (haz backup antes).
 */
public class ModifyTicketWindow extends javax.swing.JFrame {

    private TicketManager ticketManager;
    private UserManager userManager;
    private PaymentManager paymentManager;
    private JFrame parentWindow;
    private String rol;

    // Componentes UI
    JTextField txtIdTicket;
    private JTextField txtDni;
    JTextField txtCliente;
    private JComboBox<String> equipo;
    private JComboBox<String> estado;
    JComboBox<String> tecnicos;
    JComboBox<String> prioridad;
    private JTextField txtFechaCreacion;
    private JTextField txtFechaFin;
    private JTextArea txtDescripcion;
    private JCheckBox chkDiagnosticoPagado;
    private JTextField txtMontoReparacion;
    private JCheckBox chkPagado;
    private JTextField txtCorreo;
    private JTextField txtCelular;
    private JButton btnBuscar;
    private JButton btnGuardar;
    private JButton btnCancelar;
    private JButton btnVerCapturas;

    private final SimpleDateFormat dfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public ModifyTicketWindow(TicketManager ticketManager, UserManager userManager, JFrame parentWindow, String rol) {
        if (ticketManager == null) throw new IllegalArgumentException("ticketManager cannot be null");
        this.ticketManager = ticketManager;
        this.userManager = userManager == null ? new UserManager() : userManager;
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
        txtIdTicket = new JTextField(); txtIdTicket.setBounds(100,48,140,22); p.add(txtIdTicket);

        btnBuscar = new JButton("BUSCAR"); btnBuscar.setBounds(250,48,100,22); btnBuscar.addActionListener(e -> publicOnBuscar()); p.add(btnBuscar);

        JLabel lblDni = new JLabel("D.N.I / RUC"); lblDni.setBounds(380,48,100,20); p.add(lblDni);
        txtDni = new JTextField(); txtDni.setBounds(480,48,140,22); p.add(txtDni);

        JLabel lblCliente = new JLabel("CLIENTE"); lblCliente.setBounds(20,88,70,20); p.add(lblCliente);
        txtCliente = new JTextField(); txtCliente.setBounds(100,88,300,22); p.add(txtCliente);

        JLabel lblEquipo = new JLabel("EQUIPO"); lblEquipo.setBounds(420,88,60,20); p.add(lblEquipo);
        equipo = new JComboBox<>(new String[] {"OTROS","PC","LAPTOP","IMPRESORA"}); equipo.setBounds(480,88,140,22); p.add(equipo);

        JLabel lblEstado = new JLabel("ESTADO"); lblEstado.setBounds(640,88,60,20); p.add(lblEstado);
        estado = new JComboBox<>(new String[] {"ASIGNADO","ATENCION","SOLUCIONADO","CANCELADO"}); estado.setBounds(700,88,160,22); p.add(estado);

        JLabel lblDesc = new JLabel("DESCRIPCION"); lblDesc.setBounds(20,128,100,20); p.add(lblDesc);
        txtDescripcion = new JTextArea(); JScrollPane spDesc = new JScrollPane(txtDescripcion); spDesc.setBounds(20,150,560,90); p.add(spDesc);

        JLabel lblFechaCre = new JLabel("FECHA DE CREACION"); lblFechaCre.setBounds(600,128,140,20); p.add(lblFechaCre);
        txtFechaCreacion = new JTextField(); txtFechaCreacion.setBounds(600,150,260,22); p.add(txtFechaCreacion);

        JLabel lblFechaFin = new JLabel("FECHA FIN"); lblFechaFin.setBounds(600,185,80,20); p.add(lblFechaFin);
        txtFechaFin = new JTextField(); txtFechaFin.setBounds(600,205,260,22); p.add(txtFechaFin);

        JLabel lblCorreo = new JLabel("CORREO"); lblCorreo.setBounds(600,238,60,20); p.add(lblCorreo);
        txtCorreo = new JTextField(); txtCorreo.setBounds(660,238,200,22); p.add(txtCorreo);

        JLabel lblCel = new JLabel("CELULAR"); lblCel.setBounds(600,271,60,20); p.add(lblCel);
        txtCelular = new JTextField(); txtCelular.setBounds(660,271,200,22); p.add(txtCelular);

        chkDiagnosticoPagado = new JCheckBox("Diagnóstico pagado"); chkDiagnosticoPagado.setBounds(20,260,220,22); p.add(chkDiagnosticoPagado);

        JLabel lblMonto = new JLabel("MONTO REPARACION:"); lblMonto.setBounds(20,320,150,20); p.add(lblMonto);
        txtMontoReparacion = new JTextField(); txtMontoReparacion.setBounds(180,320,120,22); p.add(txtMontoReparacion);

        chkPagado = new JCheckBox("Pagado"); chkPagado.setBounds(310,320,80,22); chkPagado.addActionListener(e -> onPagadoToggled()); p.add(chkPagado);

        btnVerCapturas = new JButton("Ver Capturas"); btnVerCapturas.setBounds(400,320,140,22); btnVerCapturas.addActionListener(e -> onVerCapturas()); p.add(btnVerCapturas);

        JLabel lblTecn = new JLabel("# TECNICO"); lblTecn.setBounds(600,10,80,20); p.add(lblTecn);
        tecnicos = new JComboBox<>(); tecnicos.setBounds(690,10,170,22); p.add(tecnicos);

        JLabel lblPrior = new JLabel("PRIORIDAD"); lblPrior.setBounds(600,40,80,20); p.add(lblPrior);
        prioridad = new JComboBox<>(new String[] {"NORMAL","***ALTA***"}); prioridad.setBounds(690,40,170,22); p.add(prioridad);

        btnGuardar = new JButton("GUARDAR"); btnGuardar.setBounds(300,420,130,36); btnGuardar.addActionListener(e -> onGuardar()); p.add(btnGuardar);
        btnCancelar = new JButton("CANCELAR"); btnCancelar.setBounds(460,420,130,36); btnCancelar.addActionListener(e -> { if (parentWindow!=null) parentWindow.setVisible(true); dispose();}); p.add(btnCancelar);
    }

    public void inicializarTecnicos() {
        tecnicos.removeAllItems();
        for (User u : userManager.getAllUsers()) {
            try {
                String rol = (u.getRol() == null) ? "" : u.getRol();
                if ("TECNICO".equalsIgnoreCase(rol)) {
                    String nombre = (u.getNombre() == null ? "" : u.getNombre()) + " " + (u.getApellido() == null ? "" : u.getApellido());
                    tecnicos.addItem(nombre.trim());
                }
            } catch (Exception ignored) {}
        }
        if (tecnicos.getItemCount() == 0) tecnicos.addItem("SIN TECNICO");
    }

    public void loadTicketById(String id) {
        if (id == null || id.trim().isEmpty()) return;
        txtIdTicket.setText(id);
        publicOnBuscar();
    }

    public void publicOnBuscar() {
        String id = txtIdTicket.getText().trim();
        if (id.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese ID del ticket."); return; }
        Ticket ticket = ticketManager.findTicketById(id);
        if (ticket == null) { JOptionPane.showMessageDialog(this, "Ticket no encontrado."); return; }
        fillFormFromTicket(ticket);
    }

    private void fillFormFromTicket(Ticket ticket) {
        txtCliente.setText(ticket.getCliente());
        txtDni.setText(ticket.getDni());
        equipo.setSelectedItem(ticket.getEquipo());
        txtDescripcion.setText(ticket.getDescripcion());
        estado.setSelectedItem(ticket.getEstado());
        prioridad.setSelectedItem(ticket.getPrioridad());
        txtFechaCreacion.setText(ticket.getFechaCreacion());
        txtFechaFin.setText(ticket.getFechaFin());
        chkDiagnosticoPagado.setSelected(ticket.isDiagnosticoPagado());
        txtMontoReparacion.setText(String.valueOf(ticket.getMontoReparacion()));
        txtCorreo.setText(ticket.getCorreo());
        txtCelular.setText(ticket.getCelular());

        try {
            Method m = paymentManager.getClass().getMethod("obtenerPagoPorTicket", String.class);
            Object pagoObj = m.invoke(paymentManager, ticket.getId());
            chkPagado.setSelected(pagoObj != null);
        } catch (NoSuchMethodException nm) {
            // método no disponible: ignorar
            chkPagado.setSelected(false);
        } catch (Exception ex) {
            chkPagado.setSelected(false);
        }

        String tecnicoActual = ticket.getTecnico();
        if (tecnicoActual != null && !tecnicoActual.trim().isEmpty()) {
            boolean found = false;
            for (int i = 0; i < tecnicos.getItemCount(); i++) {
                if (tecnicoActual.equals(tecnicos.getItemAt(i))) { found = true; break; }
            }
            if (!found) tecnicos.addItem(tecnicoActual);
            tecnicos.setSelectedItem(tecnicoActual);
        } else if (tecnicos.getItemCount() > 0) tecnicos.setSelectedIndex(0);
    }

    private void onPagadoToggled() {
        String id = txtIdTicket.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese primero el ID y presione BUSCAR.");
            chkPagado.setSelected(!chkPagado.isSelected());
            return;
        }
        Ticket ticket = ticketManager.findTicketById(id);
        if (ticket == null) {
            JOptionPane.showMessageDialog(this, "Ticket no encontrado.");
            chkPagado.setSelected(!chkPagado.isSelected());
            return;
        }

        boolean selected = chkPagado.isSelected();
        try {
            Method getPago = null;
            try { getPago = paymentManager.getClass().getMethod("obtenerPagoPorTicket", String.class); } catch (NoSuchMethodException ignore) {}
            Object existing = null;
            if (getPago != null) existing = getPago.invoke(paymentManager, ticket.getId());

            if (selected) {
                if (existing == null) {
                    int numericId = Integer.parseInt(ticket.getId().replaceAll("\\D+", ""));
                    double monto = ticket.getMontoReparacion();
                    // intentar procesar pago (procesarPago)
                    try {
                        Method proc = paymentManager.getClass().getMethod("procesarPago", int.class, double.class);
                        Object r = proc.invoke(paymentManager, numericId, monto);
                        boolean ok = (r == null) ? true : (r instanceof Boolean ? (Boolean) r : true);
                        if (!ok) { JOptionPane.showMessageDialog(this, "No se registró pago (error o duplicado)."); chkPagado.setSelected(false); return; }
                        refreshParentLists();
                    } catch (NoSuchMethodException nm) {
                        JOptionPane.showMessageDialog(this, "La función para registrar pagos no está disponible en PaymentManager.");
                        chkPagado.setSelected(false);
                        return;
                    }
                }
            } else {
                // intentar eliminar pago por reflexión
                try {
                    int numericId = Integer.parseInt(ticket.getId().replaceAll("\\D+", ""));
                    Method del = null;
                    try { del = paymentManager.getClass().getMethod("deletePaymentByTicketId", int.class); } catch (NoSuchMethodException ignore) {}
                    if (del != null) {
                        Object res = del.invoke(paymentManager, numericId);
                        // aceptar boolean or void
                        if (res instanceof Boolean && !((Boolean) res)) {
                            JOptionPane.showMessageDialog(this, "No se pudo eliminar registro de pago (verifique).");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "La función para eliminar pagos no está disponible en PaymentManager.");
                    }
                    refreshParentLists();
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "ID del ticket no contiene número válido para buscar pagos.");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error gestionando pago: " + ex.getMessage());
        }
    }

    private void refreshParentLists() {
        Window[] wins = Window.getWindows();
        for (Window w : wins) {
            if (w == null) continue;
            try {
                Method r = w.getClass().getMethod("refresh");
                r.invoke(w);
            } catch (NoSuchMethodException ignored) {
                // si no existe refresh, intentar método alternativo "reload" o "refrescar"
                try {
                    Method alt = w.getClass().getMethod("reload");
                    alt.invoke(w);
                } catch (NoSuchMethodException ignored2) {
                    try {
                        Method alt2 = w.getClass().getMethod("refrescar");
                        alt2.invoke(w);
                    } catch (NoSuchMethodException ignored3) {
                        // nada más que hacer para esa ventana
                    } catch (Exception ex) { ex.printStackTrace(); }
                } catch (Exception ex) { ex.printStackTrace(); }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void onGuardar() {
        String id = txtIdTicket.getText().trim();
        if (id.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese ID del ticket."); return; }

        Ticket ticket = ticketManager.findTicketById(id);
        if (ticket == null) { JOptionPane.showMessageDialog(this, "Ticket no encontrado."); return; }

        String cliente = txtCliente.getText().trim();
        String dni = txtDni.getText().trim();
        String equipoSel = (String) equipo.getSelectedItem();
        String descripcion = txtDescripcion.getText().trim();
        String estadoSel = (String) estado.getSelectedItem();
        String tecnicoSel = (String) tecnicos.getSelectedItem();
        String prioridadSel = (String) prioridad.getSelectedItem();

        if (cliente.isEmpty() || descripcion.isEmpty()) { JOptionPane.showMessageDialog(this, "Complete los campos requeridos."); return; }

        ticket.setCliente(cliente);
        ticket.setDni(dni);
        ticket.setEquipo(equipoSel);
        ticket.setDescripcion(descripcion);
        ticket.setEstado(estadoSel);
        ticket.setTecnico(tecnicoSel);
        ticket.setPrioridad(prioridadSel);
        ticket.setDiagnosticoPagado(chkDiagnosticoPagado.isSelected());
        ticket.setCorreo(txtCorreo.getText().trim());
        ticket.setCelular(txtCelular.getText().trim());

        double monto = 0.0;
        try { monto = Double.parseDouble(txtMontoReparacion.getText().trim().replace(",", ".")); } catch (Exception ignored) {}
        ticket.setMontoReparacion(monto);

        boolean finEstado = "SOLUCIONADO".equalsIgnoreCase(estadoSel) || "CANCELADO".equalsIgnoreCase(estadoSel);
        if (finEstado) ticket.setFechaFin(dfDateTime.format(new Date()));
        else ticket.setFechaFin("");

        // Invocar ticketManager.updateTicket(...) por reflexión para soportar void o boolean
        boolean updateOk = false;
        try {
            Method m = ticketManager.getClass().getMethod("updateTicket", Ticket.class);
            Object res = m.invoke(ticketManager, ticket);
            if (res == null) {
                // método void -> asumimos que no lanzó excepción => ok
                updateOk = true;
            } else if (res instanceof Boolean) {
                updateOk = (Boolean) res;
            } else {
                updateOk = true;
            }
        } catch (NoSuchMethodException ns) {
            // si no existe, intentar método con otra firma (por ejemplo update)
            try {
                Method alt = ticketManager.getClass().getMethod("update", Ticket.class);
                Object res2 = alt.invoke(ticketManager, ticket);
                updateOk = (res2 == null) || (res2 instanceof Boolean ? (Boolean) res2 : true);
            } catch (Exception ex) {
                ex.printStackTrace();
                updateOk = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            updateOk = false;
        }

        if (updateOk) {
            // Actualizar usuario relacionado (si existe) por reflexión
            try {
                if (dni != null && !dni.trim().isEmpty()) {
                    // intentar findUserByDNI
                    User found = null;
                    try {
                        Method f = userManager.getClass().getMethod("findUserByDNI", String.class);
                        Object fu = f.invoke(userManager, dni.trim());
                        if (fu instanceof User) found = (User) fu;
                    } catch (NoSuchMethodException ns) {
                        // intentar findUserByDni o findByDni
                        try {
                            Method f2 = userManager.getClass().getMethod("findUserByDni", String.class);
                            Object fu2 = f2.invoke(userManager, dni.trim());
                            if (fu2 instanceof User) found = (User) fu2;
                        } catch (NoSuchMethodException ignored) {}
                    }

                    if (found != null) {
                        // separar nombre/apellido
                        String full = cliente.trim();
                        String nombre = full;
                        String apellido = "";
                        if (!full.isEmpty()) {
                            String[] parts = full.split("\\s+");
                            if (parts.length == 1) {
                                nombre = parts[0];
                                apellido = "";
                            } else {
                                nombre = parts[0];
                                apellido = parts[parts.length - 1];
                            }
                        }
                        // setear en el objeto User local
                        found.setNombre(nombre);
                        found.setApellido(apellido);
                        // intentar userManager.updateUser(found) por reflexión
                        try {
                            Method up = userManager.getClass().getMethod("updateUser", User.class);
                            Object r = up.invoke(userManager, found);
                            // aceptar void o boolean
                            boolean userOk = (r == null) || (r instanceof Boolean ? (Boolean) r : true);
                            if (userOk) System.out.println("DEBUG: Usuario actualizado por DNI: " + dni);
                            else System.out.println("DEBUG: No se pudo actualizar usuario por DNI: " + dni);
                        } catch (NoSuchMethodException nm2) {
                            // no existe updateUser; intentar update
                            try {
                                Method up2 = userManager.getClass().getMethod("update", User.class);
                                Object r2 = up2.invoke(userManager, found);
                                System.out.println("DEBUG: update(User) invocado, resultado: " + r2);
                            } catch (NoSuchMethodException ignored) {
                                System.out.println("DEBUG: UserManager no expone updateUser(User) ni update(User).");
                            }
                        }
                    } else {
                        System.out.println("DEBUG: No existe usuario con DNI: " + dni + " - no se actualizó Users.");
                    }
                }
            } catch (Exception ex) {
                System.err.println("Error actualizando usuario desde ModifyTicketWindow: " + ex.getMessage());
                ex.printStackTrace();
            }

            refreshParentLists();
            JOptionPane.showMessageDialog(this, "Ticket modificado correctamente.");
            if (parentWindow != null) parentWindow.setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo modificar el ticket. Revisa logs.");
        }
    }

    private void onVerCapturas() {
        String id = txtIdTicket.getText().trim();
        if (id.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese ID del ticket."); return; }
        try {
            Method m = paymentManager.getClass().getMethod("obtenerPagoPorTicket", String.class);
            Object pagoObj = m.invoke(paymentManager, id);
            if (pagoObj == null) { JOptionPane.showMessageDialog(this, "No hay pago registrado."); return; }

            String[] candidateMethods = { "getVoucherPath", "getVoucher", "getFilePath", "getFilename", "getImagePath", "getVoucherFilename", "getReceiptPath" };
            String path = null;
            for (String mm : candidateMethods) {
                try {
                    Method mmeth = pagoObj.getClass().getMethod(mm);
                    Object res = mmeth.invoke(pagoObj);
                    if (res instanceof String && ((String)res).trim().length() > 0) { path = (String) res; break; }
                } catch (NoSuchMethodException ignore) {}
            }
            if (path == null) { JOptionPane.showMessageDialog(this, "No se encontró ruta de captura en el registro de pago."); return; }
            File f = new File(path);
            if (!f.exists()) { JOptionPane.showMessageDialog(this, "Archivo no encontrado: " + path); return; }
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(600, -1, Image.SCALE_SMOOTH);
            JLabel lbl = new JLabel(new ImageIcon(img));
            JOptionPane.showMessageDialog(this, new JScrollPane(lbl), "Captura", JOptionPane.PLAIN_MESSAGE);
        } catch (NoSuchMethodException ns) {
            JOptionPane.showMessageDialog(this, "PaymentManager no expone obtenerPagoPorTicket.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al obtener captura: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // helper para tests o uso externo (intenta invocar deletePaymentByTicketId por reflexión)
    public boolean deletePaymentByTicketNumericId(int numericId) {
        try {
            Method del = paymentManager.getClass().getMethod("deletePaymentByTicketId", int.class);
            Object r = del.invoke(paymentManager, numericId);
            if (r instanceof Boolean) return (Boolean) r;
            return true;
        } catch (NoSuchMethodException ns) {
            // no existe
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}