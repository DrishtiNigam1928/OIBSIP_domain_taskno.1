package com.reservation;

import com.reservation.db.DatabaseManager;
import com.reservation.ui.LoginForm;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Create tables + seed sample data (trains, default admin user) if not already present
        DatabaseManager.initializeDatabase();

        // Launch GUI on the Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
