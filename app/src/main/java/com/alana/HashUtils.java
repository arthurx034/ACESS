package com.alana;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    /**
     * Gera o hash SHA-256 de uma string.
     *
     * @param input String de entrada
     * @return hash SHA-256 em hexadecimal
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));

            // Converte bytes para hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // Teste rápido
    public static void main(String[] args) {
        String texto = "minhaSenha123";
        String hash = sha256(texto);
        System.out.println("SHA-256: " + hash);
    }
}
