package toptech;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import managers.TicketManager;
import models.Ticket;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ListTicketsWindow extends javax.swing.JFrame {

    private TicketManager ticketManager;
    private JFrame parentWindow;

    // Componentes agregados para búsqueda y exportación

    public ListTicketsWindow(TicketManager ticketManager, JFrame parentWindow) {
        this.ticketManager = ticketManager;
        this.parentWindow = parentWindow;
        initComponents();
        setTitle("Lista de Tickets");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        btnBuscar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                buscarYFiltrarTickets();
            }
        });

        txtBuscar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                buscarYFiltrarTickets();
            }
        });

        btnExportarExcel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exportarTablaAExcel();
            }
        });

        cargarTickets();
    }

    public ListTicketsWindow() {
        initComponents();
btnBuscar.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
buscarYFiltrarTickets();
    }
});
txtBuscar.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        buscarYFiltrarTickets();
    }
});
btnExportarExcel.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        exportarTablaAExcel();
    }
});
    }

    private void cargarTickets() {
        DefaultTableModel model = (DefaultTableModel) tblistadetickets.getModel();
        model.setRowCount(0);

        Ticket[] tickets = ticketManager.getAllTickets();
        for (Ticket t : tickets) {
            model.addRow(new Object[]{
                t.getId(), t.getCliente(), t.getDni(), t.getEquipo(), t.getDescripcion(),
                t.getEstado(), t.getTecnico(), t.getPrioridad(), t.getFechaCreacion(), t.getFechaFin(),
                t.getCorreo(), t.getCelular()
            });
        }
    }

    private void buscarYFiltrarTickets() {
                String filtro = comboFiltro.getSelectedItem().toString();
    String texto = txtBuscar.getText().trim().toLowerCase();

    DefaultTableModel model = (DefaultTableModel) tblistadetickets.getModel();
    model.setRowCount(0);

    Ticket[] tickets = ticketManager.getAllTickets();
    for (Ticket t : tickets) {
        boolean coincide = false;
        if (filtro.equals("ID")) {
            coincide = t.getId().toLowerCase().contains(texto);
        } else if (filtro.equals("DNI")) {
            coincide = t.getDni().toLowerCase().contains(texto);
        } else if (filtro.equals("ESTADO")) {
            coincide = t.getEstado().toLowerCase().contains(texto);
        } else if (filtro.equals("DESCRIPCION")) {
            coincide = t.getDescripcion().toLowerCase().contains(texto);
        }
        if (coincide || texto.isEmpty()) {
            model.addRow(new Object[]{
                t.getId(), t.getCliente(), t.getDni(), t.getEquipo(), t.getDescripcion(),
                t.getEstado(), t.getTecnico(), t.getPrioridad(), t.getFechaCreacion(), t.getFechaFin(),
                t.getCorreo(), t.getCelular()
            });
        }
    }
}

private void exportarTablaAExcel() {
    JFileChooser chooser = new JFileChooser();
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        try {
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Tickets");
            DefaultTableModel model = (DefaultTableModel) tblistadetickets.getModel();

            // Encabezados
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int col = 0; col < model.getColumnCount(); col++) {
                headerRow.createCell(col).setCellValue(model.getColumnName(col));
            }

            // Datos
            for (int row = 0; row < model.getRowCount(); row++) {
                org.apache.poi.ss.usermodel.Row excelRow = sheet.createRow(row + 1);
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object value = model.getValueAt(row, col);
                    excelRow.createCell(col).setCellValue(value == null ? "" : value.toString());
                }
            }

            try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(chooser.getSelectedFile() + ".xlsx")) {
                workbook.write(fileOut);
            }
            workbook.close();
            JOptionPane.showMessageDialog(this, "Exportado correctamente a Excel.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al exportar: " + e.getMessage());
        }
    }
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
        btnVolver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVolverActionPerformed(evt);
            }
        });
        jPanel5.add(btnVolver, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 110, 100, 40));

        btnimprimirpdf.setFont(new java.awt.Font("Decker", 0, 14)); // NOI18N
        btnimprimirpdf.setText("IMPRIMIR PDF");
        btnimprimirpdf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnimprimirpdfActionPerformed(evt);
            }
        });
        jPanel5.add(btnimprimirpdf, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 110, 142, 40));

        jPanel4.setBackground(new java.awt.Color(153, 204, 255));

        txttoptech.setFont(new java.awt.Font("Copperplate Gothic Bold", 1, 48)); // NOI18N
        txttoptech.setText("top tech");

        btnBuscar.setText("Buscar");

        btnExportarExcel.setText("Exportar a Excel");

        lblBuscar.setText("Buscar por:");

        comboFiltro.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "SELECCIONAR ESTADO", "ASIGNADO", "ATENCION", "SOLUCIONADO", "CANCELADO" }));
        comboFiltro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboFiltroActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(172, 172, 172)
                .addComponent(lblBuscar)
                .addGap(18, 18, 18)
                .addComponent(comboFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnBuscar)
                        .addGap(48, 48, 48)
                        .addComponent(btnExportarExcel)
                        .addGap(248, 248, 248))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(txttoptech, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                    .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tablalistausuarios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tablalistausuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    private void btnVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVolverActionPerformed
        // TODO add your handling code here:
        if (parentWindow != null) {
            parentWindow.setVisible(true);
        }
        this.dispose();
    }//GEN-LAST:event_btnVolverActionPerformed

    private void btnimprimirpdfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnimprimirpdfActionPerformed
        // TODO add your handling code here:
        try {
            boolean complete = tblistadetickets.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "Impresión completa.");
            } else {
                JOptionPane.showMessageDialog(this, "Impresión cancelada.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al imprimir: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnimprimirpdfActionPerformed

    private void comboFiltroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboFiltroActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboFiltroActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ListTicketsWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ListTicketsWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ListTicketsWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ListTicketsWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ListTicketsWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnExportarExcel;
    private javax.swing.JButton btnVolver;
    private javax.swing.JButton btnimprimirpdf;
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
    // End of variables declaration//GEN-END:variables
}
