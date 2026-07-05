package com.reservation.ui;

import com.reservation.db.DatabaseManager;
import com.reservation.model.Booking;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReservationForm extends JFrame {
    private final JTextField nameField;
    private final JTextField trainNumberField;
    private final JTextField trainNameField;
    private final JComboBox<String> classCombo;
    private final JTextField dateField;
    private final JTextField sourceField;
    private final JTextField destField;

    public ReservationForm() {
        setTitle("New Reservation");
        setSize(460, 430);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Passenger Name:"), gbc);
        nameField = new JTextField(18);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Train Number:"), gbc);
        trainNumberField = new JTextField(18);
        gbc.gridx = 1;
        panel.add(trainNumberField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Train Name:"), gbc);
        trainNameField = new JTextField(18);
        trainNameField.setEditable(false);
        gbc.gridx = 1;
        panel.add(trainNameField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Class Type:"), gbc);
        classCombo = new JComboBox<>(new String[]{
                "Sleeper (SL)", "AC 3-Tier (3A)", "AC 2-Tier (2A)", "AC First Class (1A)"
        });
        gbc.gridx = 1;
        panel.add(classCombo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Date of Journey (dd-MM-yyyy):"), gbc);
        dateField = new JTextField(18);
        gbc.gridx = 1;
        panel.add(dateField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Source Station:"), gbc);
        sourceField = new JTextField(18);
        gbc.gridx = 1;
        panel.add(sourceField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Destination Station:"), gbc);
        destField = new JTextField(18);
        gbc.gridx = 1;
        panel.add(destField, gbc);
        row++;

        JButton bookButton = new JButton("Book Ticket");
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(bookButton, gbc);
        row++;

        JLabel note = new JLabel("<html><i>Tip: try train numbers 12301, 12951, 12259, 12626, 12002, 12909</i></html>");
        note.setFont(new Font("SansSerif", Font.PLAIN, 10));
        gbc.gridy = row;
        panel.add(note, gbc);

        add(panel);

        // Auto-populate train name when the user leaves the train number field
        trainNumberField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                autoFillTrainName();
            }
        });
        trainNumberField.addActionListener(e -> autoFillTrainName());

        bookButton.addActionListener(e -> handleBooking());
    }

    private void autoFillTrainName() {
        String num = trainNumberField.getText().trim();
        if (num.isEmpty()) {
            trainNameField.setText("");
            return;
        }
        if (!num.matches("\\d+")) {
            trainNameField.setText("");
            return;
        }
        String name = DatabaseManager.getTrainName(num);
        trainNameField.setText(name != null ? name : "Unknown Train (please verify number)");
    }

    private void handleBooking() {
        String name = nameField.getText().trim();
        String trainNumber = trainNumberField.getText().trim();
        String trainName = trainNameField.getText().trim();
        String classType = (String) classCombo.getSelectedItem();
        String date = dateField.getText().trim();
        String source = sourceField.getText().trim();
        String dest = destField.getText().trim();

        // --- Validation ---
        if (name.isEmpty() || trainNumber.isEmpty() || date.isEmpty() || source.isEmpty() || dest.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!trainNumber.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Train number must be numeric.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(false);
        try {
            sdf.parse(date);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use dd-MM-yyyy.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (source.equalsIgnoreCase(dest)) {
            JOptionPane.showMessageDialog(this, "Source and destination cannot be the same.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (trainName.isEmpty() || trainName.startsWith("Unknown Train")) {
            trainName = "Unknown Train";
        }

        // --- Save booking ---
        String pnr = DatabaseManager.generateUniquePnr();
        String bookingTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now());

        Booking booking = new Booking(pnr, name, trainNumber, trainName, classType, date, source, dest, bookingTime);
        boolean success = DatabaseManager.insertBooking(booking);

        if (success) {
            String details = "PNR: " + pnr +
                    "\nPassenger: " + name +
                    "\nTrain: " + trainNumber + " - " + trainName +
                    "\nClass: " + classType +
                    "\nDate: " + date +
                    "\nFrom: " + source + "   To: " + dest +
                    "\n\nPlease note your PNR number for future reference or cancellation.";
            JOptionPane.showMessageDialog(this, details, "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Booking failed. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
