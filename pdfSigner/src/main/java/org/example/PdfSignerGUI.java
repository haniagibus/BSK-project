package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * @file PdfSignerGUI.java
 * @brief Główna klasa aplikacji GUI do podpisywania i weryfikacji dokumentów PDF
 *
 * Aplikacja zapewnia graficzny interfejs użytkownika do podpisywania dokumentów PDF przy użyciu
 * kryptografii asymetrycznej RSA oraz weryfikacji istniejących podpisów cyfrowych.
 *
 * @note Wymaga klasy PdfSigner do wykonywania operacji kryptograficznych
 * @see PdfSigner
 */
public class PdfSignerGUI {

    /** @brief Szerokość okna aplikacji w pikselach */
    private static final int WINDOW_WIDTH = 700;

    /** @brief Wysokość okna aplikacji w pikselach */
    private static final int WINDOW_HEIGHT = 350;

    /** @brief Kolor tła głównego panelu */
    private static final Color BACKGROUND_COLOR = new Color(255, 218, 232);

    /** @brief Kolor przycisków */
    private static final Color BUTTON_COLOR = new Color(255, 128, 174);

    /** @brief Margines wewnętrzny panelu */
    private static final int PANEL_MARGIN = 10;

    /** @brief Szerokość pól tekstowych */
    private static final int TEXT_FIELD_WIDTH = 25;

    /**
     * @brief Metoda uruchamiająca GUI aplikacji
     *
     * Tworzy i wyświetla główne okno aplikacji z interfejsem użytkownika.
     * Konfiguruje wszystkie komponenty GUI oraz obsługę zdarzeń.
     *
     * @see #createMainFrame()
     * @see #createMainPanel()
     * @see #setupEventHandlers(JFrame, JTextField, JPasswordField, JLabel)
     */
    public static void main(String[] args) {
        JFrame frame = createMainFrame();
        JPanel panel = createMainPanel();

        JTextField filePathField = new JTextField(TEXT_FIELD_WIDTH);
        JPasswordField pinField = new JPasswordField(TEXT_FIELD_WIDTH);
        JLabel statusText = new JLabel("");

        addComponentsToPanel(panel, filePathField, pinField, statusText);

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        setupEventHandlers(frame, filePathField, pinField, statusText);
    }

