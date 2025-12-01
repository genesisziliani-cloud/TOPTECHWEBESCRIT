/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package toptech;

import java.io.InputStream;
import java.util.Properties;
import managers.UserManager;
import managers.TicketManager;
import models.User;
import notifications.EmailNotificationService;

public class Main {
    public static void main(String[] args) {
        UserManager userManager = new UserManager();
        TicketManager ticketManager = new TicketManager();

        //VERIFICAR si el usuario ya existe antes de crearlo
        User usuarioExistente = userManager.findUserByDNI("00000000");
        if (usuarioExistente == null) {
            userManager.addUser(new User("00000000", "Admin", "Sistema", "admini", "admini", "ADMIN"));
            System.out.println("Usuario admin creado exitosamente");
        } else {
            System.out.println("Usuario admin ya existe: " + usuarioExistente.getNombre() + " " + usuarioExistente.getApellido());
        }

        //Configurar notificaciones por email
        configurarNotificaciones(ticketManager);

        LoginWindow login = new LoginWindow(userManager, ticketManager);
        login.setVisible(true);
    }

    // ✅ NUEVO: Método para configurar notificaciones
    private static void configurarNotificaciones(TicketManager ticketManager) {
        try (InputStream in = Main.class.getResourceAsStream("/smtp.properties")) {
            Properties smtp = new Properties();
            if (in != null) {
                smtp.load(in);
                EmailNotificationService emailNotifier = new EmailNotificationService(smtp);
                ticketManager.addNotifier(emailNotifier);
                System.out.println("Sistema de notificaciones por email configurado");
            } else {
                System.err.println("No se encontro smtp.properties - Las notificaciones por email no funcionaran");
            }
        } catch (Exception e) {
            System.err.println("Error configurando notificaciones: " + e.getMessage());
        }
    }
}