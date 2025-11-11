package toptech;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import managers.TicketManager;
import models.Ticket;

/**
 * Versión corregida de ListTicketsWindow:
 * - Búsqueda/filtrado por ID, DNI, ESTADO y DESCRIPCION.
 * - Compatibilidad con el modelo anterior (si el combo trae nombres de estado directamente).
 * - Exportación a CSV (evita dependencias externas a POI/commons-io).
 * - Protecciones contra NPE (ticketManager null).
 *
 * Reemplaza tu archivo actual por este. Haz backup antes.
 */
public class ListTicketsWindow extends javax.swing.JFrame {

    private TicketManager ticketManager;
    private JFrame parentWindow;

    public ListTicketsWindow(TicketManager ticketManager, JFrame parentWindow) {
        this.ticketManager = ticketManager;
        this.parentWindow = parentWindow;
        initComponents();
        configureBehavior();
        setTitle("Lista de Tickets");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        cargarTickets();
    }

    // Constructor vacío usado por el diseñador (evita NPE)
    public ListTicketsWindow() {
        initComponents();
        configureBehavior();
    }

    // Centraliza listeners y comportamientos
    private void configureBehavior() {
        // Si no se recibió TicketManager, creamos uno (para evitar NPE en diseñador).
        if (this.ticketManager == null) {
            this.ticketManager = new TicketManager();
        }

        // Botón Buscar y Enter en txtBuscar
        btnBuscar.addActionListener(e -> buscarYFiltrarTickets());
        txtBuscar.addActionListener(e -> buscarYFiltrarTickets());

        // Botón Exportar -> exportar CSV (más compatible que POI sin configurar dependencias)
        btnExportarExcel.addActionListener(e -> exportarTablaACSV());

        // Manejo del filtro: si se selecciona ESTADO mostramos comboEstado, si no lo ocultamos.
        comboFiltro.addActionListener(e -> {
            String sel = String.valueOf(comboFiltro.getSelectedItem()).trim().toUpperCase();
            // Si el usuario dejó el combo con estados (compatibilidad), también permitimos seleccionar estado directamente.
            if ("ESTADO".equalsIgnoreCase(sel)) {
                comboEstado.setVisible(true);
                txtBuscar.setEnabled(false);
                buscarYFiltrarTickets();
            } else {
                comboEstado.setVisible(false);
                txtBuscar.setEnabled(true);
            }
        });

        // Si cambia comboEstado, refrescar la búsqueda automáticamente
        comboEstado.addActionListener(e -> {
            if (comboEstado.isVisible()) buscarYFiltrarTickets();
        });

        // Volver
        btnVolver.addActionListener(e -> {
            if (parentWindow != null) parentWindow.setVisible(true);
            dispose();
        });
    }

    // Carga todos los tickets en la tabla
    private void cargarTickets() {
        DefaultTableModel model = (DefaultTableModel) tblistadetickets.getModel();
        model.setRowCount(0);

        if (ticketManager == null) return;
        Ticket[] tickets = ticketManager.getAllTickets();
        if (tickets == null) return;

        for (Ticket t : tickets) {
            model.addRow(new Object[]{
                safe(t.getId()), safe(t.getCliente()), safe(t.getDni()), safe(t.getEquipo()), safe(t.getDescripcion()),
                safe(t.getEstado()), safe(t.getTecnico()), safe(t.getPrioridad()), safe(t.getFechaCreacion()), safe(t.getFechaFin()),
                safe(t.getCorreo()), safe(t.getCelular())
            });
        }
    }

