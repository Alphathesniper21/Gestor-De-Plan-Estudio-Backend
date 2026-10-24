package co.edu.unbosque.sdgh.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Clase de utilidad para operaciones de cifrado AES y funciones de hash.
 *
 * Proporciona métodos para cifrar y descifrar texto usando AES en modo GCM,
 * así como métodos para generar hashes usando varios algoritmos
 * (MD5, SHA-1, SHA-256, SHA-384, SHA-512).
 *
 * Utiliza únicamente APIs nativas de Java ({@code javax.crypto},
 * {@code java.security}, {@code java.util.Base64}) sin dependencias externas.
 */
public class AESUtil {

	 /** Algoritmo de cifrado simétrico utilizado. */
    private static final String ALGORITMO = "AES";

    /** Transformación completa: algoritmo / modo / padding. */
    private static final String TIPO_CIFRADO = "AES/GCM/NoPadding";

    /** Longitud del tag de autenticación GCM en bits. */
    private static final int GCM_TAG_LENGTH = 128;

    /** Clave predeterminada de 16 bytes (AES-128). */
    private static final String DEFAULT_KEY = "llavede16carater";

    /** Vector de inicialización predeterminado de 16 bytes. */
    private static final String DEFAULT_IV = "programacioncomp";


    /**
     * Cifra un texto utilizando AES-128 en modo GCM sin padding.
     *
     * @param llave  Clave de cifrado (debe tener exactamente 16 caracteres para AES-128).
     * @param iv     Vector de inicialización (debe tener exactamente 16 caracteres).
     * @param texto  Texto plano a cifrar.
     * @return Texto cifrado codificado en Base64, o cadena vacía si ocurre un error.
     */
    public static String encrypt(String llave, String iv, String texto) {
        try {
            Cipher cipher = Cipher.getInstance(TIPO_CIFRADO);
            SecretKeySpec secretKeySpec = new SecretKeySpec(llave.getBytes(), ALGORITMO);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
            byte[] encrypted = cipher.doFinal(texto.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Descifra un texto cifrado con AES-128 en modo GCM.
     *
     * @param llave     Clave de cifrado (debe ser la misma utilizada al cifrar).
     * @param iv        Vector de inicialización (debe ser el mismo utilizado al cifrar).
     * @param encrypted Texto cifrado en formato Base64.
     * @return Texto descifrado en claro, o cadena vacía si ocurre un error.
     */
    public static String decrypt(String llave, String iv, String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance(TIPO_CIFRADO);
            SecretKeySpec secretKeySpec = new SecretKeySpec(llave.getBytes(), ALGORITMO);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
            byte[] enc = Base64.getDecoder().decode(encrypted);
            byte[] decrypted = cipher.doFinal(enc);
            return new String(decrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Cifra un texto usando la clave y el IV predeterminados del proyecto.
     *
     * @param plainText Texto plano a cifrar.
     * @return Texto cifrado en formato Base64.
     */
    public static String encrypt(String plainText) {
        return encrypt(DEFAULT_KEY, DEFAULT_IV, plainText);
    }

    /**
     * Descifra un texto usando la clave y el IV predeterminados del proyecto.
     *
     * @param encrypted Texto cifrado en formato Base64.
     * @return Texto descifrado en claro.
     */
    public static String decrypt(String encrypted) {
        return decrypt(DEFAULT_KEY, DEFAULT_IV, encrypted);
    }

    /**
     * Genera un hash del contenido usando el algoritmo especificado.
     *
     * @param content   Texto a convertir en hash.
     * @param algorithm Nombre del algoritmo (p. ej. "MD5", "SHA-1", "SHA-256").
     * @return Representación hexadecimal del hash, o cadena vacía si el algoritmo no existe.
     */
    public static String hash(String content, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashBytes = digest.digest(content.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Genera un hash MD5 del contenido proporcionado.
     *
     * @param content Texto a convertir en hash.
     * @return Representación hexadecimal del hash MD5 (32 caracteres).
     */
    public static String hashingToMD5(String content) {
        return hash(content, "MD5");
    }

    /**
     * Genera un hash SHA-1 del contenido proporcionado.
     *
     * @param content Texto a convertir en hash.
     * @return Representación hexadecimal del hash SHA-1 (40 caracteres).
     */
    public static String hashingToSHA1(String content) {
        return hash(content, "SHA-1");
    }

    /**
     * Genera un hash SHA-256 del contenido proporcionado.
     *
     * @param content Texto a convertir en hash.
     * @return Representación hexadecimal del hash SHA-256 (64 caracteres).
     */
    public static String hashingToSHA256(String content) {
        return hash(content, "SHA-256");
    }

    /**
     * Genera un hash SHA-384 del contenido proporcionado.
     *
     * @param content Texto a convertir en hash.
     * @return Representación hexadecimal del hash SHA-384 (96 caracteres).
     */
    public static String hashingToSHA384(String content) {
        return hash(content, "SHA-384");
    }

    /**
     * Genera un hash SHA-512 del contenido proporcionado.
     *
     * @param content Texto a convertir en hash.
     * @return Representación hexadecimal del hash SHA-512 (128 caracteres).
     */
    public static String hashingToSHA512(String content) {
        return hash(content, "SHA-512");
    }
}
