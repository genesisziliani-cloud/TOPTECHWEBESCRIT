package toptech;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import managers.TicketManager;
import managers.PaymentManager;
import managers.UserManager;
import models.Ticket;
import models.Payment;
import models.User;

/**
 * ListTicketsWindow — versión con filtro de TÉCNICO como desplegable poblado desde la BD.
 *
 * - Si eliges el filtro "TECNICO" aparece un JComboBox con los técnicos registrados (rol == "TECNICO").
 * - Mantiene: exportar CSV, imprimir (JTable.print), filtros por ID/DNI/NOMBRE/APELLIDO/ESTADO/FECHA/PRIORIDAD/CORREO/CELULAR/ESTADO_DE_PAGO/DESCRIPCION.
 *
 * Reemplaza este archivo en src/toptech/ (haz backup antes).
 */
public class ListTicketsWindow extends javax.swing.JFrame {

    private final TicketManager ticketManager;
    private final PaymentManager paymentManager;
    private final UserManager userManager;
    private JFrame parentWindow;

    // UI components
    private JTable tblistadetickets;
    private JComboBox<String> comboFiltro;
    private JTextField txtBuscar;
    private JSpinner spinnerFrom;
    private JSpinner spinnerTo;
    private JComboBox<String> comboPago;
    private JButton btnBuscar;
    private JButton btnExportarCSV;
    private JButton btnVolver;
    private JButton btnImprimir;

    // New: desplegable de técnicos para filtro
    private JComboBox<String> comboTecnicosFilter;

    private final SimpleDateFormat dfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private final SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");

    public ListTicketsWindow(TicketManager ticketManager, JFrame parentWindow) {
        if (ticketManager == null) throw new IllegalArgumentException("ticketManager no puede ser null");
        this.ticketManager = ticketManager;
        this.paymentManager = new PaymentManager();
        this.userManager = new UserManager(); // usado para poblar la lista de técnicos
        this.parentWindow = parentWindow;
        initComponents();
        configureBehavior();
        cargarTickets();
        poblarComboTecnicos(); // carga técnicos desde la BD
    }

    // Designer / test constructor
    public ListTicketsWindow() {
        this(new TicketManager(), null);
    }

    private void initComponents() {
        setTitle("Lista de Tickets");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1180, 560);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top panel: filtros y botones
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.setBackground(new Color(198, 227, 255));
        top.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        top.add(new JLabel("Filtrar por:"));

        comboFiltro = new JComboBox<>(new String[] {
            "ID","DNI","NOMBRE","APELLIDO","ESTADO","TECNICO",
            "FECHA (RANGO)","PRIORIDAD","CORREO","CELULAR","ESTADO_DE_PAGO","DESCRIPCION"
        });
        top.add(comboFiltro);

        txtBuscar = new JTextField(26);
        top.add(txtBuscar);

        // Combo de técnicos (invisible por defecto; se hará visible si el filtro == "TECNICO")
        comboTecnicosFilter = new JComboBox<>();
        comboTecnicosFilter.setPreferredSize(new Dimension(220, comboTecnicosFilter.getPreferredSize().height));
        comboTecnicosFilter.setVisible(false);
        top.add(comboTecnicosFilter);

        top.add(new JLabel("Desde:"));
        spinnerFrom = createDateSpinner();
        top.add(spinnerFrom);

        top.add(new JLabel("Hasta:"));
        spinnerTo = createDateSpinner();
        top.add(spinnerTo);

        top.add(new JLabel("Pago:"));
        comboPago = new JComboBox<>(new String[] {"TODOS","PAGADO","PENDIENTE PAGO"});
        comboPago.setVisible(false);
        top.add(comboPago);

        btnBuscar = new JButton("Buscar");
        top.add(btnBuscar);

        btnExportarCSV = new JButton("Exportar a CSV");
        top.add(btnExportarCSV);

        add(top, BorderLayout.NORTH);