    /**
     * Filtrado robusto. El comboFiltro puede tener:
     * - "ID","DNI","ESTADO","DESCRIPCION"  (modo preferido)
     * - O directamente los nombres de estado (ej. "ASIGNADO") — en cuyo caso lo tratamos como filtro por estado.
     */
    private void buscarYFiltrarTickets() {
        String filtroSeleccionado = String.valueOf(comboFiltro.getSelectedItem()).trim();
        String filtro = filtroSeleccionado.toUpperCase();
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();

        DefaultTableModel model = (DefaultTableModel) tblistadetickets.getModel();
        model.setRowCount(0);

        if (ticketManager == null) return;
        Ticket[] tickets = ticketManager.getAllTickets();
        if (tickets == null) return;

        for (Ticket t : tickets) {
            boolean coincide = false;

            // Si el combo trae uno de los tipos esperados:
            if ("ID".equalsIgnoreCase(filtro)) {
                if (texto.isEmpty()) coincide = true;
                else if (t.getId() != null) coincide = t.getId().toLowerCase().contains(texto);
            } else if ("DNI".equalsIgnoreCase(filtro)) {
                if (texto.isEmpty()) coincide = true;
                else if (t.getDni() != null) coincide = t.getDni().toLowerCase().contains(texto);
            } else if ("DESCRIPCION".equalsIgnoreCase(filtro)) {
                if (texto.isEmpty()) coincide = true;
                else if (t.getDescripcion() != null) coincide = t.getDescripcion().toLowerCase().contains(texto);
            } else if ("ESTADO".equalsIgnoreCase(filtro)) {
                // usamos comboEstado para elegir estado concreto
                String estadoSel = String.valueOf(comboEstado.getSelectedItem()).trim().toLowerCase();
                if (estadoSel.isEmpty() || "TODOS".equalsIgnoreCase(estadoSel)) {
                    coincide = true;
                } else if (t.getEstado() != null) {
                    coincide = t.getEstado().toLowerCase().contains(estadoSel);
                }
            } else {
                // Compatibilidad: si el combo trae un nombre de estado directamente (ej. "ASIGNADO"),
                // filtramos por ese estado y ignoramos txtBuscar.
                String posibleEstado = filtro.toLowerCase();
                if (t.getEstado() != null && t.getEstado().toLowerCase().contains(posibleEstado)) {
                    coincide = true;
                } else {
                    // Si no es reconocido, caemos a búsqueda libre por texto (mostrar todo si texto vacío)
                    if (texto.isEmpty()) coincide = true;
                    else {
                        // Buscar en varios campos como fallback
                        if ((t.getId() != null && t.getId().toLowerCase().contains(texto))
                                || (t.getDni() != null && t.getDni().toLowerCase().contains(texto))
                                || (t.getDescripcion() != null && t.getDescripcion().toLowerCase().contains(texto))
                                || (t.getEstado() != null && t.getEstado().toLowerCase().contains(texto))) {
                            coincide = true;
                        }
                    }
                }
            }

            if (coincide) {
                model.addRow(new Object[]{
                    safe(t.getId()), safe(t.getCliente()), safe(t.getDni()), safe(t.getEquipo()), safe(t.getDescripcion()),
                    safe(t.getEstado()), safe(t.getTecnico()), safe(t.getPrioridad()), safe(t.getFechaCreacion()), safe(t.getFechaFin()),
                    safe(t.getCorreo()), safe(t.getCelular())
                });
            }
        }
    }

