package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.file.*;
import java.security.MessageDigest;

/**
 * @file KeyGenerator.java
 * @brief Klasa do generowania i zarządzania parami kluczy RSA dla podpisów cyfrowych
 *
 * Klasa KeyGenerator umożliwia generowanie par kluczy RSA, szyfrowanie klucza prywatnego
 * przy użyciu PIN-u.
 * Klucz publiczny jest zapisywany lokalnie, a zaszyfrowany klucz prywatny na pendrive.
 */
public class KeyGenerator {

    /** @brief Rozmiar klucza RSA w bitach */
    private static final int RSA_KEY_SIZE = 4096;

    /** @brief Algorytm haszowania używany do generowania klucza AES */
    private static final String HASH_ALGORITHM = "SHA-256";

    /** @brief Algorytm szyfrowania symetrycznego */
    private static final String AES_ALGORITHM = "AES";

    /** @brief Algorytm kryptografii asymetrycznej */
    private static final String RSA_ALGORITHM = "RSA";

    /** @brief Nazwa pliku z zaszyfrowanym kluczem prywatnym */
    private static final String PRIVATE_KEY_FILE = "private_key.enc";

    /** @brief Nazwa pliku z kluczem publicznym */
    private static final String PUBLIC_KEY_FILE = "public_key.pem";

    /**
     * @brief Wykrywa katalog główny pendrive lub innego nośnika zewnętrznego
     *
     * @return File - obiekt reprezentujący katalog główny pendrive lub null - jeśli nie znaleziono
     *
     * @note Metoda zakłada system Windows
     * @see #generateKeys(String)
     */
    private static File detectPendriveRoot() {
        File[] roots = File.listRoots();
        for (File root : roots) {
            String path = root.getAbsolutePath().toUpperCase();
            if (!path.startsWith("C") && root.canWrite()) {
                return root;
            }
        }
        return null;
    }

    /**
     * @brief Generuje parę kluczy RSA i zapisuje je w odpowiednich lokalizacjach
     *
     * Metoda wykonuje następujące kroki:
     * 1. Wykrywa pendrive lub nośnik zewnętrzny
     * 2. Generuje parę kluczy RSA o rozmiarze 4096 bitów
     * 3. Szyfruje klucz prywatny przy użyciu PIN-u
     * 4. Zapisuje zaszyfrowany klucz prywatny na pendrive
     * 5. Zapisuje klucz publiczny lokalnie w formacie Base64
     *
     * @param PIN PIN służący do szyfrowania klucza prywatnego
     * @throws Exception W przypadku błędów kryptograficznych, I/O lub braku pendrive
     *
     * @see #detectPendriveRoot()
     * @see #generateRSAKeyPair()
     * @see #encryptPrivateKey(PrivateKey, String)
     */
    public void generateKeys(String PIN) throws Exception {
        File pendriveRoot = detectPendriveRoot();
        if (pendriveRoot == null) {
            System.out.println("Pendrive not found");
            return;
        }

        KeyPair keyPair = generateRSAKeyPair();
        String encryptedPrivateKey = encryptPrivateKey(keyPair.getPrivate(), PIN);

        saveToFile(new File(pendriveRoot, PRIVATE_KEY_FILE).getAbsolutePath(), encryptedPrivateKey);
        saveToFile(PUBLIC_KEY_FILE, Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));

        System.out.println("RSA keys generated and saved to: " + pendriveRoot.getAbsolutePath());
    }

    /**
     * @brief Generuje parę kluczy RSA o określonym rozmiarze
     *
     * Metoda tworzy nową parę kluczy RSA składającą się z klucza publicznego
     * i prywatnego o rozmiarze zdefiniowanym w RSA_KEY_SIZE.
     *
     * @return KeyPair - para kluczy RSA (publiczny i prywatny)
     * @throws NoSuchAlgorithmException W przypadku braku dostępności algorytmu RSA
     *
     * @see #generateKeys(String)
     */
    public KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyGen.initialize(RSA_KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    /**
     * @brief Szyfruje klucz prywatny przy użyciu PIN-u i algorytmu AES
     *
     * Metoda generuje klucz AES na podstawie PIN-u (przy użyciu SHA-256),
     * następnie szyfruje klucz prywatny RSA.
     *
     * @param privateKey Klucz prywatny RSA do zaszyfrowania
     * @param pin PIN służący do generowania klucza szyfrującego AES
     * @return String - zaszyfrowany klucz prywatny zakodowany w Base64
     * @throws Exception W przypadku błędów kryptograficznych
     *
     * @see #generateAESKey(String)
     * @see #generateKeys(String)
     */
    private static String encryptPrivateKey(PrivateKey privateKey, String pin) throws Exception {
        SecretKey aesKey = generateAESKey(pin);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedKey = cipher.doFinal(privateKey.getEncoded());
        return Base64.getEncoder().encodeToString(encryptedKey);
    }

    /**
     * @brief Generuje klucz AES na podstawie PIN-u przy użyciu funkcji skrótu SHA-256
     *
     * Metoda tworzy hash SHA-256 z PIN-u i używa go jako klucza AES
     * do szyfrowania klucza prywatnego RSA.
     *
     * @param pin PIN służący do generowania klucza AES
     * @return SecretKey - klucz AES wygenerowany z PIN-u
     * @throws Exception W przypadku błędu algorytmu haszowania
     *
     * @see #encryptPrivateKey(PrivateKey, String)
     */
    private static SecretKey generateAESKey(String pin) throws Exception {
        MessageDigest sha = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] key = sha.digest(pin.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, AES_ALGORITHM);
    }

    /**
     * @brief Zapisuje dane tekstowe do pliku
     *
     * Metoda zapisuje podany ciąg znaków do pliku o określonej ścieżce.
     * Dane są zapisywane w kodowaniu UTF-8.
     *
     * @param filePath Ścieżka do pliku docelowego
     * @param data Dane tekstowe do zapisania
     * @throws IOException W przypadku błędów operacji I/O
     *
     * @see #generateKeys(String)
     */
    private static void saveToFile(String filePath, String data) throws IOException {
        Files.write(Paths.get(filePath), data.getBytes(StandardCharsets.UTF_8));
    }
}