        // Tabla
        DefaultTableModel model = new DefaultTableModel(
            new Object[][] {},
            new String[] {
                "ID","CLIENTE","DNI","EQUIPO","DESCRIPCION","ESTADO","TECNICO","PRIORIDAD",
                "FECHA DE CREACION","FECHA FIN","CORREO","CELULAR",
                "DIAGNÓSTICO PAGADO","MONTO REPARACION","ESTADO DE PAGO"
            }
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tblistadetickets = new JTable(model);
        tblistadetickets.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {70,150,90,90,300,110,140,90,140,120,180,110,160,110,130};
        for (int i = 0; i < widths.length && i < tblistadetickets.getColumnModel().getColumnCount(); i++) {
            tblistadetickets.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JTableHeader header = tblistadetickets.getTableHeader();
        header.setBackground(new Color(144, 199, 255));
        header.setForeground(Color.BLACK);
        header.setFont(header.getFont().deriveFont(Font.BOLD));

        tblistadetickets.setBackground(new Color(245, 245, 246));
        tblistadetickets.setGridColor(Color.LIGHT_GRAY);
        tblistadetickets.setSelectionBackground(new Color(200, 220, 240));
        tblistadetickets.setSelectionForeground(Color.BLACK);

        JScrollPane scroll = new JScrollPane(tblistadetickets);
        add(scroll, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12));
        bottom.setBackground(new Color(198, 227, 255));
        btnVolver = new JButton("VOLVER");
        btnImprimir = new JButton("IMPRIMIR PDF");
        bottom.add(btnVolver);
        bottom.add(btnImprimir);
        add(bottom, BorderLayout.SOUTH);

        // Ocultar controles que solo aparecen para ciertos filtros
        toggleFilterInputs();
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(110, spinner.getPreferredSize().height));
        spinner.setVisible(false);
        return spinner;
    }

    private void configureBehavior() {
        comboFiltro.addActionListener(e -> toggleFilterInputs());
        btnBuscar.addActionListener(e -> buscarYFiltrarTickets());
        txtBuscar.addActionListener(e -> buscarYFiltrarTickets());

        btnExportarCSV.addActionListener(e -> exportarTablaACSV());

        btnVolver.addActionListener(e -> {
            if (parentWindow != null) parentWindow.setVisible(true);
            dispose();
        });

        btnImprimir.addActionListener(e -> {
            try {
                boolean complete = tblistadetickets.print();
                JOptionPane.showMessageDialog(this, complete ? "Impresión completa." : "Impresión cancelada.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al imprimir: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Doble clic abre detalle (intento robusto usando reflexión para compatibilidad con varias versiones)
        tblistadetickets.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = tblistadetickets.getSelectedRow();
                    if (r >= 0) {
                        String id = String.valueOf(tblistadetickets.getValueAt(r, 0));
                        openModifyWindowWithId(id);
                    }
                }
            }
        });
    }

    /**
     * Intento robusto de abrir ModifyTicketWindow y cargar el ticket por id.
     * - Primero intenta loadTicketById(String)
     * - Si no existe, intenta colocar el id en campos comunes y llamar a métodos públicos de búsqueda
     * - Si todo falla, abre la ventana sin cargar (evita crash)
     */
    private void openModifyWindowWithId(String id) {
        try {
            ModifyTicketWindow mtw = new ModifyTicketWindow(ticketManager, userManager, ListTicketsWindow.this, "ADMIN");

            // 1) intentar método loadTicketById(String)
            try {
                Method m = mtw.getClass().getMethod("loadTicketById", String.class);
                m.invoke(mtw, id);
                mtw.setVisible(true);
                return;
            } catch (NoSuchMethodException ignore) {}

            // 2) intentar colocar el id en campos comunes y llamar a métodos de búsqueda
            String[] possibleFieldNames = new String[] {
                "txtidedelticket", "txtIdTicket", "txtId", "txtidedelTicket", "txtIdDelTicket"
            };
            boolean fieldSet = false;
            for (String fname : possibleFieldNames) {
                try {
                    Field f = mtw.getClass().getDeclaredField(fname);
                    f.setAccessible(true);
                    Object comp = f.get(mtw);
                    if (comp instanceof JTextComponent) {
                        ((JTextComponent) comp).setText(id);
                        fieldSet = true;
                        break;
                    } else if (comp instanceof JTextField) {
                        ((JTextField) comp).setText(id);
                        fieldSet = true;
                        break;
                    } else if (comp instanceof JTextPane) {
                        ((JTextPane) comp).setText(id);
                        fieldSet = true;
                        break;
                    }
                } catch (NoSuchFieldException | IllegalAccessException ignored) {}
            }

            // 3) intentar invocar métodos públicos de búsqueda comunes
            String[] possibleSearchMethods = new String[] {
                "publicOnBuscar", "onBuscar", "cargarTicket", "buscarTicket", "buscar", "loadById"
            };
            if (fieldSet) {
                for (String mname : possibleSearchMethods) {
                    try {
                        Method ms = mtw.getClass().getMethod(mname);
                        ms.invoke(mtw);
                        mtw.setVisible(true);
                        return;
                    } catch (NoSuchMethodException ignored) {}
                }
            }

            // 4) fallback: intentar método que acepte el id pero con nombre alternativo
            String[] alternateLoadNames = new String[] {"loadTicket","loadById","setTicketId"};
            for (String an : alternateLoadNames) {
                try {
                    Method ma = mtw.getClass().getMethod(an, String.class);
                    ma.invoke(mtw, id);
                    mtw.setVisible(true);
                    return;
                } catch (NoSuchMethodException ignored) {}
            }

            // 5) si no pudimos cargar, mostrar la ventana vacía para que el usuario la abra manualmente
            mtw.setVisible(true);
        } catch (Throwable ex) {
            // No queremos que un doble clic rompa la aplicación.
            JOptionPane.showMessageDialog(this, "No se pudo abrir la ventana de modificación automáticamente: " + ex.getMessage(),
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            try {
                ModifyTicketWindow mtw = new ModifyTicketWindow(ticketManager, userManager, ListTicketsWindow.this, "ADMIN");
                mtw.setVisible(true);
            } catch (Exception e2) {
                // si aún falla, lo mostramos por consola
                ex.printStackTrace();
            }
        }
    }

    // Mostrar/ocultar controles según filtro seleccionado
    private void toggleFilterInputs() {
        String sel = String.valueOf(comboFiltro.getSelectedItem());
        boolean isFecha = "FECHA (RANGO)".equalsIgnoreCase(sel);
        boolean isEstadoPago = "ESTADO_DE_PAGO".equalsIgnoreCase(sel);
        boolean isTecnico = "TECNICO".equalsIgnoreCase(sel);

        txtBuscar.setVisible(!isFecha && !isEstadoPago && !isTecnico);
        comboTecnicosFilter.setVisible(isTecnico);
        spinnerFrom.setVisible(isFecha);
        spinnerTo.setVisible(isFecha);
        comboPago.setVisible(isEstadoPago);
    }

    // Poblar comboTecnicosFilter con técnicos registrados en BD (rol "TECNICO")
    private void poblarComboTecnicos() {
        comboTecnicosFilter.removeAllItems();
        comboTecnicosFilter.addItem("TODOS");
        try {
            for (User u : userManager.getAllUsers()) {
                if (u != null && u.getRol() != null && "TECNICO".equalsIgnoreCase(u.getRol())) {
                    String nombre = (u.getNombre() == null ? "" : u.getNombre().trim()) + " " + (u.getApellido() == null ? "" : u.getApellido().trim());
                    nombre = nombre.trim();
                    if (nombre.isEmpty()) continue;
                    // evitar duplicados
                    boolean exists = false;
                    for (int i = 0; i < comboTecnicosFilter.getItemCount(); i++) {
                        if (nombre.equals(comboTecnicosFilter.getItemAt(i))) { exists = true; break; }
                    }
                    if (!exists) comboTecnicosFilter.addItem(nombre);
                }
            }
        } catch (Exception e) {
            // Si falla la consulta a usuarios, dejamos solo "TODOS"
        }
    }

    // Carga todos los tickets
    private void cargarTickets() {
        DefaultTableModel model = (DefaultTableModel) tblistadetickets.getModel();
        model.setRowCount(0);
        Ticket[] tickets = ticketManager.getAllTickets();
        if (tickets == null) return;
        for (Ticket t : tickets) {
            model.addRow(buildRowForTicket(t));
        }
    }

    private Object[] buildRowForTicket(Ticket t) {
        String diag = t.isDiagnosticoPagado() ? "DIAGNÓSTICO PAGADO" : "DIAGNÓSTICO NO PAGADO";
        String monto = String.format("%.2f", t.getMontoReparacion());
        String estadoPago = calcularEstadoPago(t);
        return new Object[] {
            safe(t.getId()),
            safe(t.getCliente()),
            safe(t.getDni()),
            safe(t.getEquipo()),
            safe(t.getDescripcion()),
            safe(t.getEstado()),
            safe(t.getTecnico()),
            safe(t.getPrioridad()),
            safe(t.getFechaCreacion()),
            safe(t.getFechaFin()),
            safe(t.getCorreo()),
            safe(t.getCelular()),
            diag,
            monto,
            estadoPago
        };
    }

    private String calcularEstadoPago(Ticket t) {
        try {
            if (!t.isDiagnosticoPagado()) return "PENDIENTE PAGO";
            double monto = t.getMontoReparacion();
            if (monto <= 0.0) return "SIN MONTO";
            Payment pago = paymentManager.obtenerPagoPorTicket(t.getId());
            if (pago == null) return "PENDIENTE PAGO";
            try {
                java.lang.reflect.Method m = pago.getClass().getMethod("getStatus");
                Object res = m.invoke(pago);
                if (res == null) return "PAGADO";
                String s = String.valueOf(res);
                return s.trim().isEmpty() ? "PAGADO" : s.toUpperCase();
            } catch (Exception ignore) {
                return "PAGADO";
            }
        } catch (Exception e) {
            return "DESCONOCIDO";
        }
    }

    // Filtrado principal
    private void buscarYFiltrarTickets() {
        String filtro = String.valueOf(comboFiltro.getSelectedItem()).trim().toUpperCase();
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        Date from = spinnerFrom.isVisible() ? (Date) spinnerFrom.getValue() : null;
        Date to = spinnerTo.isVisible() ? (Date) spinnerTo.getValue() : null;
        String paymentSel = String.valueOf(comboPago.getSelectedItem()).trim().toUpperCase();

        // Si filtro por tecnico, leer valor del combo
        String tecnicoSel = null;
        if (comboTecnicosFilter.isVisible()) {
            Object sel = comboTecnicosFilter.getSelectedItem();
            tecnicoSel = sel == null ? "" : String.valueOf(sel).trim();
        }

        DefaultTableModel model = (DefaultTableModel) tblistadetickets.getModel();
        model.setRowCount(0);

        Ticket[] tickets = ticketManager.getAllTickets();
        if (tickets == null) return;

        for (Ticket t : tickets) {
            boolean coincide = false;
            switch (filtro) {
                case "ID":
                    coincide = texto.isEmpty() || contains(t.getId(), texto);
                    break;
                case "DNI":
                    coincide = texto.isEmpty() || contains(t.getDni(), texto);
                    break;
                case "NOMBRE":
                    coincide = texto.isEmpty() || matchesNamePart(t.getCliente(), texto, true);
                    break;
                case "APELLIDO":
                    coincide = texto.isEmpty() || matchesNamePart(t.getCliente(), texto, false);
                    break;
                case "ESTADO":
                    coincide = texto.isEmpty() || contains(t.getEstado(), texto);
                    break;
                case "TECNICO":
                    if (tecnicoSel == null || tecnicoSel.isEmpty() || "TODOS".equalsIgnoreCase(tecnicoSel)) {
                        coincide = true;
                    } else {
                        coincide = t.getTecnico() != null && t.getTecnico().toLowerCase().contains(tecnicoSel.toLowerCase());
                    }
                    break;
                case "FECHA (RANGO)":
                    coincide = dateInRange(t.getFechaCreacion(), from, to);
                    break;
                case "PRIORIDAD":
                    coincide = texto.isEmpty() || contains(t.getPrioridad(), texto);
                    break;
                case "CORREO":
                    coincide = texto.isEmpty() || contains(t.getCorreo(), texto);
                    break;
                case "CELULAR":
                    coincide = texto.isEmpty() || contains(t.getCelular(), texto);
                    break;
                case "ESTADO_DE_PAGO":
                    String estadoPago = calcularEstadoPago(t).toUpperCase();
                    if ("TODOS".equalsIgnoreCase(paymentSel) || paymentSel.isEmpty()) coincide = true;
                    else coincide = estadoPago.contains(paymentSel);
                    break;
                case "DESCRIPCION":
                    coincide = texto.isEmpty() || contains(t.getDescripcion(), texto);
                    break;
                default:
                    coincide = texto.isEmpty() ||
                        contains(t.getId(), texto) ||
                        contains(t.getDni(), texto) ||
                        contains(t.getDescripcion(), texto);
            }
            if (coincide) model.addRow(buildRowForTicket(t));
        }
    }

    // Helpers
    private boolean contains(String source, String sub) {
        return source != null && source.toLowerCase().contains(sub);
    }

    private boolean matchesNamePart(String cliente, String textLower, boolean firstName) {
        if (cliente == null || cliente.trim().isEmpty()) return false;
        String[] parts = cliente.trim().split("\\s+");
        if (parts.length == 0) return false;
        String target = firstName ? parts[0] : parts[parts.length - 1];
        return target.toLowerCase().contains(textLower);
    }

    private boolean dateInRange(String ticketDateStr, Date from, Date to) {
        if ((from == null) && (to == null)) return true;
        Date ticketDate = parseDate(ticketDateStr);
        if (ticketDate == null) return false;
        if (from != null) {
            Date fromNorm = stripTimeStart(from);
            if (ticketDate.before(fromNorm)) return false;
        }
        if (to != null) {
            Date toNorm = stripTimeEnd(to);
            if (ticketDate.after(toNorm)) return false;
        }
        return true;
    }

    private Date stripTimeStart(Date d) {
        Calendar c = Calendar.getInstance(); c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private Date stripTimeEnd(Date d) {
        Calendar c = Calendar.getInstance(); c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    private Date parseDate(String s) {
        if (s == null) return null;
        for (SimpleDateFormat df : new SimpleDateFormat[] { dfDateTime, dfDate }) {
            try { df.setLenient(true); return df.parse(s); } catch (ParseException ignored) {}
        }
        return null;
    }

    private void exportarTablaACSV() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File sel = chooser.getSelectedFile();
        if (sel == null) return;
        String path = sel.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".csv")) path += ".csv";
        File out = new File(path);
        File parent = out.getParentFile();
        try {
            if (parent != null && !parent.exists()) parent.mkdirs();
            DefaultTableModel model = (DefaultTableModel) tblistadetickets.getModel();
            try (Writer w = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8);
                 PrintWriter pw = new PrintWriter(w)) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    pw.print("\"" + model.getColumnName(c).replace("\"","\"\"") + "\"");
                    if (c < model.getColumnCount() - 1) pw.print(",");
                }
                pw.println();
                for (int r = 0; r < model.getRowCount(); r++) {
                    for (int c = 0; c < model.getColumnCount(); c++) {
                        Object val = model.getValueAt(r, c);
                        String cell = val == null ? "" : String.valueOf(val).replace("\"","\"\"");
                        pw.print("\"" + cell + "\"");
                        if (c < model.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                }
            }
            JOptionPane.showMessageDialog(this, "Exportado correctamente a: " + out.getAbsolutePath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exportando CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Permitir que otras ventanas fuerce la recarga
    public void refresh() {
        poblarComboTecnicos(); // actualizar lista de técnicos en caso de cambios en usuarios
        cargarTickets();
    }

    private String safe(String s) { return s == null ? "" : s; }
}