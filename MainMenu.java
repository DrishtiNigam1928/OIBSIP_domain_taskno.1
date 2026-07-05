package com.reservation.ui;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {
    public MainMenu() {
        setTitle("Train Reservation System - Main Menu");
        setSize(380, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(3, 1, 12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JButton reserveButton = new JButton("New Reservation");
        JButton cancelButton = new JButton("Cancel Booking");
        JButton exitButton = new JButton("Exit");

        reserveButton.addActionListener(e -> new ReservationForm().setVisible(true));
        cancelButton.addActionListener(e -> new CancellationForm().setVisible(true));
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(reserveButton);
        panel.add(cancelButton);
        panel.add(exitButton);

        add(panel);
    }
}
