package org.example;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @file PdfSigner.java
 * @brief Klasa do podpisywania i weryfikacji podpisów cyfrowych dokumentów PDF
 *
 * Klasa PdfSigner umożliwia cyfrowe podpisywanie dokumentów PDF przy użyciu
 * kryptografii asymetrycznej RSA oraz weryfikację istniejących podpisów.
 * Klucz prywatny jest przechowywany w zaszyfrowanej formie na nośniku zewnętrznym.
 */
public class PdfSigner {

    /** @brief Nazwa pliku z zaszyfrowanym kluczem prywatnym */
    private static final String PRIVATE_KEY_FILE = "private_key.enc";

    /** @brief Ścieżka do pliku z kluczem publicznym */
    private static final String PUBLIC_KEY_PATH = "../KeyGenerator/public_key.pem";

    /** @brief Algorytm haszowania używany do podpisów */
    private static final String HASH_ALGORITHM = "SHA-256";

    /** @brief Algorytm podpisu cyfrowego */
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    /** @brief Algorytm szyfrowania symetrycznego */
    private static final String AES_ALGORITHM = "AES";

    /** @brief Algorytm kryptografii asymetrycznej */
    private static final String RSA_ALGORITHM = "RSA";

    /**
     * @brief Podpisuje dokument PDF podpisem cyfrowym przy użyciu klucza RSA
     *
     * Metoda wykonuje następujące kroki:
     * 1. Wyszukuje zaszyfrowany klucz prywatny na nośnikach zewnętrznych
     * 2. Odszyfrowuje klucz prywatny przy użyciu PIN-u
     * 3. Generuje hash SHA-256 z pliku PDF
     * 4. Tworzy podpis cyfrowy RSA
     * 5. Zapisuje podpis do pliku z rozszerzeniem .sig
     *
     * @param filePath Ścieżka do pliku PDF do podpisania
     * @param pin PIN służący do odszyfrowania klucza prywatnego
     * @return true - jeśli operacja podpisania się powiodła, false - w przeciwnym razie
     *
     * @throws Exception W przypadku błędów kryptograficznych lub I/O
     *
     * @see #verifyPDFSignature(String)
     * @see #findEncryptedPrivateKey()
     */
    public static boolean signPDF(String filePath, String pin) {
        try {
            File encryptedKeyFile = findEncryptedPrivateKey();
            if (encryptedKeyFile == null) {
                System.err.println("Nie znaleziono zaszyfrowanego klucza prywatnego");
                return false;
            }

            byte[] encryptedPrivateKey = Files.readAllBytes(encryptedKeyFile.toPath());

            MessageDigest sha = MessageDigest.getInstance(HASH_ALGORITHM);
            SecretKey aesKey = new SecretKeySpec(
                    sha.digest(pin.getBytes(StandardCharsets.UTF_8)),
                    AES_ALGORITHM
            );

            Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
            aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] privateKeyBytes = aesCipher.doFinal(
                    Base64.getDecoder().decode(encryptedPrivateKey)
            );

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = KeyFactory.getInstance(RSA_ALGORITHM)
                    .generatePrivate(keySpec);

            byte[] pdfData = Files.readAllBytes(Paths.get(filePath));
            byte[] pdfHash = MessageDigest.getInstance(HASH_ALGORITHM).digest(pdfData);

            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(pdfHash);
            byte[] digitalSignature = signature.sign();

            Files.write(Paths.get(filePath + ".sig"), digitalSignature);
            return true;

        } catch (Exception e) {
            System.err.println("Błąd podczas podpisywania PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @brief Weryfikuje podpis cyfrowy dokumentu PDF na podstawie klucza publicznego
     *
     * Metoda wykonuje następujące kroki:
     * 1. Wczytuje klucz publiczny z pliku PEM
     * 2. Generuje hash SHA-256 z pliku PDF
     * 3. Wczytuje podpis z pliku .sig
     * 4. Weryfikuje podpis przy użyciu klucza publicznego
     *
     * @param filePath Ścieżka do pliku PDF do weryfikacji
     * @return true - jeśli podpis jest prawidłowy i dokument nie został zmodyfikowany,
     *         false - w przeciwnym razie
     *
     * @throws Exception W przypadku błędów kryptograficznych lub I/O
     *
     * @note Wymaga istnienia pliku podpisu o nazwie {filePath}.sig
     * @see #signPDF(String, String)
     */
    public static boolean verifyPDFSignature(String filePath) {
        try {
            byte[] pubBytes = Base64.getDecoder().decode(
                    Files.readString(Paths.get(PUBLIC_KEY_PATH))
            );
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubBytes);
            PublicKey publicKey = KeyFactory.getInstance(RSA_ALGORITHM)
                    .generatePublic(pubKeySpec);

            byte[] pdfData = Files.readAllBytes(Paths.get(filePath));
            byte[] pdfHash = MessageDigest.getInstance(HASH_ALGORITHM).digest(pdfData);

            byte[] signatureBytes = Files.readAllBytes(Paths.get(filePath + ".sig"));

            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(pdfHash);
            return signature.verify(signatureBytes);

        } catch (Exception e) {
            System.err.println("Błąd podczas weryfikacji podpisu PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @brief Wyszukuje pendrive lub nośnik zewnętrzny z plikiem zaszyfrowanego klucza prywatnego
     *
     * @return File - obiekt pliku z zaszyfrowanym kluczem prywatnym lub null - jeśli nie znaleziono
     *
     * @see #signPDF(String, String)
     */
    private static File findEncryptedPrivateKey() {
        File[] roots = File.listRoots();
        for (File root : roots) {
            File keyFile = new File(root, PRIVATE_KEY_FILE);
            if (keyFile.exists() && keyFile.canRead()) {
                System.out.println("Znaleziono klucz prywatny: " + keyFile.getAbsolutePath());
                return keyFile;
            }
        }
        System.err.println("Nie znaleziono pliku klucza prywatnego: " + PRIVATE_KEY_FILE);
        return null;
    }
}