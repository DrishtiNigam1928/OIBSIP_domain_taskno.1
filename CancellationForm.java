package com.reservation.ui;

import com.reservation.db.DatabaseManager;
import com.reservation.model.Booking;

import javax.swing.*;
import java.awt.*;

public class CancellationForm extends JFrame {
    private final JTextField pnrField;
    private final JTextField nameField, trainField, classField, dateField, sourceField, destField, timeField;
    private final JButton cancelButton;
    private Booking currentBooking;

    public CancellationForm() {
        setTitle("Cancel Booking");
        setSize(460, 440);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("PNR Number:"), gbc);
        pnrField = new JTextField(14);
        gbc.gridx = 1;
        panel.add(pnrField, gbc);
        JButton fetchButton = new JButton("Fetch");
        gbc.gridx = 2;
        panel.add(fetchButton, gbc);
        row++;

        nameField = addReadOnlyRow(panel, gbc, row++, "Passenger Name:");
        trainField = addReadOnlyRow(panel, gbc, row++, "Train:");
        classField = addReadOnlyRow(panel, gbc, row++, "Class:");
        dateField = addReadOnlyRow(panel, gbc, row++, "Date of Journey:");
        sourceField = addReadOnlyRow(panel, gbc, row++, "Source:");
        destField = addReadOnlyRow(panel, gbc, row++, "Destination:");
        timeField = addReadOnlyRow(panel, gbc, row++, "Booked On:");

        cancelButton = new JButton("Cancel Booking");
        cancelButton.setEnabled(false);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        panel.add(cancelButton, gbc);

        add(panel);

        fetchButton.addActionListener(e -> handleFetch());
        pnrField.addActionListener(e -> handleFetch());
        cancelButton.addActionListener(e -> handleCancel());
    }

    private JTextField addReadOnlyRow(JPanel panel, GridBagConstraints gbc, int row, String label) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        JTextField field = new JTextField(18);
        field.setEditable(false);
        gbc.gridx = 1; gbc.gridwidth = 2;
        panel.add(field, gbc);
        return field;
    }

    private void handleFetch() {
        String pnr = pnrField.getText().trim();
        if (pnr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a PNR number.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Booking booking = DatabaseManager.getBookingByPnr(pnr);
        if (booking == null) {
            JOptionPane.showMessageDialog(this, "No booking found for PNR: " + pnr,
                    "Not Found", JOptionPane.ERROR_MESSAGE);
            clearFields();
            cancelButton.setEnabled(false);
            currentBooking = null;
            return;
        }

        currentBooking = booking;
        nameField.setText(booking.getPassengerName());
        trainField.setText(booking.getTrainNumber() + " - " + booking.getTrainName());
        classField.setText(booking.getClassType());
        dateField.setText(booking.getJourneyDate());
        sourceField.setText(booking.getSourceStation());
        destField.setText(booking.getDestinationStation());
        timeField.setText(booking.getBookingTime());
        cancelButton.setEnabled(true);
    }

    private void clearFields() {
        nameField.setText("");
        trainField.setText("");
        classField.setText("");
        dateField.setText("");
        sourceField.setText("");
        destField.setText("");
        timeField.setText("");
    }

    private void handleCancel() {
        if (currentBooking == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel booking PNR: " + currentBooking.getPnr() + "?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = DatabaseManager.deleteBooking(currentBooking.getPnr());
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully.",
                        "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
                pnrField.setText("");
                cancelButton.setEnabled(false);
                currentBooking = null;
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel booking.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
