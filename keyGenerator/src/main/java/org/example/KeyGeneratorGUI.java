package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;

public class KeyGeneratorGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(KeyGeneratorGUI::createAndShowGUI);
    }

    public static void createAndShowGUI() {

        JFrame frame = new JFrame("Generowanie Kluczy RSA");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(650, 300);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 218, 232));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel pinLabel = new JLabel("Wprowadź PIN:");
        JTextField pinField = new JTextField(25);
        JButton generateButton = new JButton("Generuj");
        generateButton.setBackground(new Color(255, 128, 174));
        generateButton.setForeground(Color.WHITE);
        JLabel statusLabel = new JLabel("Status: ");
        JLabel statusText = new JLabel("");


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        //pierwsza linia - etykieta pin, pole do wpisywania i przycisk generuj
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(pinLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(pinField, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(generateButton, gbc);


        //druga linia - status i błędy
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(statusText, gbc);

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);


        generateButton.addActionListener(new ActionListener() {
            KeyGenerator keyGenerator = new KeyGenerator();
            @Override
            public void actionPerformed(ActionEvent e) {
                String pin = pinField.getText();
                if (!pin.isEmpty()) {
                    try {
                        keyGenerator.generateKeys(pin);
                        statusText.setText("Klucze zostały wygenerowane! #wow #slay #YASSSQUEEN");
                    } catch (Exception ex) {
                        statusText.setText("Wystąpił błąd: " + ex.getMessage());
                    }
                } else {
                    statusText.setText("PIN nie może być pusty!");
                }
            }
        });
    }
}
