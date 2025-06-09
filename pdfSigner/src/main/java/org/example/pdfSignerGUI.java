package org.example;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class pdfSignerGUI {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Podpisywanie Dokumentów PDF");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 350);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 218, 232));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel fileLabel = new JLabel("Wybierz plik PDF:");
        JTextField filePathField = new JTextField(25);
        JButton browseButton = new JButton("Przeglądaj...");
        browseButton.setBackground(new Color(255, 128, 174));
        browseButton.setForeground(Color.WHITE);

        JLabel pinLabel = new JLabel("Wprowadź PIN:");
        JPasswordField pinField = new JPasswordField(25);

        JButton signButton = new JButton("Podpisz PDF");
        signButton.setBackground(new Color(255, 128, 174));
        signButton.setForeground(Color.WHITE);

        JButton verifyButton = new JButton("Zweryfikuj podpis");
        verifyButton.setBackground(new Color(255, 128, 174));
        verifyButton.setForeground(Color.WHITE);

        JLabel statusLabel = new JLabel("Status:");
        JLabel statusText = new JLabel("");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // pierwsza linia - pole do wpisania ścieżki i przycisk przeglądaj
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(fileLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(filePathField, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(browseButton, gbc);

        // druga linia - pole do wpisania PIN
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(pinLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(pinField, gbc);

        // trzecia linia - przyciski podpisu i weryfikacji
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(signButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(verifyButton, gbc);

        // czwarta linia - status
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        panel.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        panel.add(statusText, gbc);

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // wybór pliku
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Pliki PDF", "pdf"));
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        });

        // podpisywanie PDF
        signButton.addActionListener(e -> {
            String filePath = filePathField.getText();
            String pin = new String(pinField.getPassword());
            if (filePath.isEmpty()) {
                statusText.setText("Wybierz plik PDF");
                return;
            }
            if (pin.isEmpty()) {
                statusText.setText("Wprowadź PIN");
                return;
            }

            boolean success = signPDF(filePath, pin);
            if (success) {
                statusText.setText("Dokument podpisany pomyślnie");
            } else {
                statusText.setText("Błąd podczas podpisywania");
            }
        });

        // weryfikacja podpisu
        verifyButton.addActionListener(e -> {
            String filePath = filePathField.getText();
            if (filePath.isEmpty()) {
                statusText.setText("Wybierz plik PDF");
                return;
            }

            boolean valid = verifyPDFSignature(filePath);
            if (valid) {
                statusText.setText("Podpis jest prawidłowy");
            } else {
                statusText.setText("Podpis jest nieprawidłowy");
            }
        });
    }

    private static boolean signPDF(String filePath, String pin) {
        try {
            File encryptedKeyFile = findEncryptedPrivateKey();
            if (encryptedKeyFile == null) {
                System.out.println("Nie znaleziono klucza prywatnego na pendrive.");
                return false;
            }
            if (!encryptedKeyFile.exists()) return false;
            byte[] encryptedPrivateKey = Files.readAllBytes(encryptedKeyFile.toPath());

            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            SecretKey aesKey = new SecretKeySpec(sha.digest(pin.getBytes(StandardCharsets.UTF_8)), "AES");
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] privateKeyBytes = aesCipher.doFinal(Base64.getDecoder().decode(encryptedPrivateKey));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);

            byte[] pdfData = Files.readAllBytes(Paths.get(filePath));
            byte[] pdfHash = MessageDigest.getInstance("SHA-256").digest(pdfData);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(pdfHash);
            byte[] digitalSignature = signature.sign();

            Files.write(Paths.get(filePath + ".sig"), digitalSignature);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private static boolean verifyPDFSignature(String filePath) {
        try {
            byte[] pubBytes = Base64.getDecoder().decode(Files.readString(Paths.get("../KeyGenerator/public_key.pem")));
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubBytes);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(pubKeySpec);

            byte[] pdfData = Files.readAllBytes(Paths.get(filePath));
            byte[] pdfHash = MessageDigest.getInstance("SHA-256").digest(pdfData);
            byte[] signatureBytes = Files.readAllBytes(Paths.get(filePath + ".sig"));

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(pdfHash);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static File findEncryptedPrivateKey() {
        File[] roots = File.listRoots();
        for (File root : roots) {
            File keyFile = new File(root, "private_key.enc");
            if (keyFile.exists()) {
                return keyFile;
            }
        }
        return null;
    }
}
