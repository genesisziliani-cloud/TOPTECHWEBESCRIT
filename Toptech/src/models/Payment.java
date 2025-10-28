package models;

import java.time.LocalDate;

public class Payment {
    private String ticketRef;
    private double amount;
    private LocalDate date;
    private String status;

    public Payment(String ticketRef, double amount, LocalDate date, String status) {
        this.ticketRef = ticketRef;
        this.amount = amount;
        this.date = date;
        this.status = status;
    }

    public String getTicketRef() { return ticketRef; }
    public void setTicketRef(String ticketRef) { this.ticketRef = ticketRef; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}