    /**
     * @brief Tworzy główne okno aplikacji
     *
     * @return JFrame - skonfigurowane główne okno aplikacji
     */
    private static JFrame createMainFrame() {
        JFrame frame = new JFrame("BSK - PdfSigner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setResizable(false);
        return frame;
    }

    /**
     * @brief Tworzy główny panel aplikacji z konfiguracją kolorów i marginesów
     *
     * @return JPanel - skonfigurowany panel główny z GridBagLayout
     */
    private static JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(PANEL_MARGIN, PANEL_MARGIN, PANEL_MARGIN, PANEL_MARGIN));
        return panel;
    }

    /**
     * @brief Dodaje wszystkie komponenty GUI do głównego panelu
     *
     * Konfiguruje layout aplikacji używając GridBagConstraints.
     *
     * @param panel Panel główny aplikacji
     * @param filePathField Pole tekstowe dla ścieżki pliku
     * @param pinField Pole hasła dla PIN-u
     * @param statusText Etykieta statusu operacji
     */
    private static void addComponentsToPanel(JPanel panel, JTextField filePathField,
                                             JPasswordField pinField, JLabel statusText) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(PANEL_MARGIN, PANEL_MARGIN, PANEL_MARGIN, PANEL_MARGIN);

        JLabel fileLabel = new JLabel("Wybierz plik PDF:");
        fileLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        JButton browseButton = createButton("Przeglądaj...");

        JLabel pinLabel = new JLabel("Wprowadź PIN:");
        pinLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        JButton signButton = createButton("Podpisz PDF");
        JButton verifyButton = createButton("Zweryfikuj podpis");

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        statusText.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));

        // wiersz 1 - pole ścieżki do pliku i przycisk przeglądaj
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(fileLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(filePathField, gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(browseButton, gbc);

        // wiersz 2 - pole do wpisania PIN-u
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(pinLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(pinField, gbc);

        // wiersz 3 - przyciski podpisu i weryfikacji
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(signButton, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(verifyButton, gbc);

        // wiersz 4 - status
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(statusLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(statusText, gbc);
    }

    /**
     * @brief Tworzy przycisk z zachowaniem wyglądu aplikacji
     *
     * @param text Tekst przycisku
     * @return JButton - przycisk z zastosowanym stylem
     */
    private static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        return button;
    }

    /**
     * @brief Konfiguruje obsługę zdarzeń dla wszystkich interaktywnych komponentów
     *
     * Dodaje akcje dla przycisków przeglądania plików, podpisywania oraz weryfikacji.
     * Implementuje walidację danych wejściowych i wyświetlanie komunikatów o statusie.
     *
     * @param frame Główne okno aplikacji
     * @param filePathField Pole ścieżki pliku
     * @param pinField Pole PIN-u
     * @param statusText Etykieta statusu
     *
     * @see PdfSigner#signPDF(String, String)
     * @see PdfSigner#verifyPDFSignature(String)
     */
    private static void setupEventHandlers(JFrame frame, JTextField filePathField,
                                           JPasswordField pinField, JLabel statusText) {

        JButton browseButton = findButtonByText(frame, "Przeglądaj...");
        JButton signButton = findButtonByText(frame, "Podpisz PDF");
        JButton verifyButton = findButtonByText(frame, "Zweryfikuj podpis");

        if (browseButton != null) {
            browseButton.addActionListener(e -> handleFileBrowsing(frame, filePathField, statusText));
        }

        if (signButton != null) {
            signButton.addActionListener(e -> handlePdfSigning(filePathField, pinField, statusText));
        }

        if (verifyButton != null) {
            verifyButton.addActionListener(e -> handleSignatureVerification(filePathField, statusText));
        }
    }

    /**
     * @brief Znajduje przycisk w kontenerze na podstawie tekstu
     *
     * @param container Kontener do przeszukania
     * @param text Tekst szukanego przycisku
     * @return JButton - znaleziony przycisk lub null
     */
    private static JButton findButtonByText(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton && text.equals(((JButton) component).getText())) {
                return (JButton) component;
            }
            if (component instanceof Container) {
                JButton found = findButtonByText((Container) component, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * @brief Obsługuje przeglądanie i wybieranie pliku PDF
     *
     * @param frame Okno aplikacji
     * @param filePathField Pole do ustawienia ścieżki pliku
     * @param statusText Etykieta statusu
     */
    private static void handleFileBrowsing(JFrame frame, JTextField filePathField, JLabel statusText) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Pliki PDF", "pdf"));
        fileChooser.setDialogTitle("Wybierz dokument PDF do podpisania");

        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            statusText.setText("Wybrano plik: " + selectedFile.getName());
        }
    }

    /**
     * @brief Obsługuje proces podpisywania dokumentu PDF
     *
     * Wykonuje walidację danych wejściowych, wywołuje funkcję podpisywania
     * i wyświetla odpowiedni komunikat o statusie operacji.
     *
     * @param filePathField Pole ze ścieżką pliku
     * @param pinField Pole z PIN-em
     * @param statusText Etykieta statusu
     */
    private static void handlePdfSigning(JTextField filePathField, JPasswordField pinField, JLabel statusText) {
        String filePath = filePathField.getText().trim();
        String pin = new String(pinField.getPassword());

        if (filePath.isEmpty()) {
            statusText.setText("Błąd: Wybierz plik PDF");
            statusText.setForeground(Color.RED);
            return;
        }

        if (pin.isEmpty()) {
            statusText.setText("Błąd: Wprowadź PIN");
            statusText.setForeground(Color.RED);
            return;
        }

        File pdfFile = new File(filePath);
        if (!pdfFile.exists()) {
            statusText.setText("Błąd: Plik nie istnieje");
            statusText.setForeground(Color.RED);
            return;
        }

        statusText.setText("Podpisywanie dokumentu...");
        statusText.setForeground(Color.BLUE);

        SwingUtilities.invokeLater(() -> {
            boolean success = PdfSigner.signPDF(filePath, pin);
            if (success) {
                statusText.setText("Dokument podpisany pomyślnie");
                statusText.setForeground(new Color(0, 128, 0));
            } else {
                statusText.setText("Błąd podczas podpisywania dokumentu");
                statusText.setForeground(Color.RED);
            }

            pinField.setText("");
        });
    }

    /**
     * @brief Obsługuje proces weryfikacji podpisu dokumentu PDF
     *
     * Sprawdza poprawność podpisu cyfrowego dla wybranego dokumentu
     * i wyświetla wynik weryfikacji.
     *
     * @param filePathField Pole ze ścieżką pliku
     * @param statusText Etykieta statusu
     */
    private static void handleSignatureVerification(JTextField filePathField, JLabel statusText) {
        String filePath = filePathField.getText().trim();

        if (filePath.isEmpty()) {
            statusText.setText("Błąd: Wybierz plik PDF");
            statusText.setForeground(Color.RED);
            return;
        }

        File pdfFile = new File(filePath);
        if (!pdfFile.exists()) {
            statusText.setText("Błąd: Plik nie istnieje");
            statusText.setForeground(Color.RED);
            return;
        }

        File signatureFile = new File(filePath + ".sig");
        if (!signatureFile.exists()) {
            statusText.setText("Błąd: Nie znaleziono pliku podpisu (.sig)");
            statusText.setForeground(Color.RED);
            return;
        }

        statusText.setText("Weryfikacja podpisu...");
        statusText.setForeground(Color.BLUE);

        SwingUtilities.invokeLater(() -> {
            boolean valid = PdfSigner.verifyPDFSignature(filePath);
            if (valid) {
                statusText.setText("Podpis jest prawidłowy - dokument nie został zmodyfikowany");
                statusText.setForeground(new Color(0, 128, 0));
            } else {
                statusText.setText("Podpis jest nieprawidłowy lub dokument został zmodyfikowany");
                statusText.setForeground(Color.RED);
            }
        });
    }
}