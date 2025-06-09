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

public class KeyGenerator {

    private static final int RSA_KEY_SIZE = 4096;

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

    public void generateKeys(String PIN) throws Exception {
        File pendriveRoot = detectPendriveRoot();
        if (pendriveRoot == null) {
            System.out.println("Pendrive not found");
            return;
        }

        KeyPair keyPair = generateRSAKeyPair();
        String encryptedPrivateKey = encryptPrivateKey(keyPair.getPrivate(), PIN);

        saveToFile(new File(pendriveRoot, "private_key.enc").getAbsolutePath(), encryptedPrivateKey);
        saveToFile("public_key.pem", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));

        System.out.println("RSA keys generated and saved to: " + pendriveRoot.getAbsolutePath());
    }


    public KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(RSA_KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    private static String encryptPrivateKey(PrivateKey privateKey, String pin) throws Exception {
        SecretKey aesKey = generateAESKey(pin);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedKey = cipher.doFinal(privateKey.getEncoded());
        return Base64.getEncoder().encodeToString(encryptedKey);
    }

    private static SecretKey generateAESKey(String pin) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(pin.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, "AES");
    }

    private static void saveToFile(String filePath, String data) throws IOException {
        Files.write(Paths.get(filePath), data.getBytes());
    }
}
