package co.edu.unbosque.sdgh.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Clase de utilidad para operaciones de cifrado AES y funciones de hash.
 *
 * Cifra y descifra texto usando AES en modo GCM. La clave no esta en el
 * codigo: se lee de la variable de entorno AES_KEY (o de la propiedad del
 * sistema aes.key) y debe tener exactamente 16, 24 o 32 caracteres. Cada
 * cifrado genera un IV aleatorio de 12 bytes que se antepone al texto
 * cifrado, por lo que el resultado cambia en cada llamada.
 *
 * Tambien genera hashes (MD5, SHA-1, SHA-256, SHA-384, SHA-512).
 * Utiliza unicamente APIs nativas de Java, sin dependencias externas.
 */
public class AESUtil {

	/** Algoritmo de cifrado simetrico utilizado. */
	private static final String ALGORITMO = "AES";

	/** Transformacion completa: algoritmo / modo / padding. */
	private static final String TIPO_CIFRADO = "AES/GCM/NoPadding";

	/** Longitud del tag de autenticacion GCM en bits. */
	private static final int GCM_TAG_LENGTH = 128;

	/** Longitud del IV en bytes (recomendado para GCM). */
	private static final int IV_LENGTH = 12;

	/** Generador de numeros aleatorios seguro para los IV. */
	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * Obtiene la clave desde la variable de entorno AES_KEY o, si no existe,
	 * desde la propiedad del sistema aes.key.
	 *
	 * @return Clave de cifrado.
	 * @throws IllegalStateException si la clave no esta configurada.
	 */
	private static String obtenerLlave() {
		String llave = System.getenv("AES_KEY");
		if (llave == null || llave.isBlank()) {
			llave = System.getProperty("aes.key");
		}
		if (llave == null || llave.isBlank()) {
			throw new IllegalStateException("Falta la clave AES: defina la variable de entorno AES_KEY");
		}
		return llave;
	}

	/**
	 * Cifra un texto con AES/GCM usando la clave configurada.
	 *
	 * @param texto Texto plano a cifrar.
	 * @return Base64 de (IV + texto cifrado).
	 * @throws IllegalStateException si falla el cifrado o la clave es invalida.
	 */
	public static String encrypt(String texto) {
		try {
			byte[] iv = new byte[IV_LENGTH];
			RANDOM.nextBytes(iv);

			SecretKeySpec key = new SecretKeySpec(obtenerLlave().getBytes(StandardCharsets.UTF_8), ALGORITMO);
			Cipher cipher = Cipher.getInstance(TIPO_CIFRADO);
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
			byte[] cifrado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));

			byte[] resultado = new byte[iv.length + cifrado.length];
			System.arraycopy(iv, 0, resultado, 0, iv.length);
			System.arraycopy(cifrado, 0, resultado, iv.length, cifrado.length);
			return Base64.getEncoder().encodeToString(resultado);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("No se pudo cifrar el texto: " + e.getMessage(), e);
		}
	}

	/**
	 * Descifra un texto generado por {@link #encrypt(String)}.
	 *
	 * @param encrypted Base64 de (IV + texto cifrado).
	 * @return Texto plano descifrado.
	 * @throws IllegalStateException si el texto fue alterado, no corresponde a
	 *         la clave o la clave es invalida.
	 */
	public static String decrypt(String encrypted) {
		try {
			byte[] datos = Base64.getDecoder().decode(encrypted);
			if (datos.length <= IV_LENGTH) {
				throw new IllegalStateException("El texto cifrado es invalido");
			}

			byte[] iv = new byte[IV_LENGTH];
			byte[] cifrado = new byte[datos.length - IV_LENGTH];
			System.arraycopy(datos, 0, iv, 0, IV_LENGTH);
			System.arraycopy(datos, IV_LENGTH, cifrado, 0, cifrado.length);

			SecretKeySpec key = new SecretKeySpec(obtenerLlave().getBytes(StandardCharsets.UTF_8), ALGORITMO);
			Cipher cipher = Cipher.getInstance(TIPO_CIFRADO);
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
			return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
		} catch (GeneralSecurityException | IllegalArgumentException e) {
			throw new IllegalStateException("No se pudo descifrar el texto: " + e.getMessage(), e);
		}
	}

	/**
	 * Genera un hash del contenido usando el algoritmo especificado.
	 *
	 * @param content Texto a convertir en hash.
	 * @param algorithm Nombre del algoritmo (p. ej. "MD5", "SHA-1", "SHA-256").
	 * @return Representacion hexadecimal del hash, o cadena vacia si el
	 *         algoritmo no existe.
	 */
	public static String hash(String content, String algorithm) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashBytes);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Genera un hash MD5 del contenido (32 caracteres).
	 * @param content Texto a convertir en hash.
	 * @return Hash en hexadecimal.
	 */
	public static String hashingToMD5(String content) {
		return hash(content, "MD5");
	}

	/**
	 * Genera un hash SHA-1 del contenido (40 caracteres).
	 * @param content Texto a convertir en hash.
	 * @return Hash en hexadecimal.
	 */
	public static String hashingToSHA1(String content) {
		return hash(content, "SHA-1");
	}

	/**
	 * Genera un hash SHA-256 del contenido (64 caracteres).
	 * @param content Texto a convertir en hash.
	 * @return Hash en hexadecimal.
	 */
	public static String hashingToSHA256(String content) {
		return hash(content, "SHA-256");
	}

	/**
	 * Genera un hash SHA-384 del contenido (96 caracteres).
	 * @param content Texto a convertir en hash.
	 * @return Hash en hexadecimal.
	 */
	public static String hashingToSHA384(String content) {
		return hash(content, "SHA-384");
	}

	/**
	 * Genera un hash SHA-512 del contenido (128 caracteres).
	 * @param content Texto a convertir en hash.
	 * @return Hash en hexadecimal.
	 */
	public static String hashingToSHA512(String content) {
		return hash(content, "SHA-512");
	}
}