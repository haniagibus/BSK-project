package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;

/**
 * @file KeyGeneratorGUI.java
 * @brief Graficzny interfejs użytkownika do generowania kluczy RSA
 *
 * Klasa KeyGeneratorGUI zapewnia intuicyjny interfejs graficzny oparty na Swing
 * do generowania par kluczy RSA. Umożliwia użytkownikowi wprowadzenie PIN-u
 * i wygenerowanie zaszyfrowanych kluczy.
 */
public class KeyGeneratorGUI {

    /** @brief Kolor tła głównego panelu aplikacji */
    private static final Color BACKGROUND_COLOR = new Color(255, 218, 232);

    /** @brief Kolor tła przycisku generowania */
    private static final Color BUTTON_COLOR = new Color(255, 128, 174);

    /** @brief Szerokość okna aplikacji */
    private static final int WINDOW_WIDTH = 650;

    /** @brief Wysokość okna aplikacji */
    private static final int WINDOW_HEIGHT = 300;

    /** @brief Szerokość pola tekstowego PIN w znakach */
    private static final int PIN_FIELD_WIDTH = 25;

    /** @brief Marginesy wewnętrzne panelu głównego */
    private static final int PANEL_PADDING = 10;

    /** @brief Odstępy między komponentami interfejsu */
    private static final int COMPONENT_SPACING = 10;

    /**
     * @brief Główna metoda aplikacji GUI
     *
     * @param args Argumenty wiersza poleceń (nie używane)
     *
     * @see #createAndShowGUI()
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(KeyGeneratorGUI::createAndShowGUI);
    }

    /**
     * @brief Tworzy i wyświetla główne okno aplikacji
     *
     * Metoda wykonuje następujące kroki:
     * 1. Konfiguruje główne okno aplikacji
     * 2. Tworzy i stylizuje komponenty interfejsu
     * 3. Układa komponenty przy użyciu GridBagLayout
     * 4. Konfiguruje obsługę zdarzeń przycisku
     * 5. Wyświetla okno w centrum ekranu
     *
     * @see #main(String[])
     * @see #setupButtonActionListener(JButton, JTextField, JLabel)
     */
    public static void createAndShowGUI() {
        JFrame frame = new JFrame("BSK - KeyGenerator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, PANEL_PADDING));

        JLabel pinLabel = new JLabel("Wprowadź PIN:");
        JTextField pinField = new JTextField(PIN_FIELD_WIDTH);
        JButton generateButton = new JButton("Generuj");
        generateButton.setBackground(BUTTON_COLOR);
        generateButton.setForeground(Color.WHITE);
        JLabel statusLabel = new JLabel("Status: ");
        JLabel statusText = new JLabel("");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(COMPONENT_SPACING, COMPONENT_SPACING, COMPONENT_SPACING, COMPONENT_SPACING);

        // wiersz 1 - etykieta PIN, pole do wpisywania i przycisk generuj
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

        // wiersz 2 - status i błędy
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

        setupButtonActionListener(generateButton, pinField, statusText);
    }

    /**
     * @brief Konfiguruje obsługę zdarzenia kliknięcia przycisku "Generuj"
     *
     * Metoda tworzy i przypisuje ActionListener do przycisku generowania kluczy.
     * Listener wykonuje następujące operacje:
     * 1. Waliduje wprowadzony PIN (nie może być pusty)
     * 2. Wywołuje generator kluczy z podanym PIN-em
     * 3. Wyświetla komunikat o sukcesie lub błędzie
     *
     * @param generateButton Przycisk "Generuj" do którego przypisywany jest listener
     * @param pinField Pole tekstowe zawierające PIN wprowadzony przez użytkownika
     * @param statusText Etykieta do wyświetlania komunikatów statusu i błędów
     *
     * @note Używa instancji KeyGenerator do generowania kluczy
     * @see KeyGenerator#generateKeys(String)
     * @see #createAndShowGUI()
     */
    private static void setupButtonActionListener(JButton generateButton, JTextField pinField, JLabel statusText) {
        generateButton.addActionListener(new ActionListener() {
            /** @brief Instancja generatora kluczy używana do tworzenia par kluczy RSA */
            KeyGenerator keyGenerator = new KeyGenerator();

            /**
             * @brief Obsługuje zdarzenie kliknięcia przycisku generowania
             *
             * @param e Obiekt zdarzenia ActionEvent
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                String pin = pinField.getText();
                if (!pin.isEmpty()) {
                    try {
                        keyGenerator.generateKeys(pin);
                        statusText.setText("Klucze zostały wygenerowane! ");
                        statusText.setForeground(Color.BLACK);
                    } catch (Exception ex) {
                        statusText.setText("Wystąpił błąd: " + ex.getMessage());
                        statusText.setForeground(Color.RED);
                    }
                } else {
                    statusText.setText("PIN nie może być pusty!");
                    statusText.setForeground(Color.RED);
                }
            }
        });
    }
}