    // Exporta la tabla mostrada a CSV (compatible y sin dependencias extra).
    private void exportarTablaACSV() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                DefaultTableModel model = (DefaultTableModel) tblistadetickets.getModel();
                File file = chooser.getSelectedFile();
                try (Writer w = new OutputStreamWriter(new FileOutputStream(file + ".csv"), StandardCharsets.UTF_8);
                     PrintWriter pw = new PrintWriter(w)) {

                    // Encabezados
                    for (int c = 0; c < model.getColumnCount(); c++) {
                        pw.print("\"" + model.getColumnName(c).replace("\"", "\"\"") + "\"");
                        if (c < model.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();

                    // Filas
                    for (int r = 0; r < model.getRowCount(); r++) {
                        for (int c = 0; c < model.getColumnCount(); c++) {
                            Object val = model.getValueAt(r, c);
                            String cell = val == null ? "" : String.valueOf(val);
                            cell = cell.replace("\"", "\"\"");
                            pw.print("\"" + cell + "\"");
                            if (c < model.getColumnCount() - 1) pw.print(",");
                        }
                        pw.println();
                    }
                }
                JOptionPane.showMessageDialog(this, "Exportado correctamente a CSV.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exportando CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // ------------------- UI generated code (modificado para incluir comboEstado) -------------------
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        tablalistausuarios = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tblistadetickets = new javax.swing.JTable();
        jScrollBar1 = new javax.swing.JScrollBar();
        jPanel5 = new javax.swing.JPanel();
        btnVolver = new javax.swing.JButton();
        btnimprimirpdf = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        txttoptech = new javax.swing.JLabel();
        btnBuscar = new javax.swing.JButton();
        btnExportarExcel = new javax.swing.JButton();
        lblBuscar = new javax.swing.JLabel();
        comboFiltro = new javax.swing.JComboBox<>();
        txtBuscar = new javax.swing.JTextField();
        comboEstado = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tablalistausuarios.setBackground(new java.awt.Color(51, 102, 255));
        tablalistausuarios.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Lista de tickets", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Decker", 0, 24), new java.awt.Color(255, 255, 255))); // NOI18N
        tablalistausuarios.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblistadetickets.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "CLIENTE", "DNI", "EQUIPO", "DESCRIPCION", "ESTADO", "TECNICO", "PRIORIDAD", "FECHA DE CREACION", "FECHA FIN", "CORREO", "CELULAR"
            }
        ));
        jScrollPane7.setViewportView(tblistadetickets);

        tablalistausuarios.add(jScrollPane7, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 1210, 230));
        tablalistausuarios.add(jScrollBar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1230, 80, -1, 210));

        jPanel5.setBackground(new java.awt.Color(153, 204, 255));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnVolver.setFont(new java.awt.Font("Decker", 0, 14)); // NOI18N
        btnVolver.setText("VOLVER");
        jPanel5.add(btnVolver, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 110, 100, 40));

        btnimprimirpdf.setFont(new java.awt.Font("Decker", 0, 14)); // NOI18N
        btnimprimirpdf.setText("IMPRIMIR PDF");
        btnimprimirpdf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    boolean complete = tblistadetickets.print();
                    if (complete) {
                        JOptionPane.showMessageDialog(null, "Impresión completa.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Impresión cancelada.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error al imprimir: " + ex.getMessage());
                }
            }
        });
        jPanel5.add(btnimprimirpdf, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 110, 142, 40));

        jPanel4.setBackground(new java.awt.Color(153, 204, 255));

        txttoptech.setFont(new java.awt.Font("Copperplate Gothic Bold", 1, 48)); // NOI18N
        txttoptech.setText("top tech");

        btnBuscar.setText("Buscar");

        btnExportarExcel.setText("Exportar a CSV"); // ahora exporta CSV para evitar dependencias

        lblBuscar.setText("Buscar por:");

        // Modelo recomendado del filtro: ID, DNI, ESTADO, DESCRIPCION
        comboFiltro.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ID", "DNI", "ESTADO", "DESCRIPCION" }));

        // comboEstado (visible solo cuando se elige "ESTADO")
        comboEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "TODOS", "ASIGNADO", "ATENCION", "SOLUCIONADO", "CANCELADO" }));
        comboEstado.setVisible(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(172, 172, 172)
                .addComponent(lblBuscar)
                .addGap(18, 18, 18)
                .addComponent(comboFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(comboEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addComponent(btnBuscar)
                .addGap(48, 48, 48)
                .addComponent(btnExportarExcel)
                .addGap(248, 248, 248))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(txttoptech, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 751, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(txttoptech)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBuscar)
                    .addComponent(btnExportarExcel)
                    .addComponent(lblBuscar)
                    .addComponent(comboFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 1272, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tablalistausuarios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tablalistausuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>
    // ------------------- end UI generated code -------------------

    /**
     * Main para pruebas rápidas.
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new ListTicketsWindow().setVisible(true);
        });
    }

    // Variables declaration - do not modify
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnExportarExcel;
    private javax.swing.JButton btnVolver;
    private javax.swing.JButton btnimprimirpdf;
    private javax.swing.JComboBox<String> comboEstado;
    private javax.swing.JComboBox<String> comboFiltro;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollBar jScrollBar1;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JLabel lblBuscar;
    private javax.swing.JPanel tablalistausuarios;
    private javax.swing.JTable tblistadetickets;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JLabel txttoptech;
    // End of variables declaration
}