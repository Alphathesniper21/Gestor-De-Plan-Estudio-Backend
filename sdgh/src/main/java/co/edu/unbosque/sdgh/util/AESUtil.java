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
 * Clase de utilidad que proporciona operaciones de cifrado y descifrado
 * mediante el algoritmo AES en modo GCM, además de funciones para generar
 * hashes utilizando diferentes algoritmos criptográficos.
 *
 * La clave de cifrado se obtiene desde la variable de entorno AES_KEY o desde
 * la propiedad del sistema aes.key. Debe tener una longitud de 16, 24 o 32
 * bytes. Cada operación de cifrado genera un vector de inicialización (IV)
 * aleatorio de 12 bytes, que se almacena junto con el contenido cifrado.
 *
 * También permite generar hashes mediante los algoritmos MD5, SHA-1, SHA-256,
 * SHA-384 y SHA-512. Utiliza únicamente las APIs nativas de Java.
 *
 * @author Sebastian Toyo
 */
public class AESUtil {

	/**
	 * Nombre del algoritmo de cifrado simétrico utilizado.
	 */
	private static final String ALGORITMO = "AES";

	/**
	 * Transformación de cifrado que especifica el algoritmo AES, el modo GCM
	 * y la ausencia de padding.
	 */
	private static final String TIPO_CIFRADO = "AES/GCM/NoPadding";

	/**
	 * Longitud del código de autenticación GCM expresada en bits.
	 */
	private static final int GCM_TAG_LENGTH = 128;

	/**
	 * Longitud del vector de inicialización (IV) expresada en bytes.
	 */
	private static final int IV_LENGTH = 12;

	/**
	 * Generador de números aleatorios seguro utilizado para crear los vectores
	 * de inicialización durante las operaciones de cifrado.
	 */
	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * Obtiene la clave de cifrado desde la variable de entorno AES_KEY. Si esta
	 * no está configurada, intenta obtenerla desde la propiedad del sistema
	 * aes.key.
	 *
	 * @return Clave de cifrado configurada.
	 * @throws IllegalStateException si la clave no está configurada.
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
	 * Cifra un texto utilizando el algoritmo AES en modo GCM y la clave
	 * configurada. Genera un vector de inicialización aleatorio y lo concatena
	 * con el contenido cifrado antes de codificar el resultado en Base64.
	 *
	 * @param texto Texto plano que se desea cifrar.
	 * @return Cadena codificada en Base64 que contiene el IV y el texto cifrado.
	 * @throws IllegalStateException si ocurre un error durante el cifrado o
	 *         la clave configurada no es válida.
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
	 * Descifra un texto previamente generado mediante el método
	 * {@link #encrypt(String)}. Recupera el vector de inicialización y utiliza
	 * la clave configurada para obtener el contenido original.
	 *
	 * @param encrypted Cadena codificada en Base64 que contiene el IV y el
	 *                  contenido cifrado.
	 * @return Texto plano obtenido después del descifrado.
	 * @throws IllegalStateException si el contenido cifrado es inválido,
	 *         fue alterado, no corresponde a la clave configurada o ocurre
	 *         un error durante el descifrado.
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
	 * Genera un hash del contenido recibido utilizando el algoritmo indicado.
	 * El resultado se representa mediante una cadena hexadecimal.
	 *
	 * @param content Texto que se desea convertir en hash.
	 * @param algorithm Nombre del algoritmo de hash, por ejemplo MD5, SHA-1
	 *                  o SHA-256.
	 * @return Representación hexadecimal del hash generado, o una cadena
	 *         vacía si el algoritmo especificado no está disponible.
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
	 * Genera un hash del contenido utilizando el algoritmo MD5.
	 *
	 * @param content Texto que se desea convertir en hash.
	 * @return Hash MD5 representado en formato hexadecimal.
	 */
	public static String hashingToMD5(String content) {
		return hash(content, "MD5");
	}

	/**
	 * Genera un hash del contenido utilizando el algoritmo SHA-1.
	 *
	 * @param content Texto que se desea convertir en hash.
	 * @return Hash SHA-1 representado en formato hexadecimal.
	 */
	public static String hashingToSHA1(String content) {
		return hash(content, "SHA-1");
	}

	/**
	 * Genera un hash del contenido utilizando el algoritmo SHA-256.
	 *
	 * @param content Texto que se desea convertir en hash.
	 * @return Hash SHA-256 representado en formato hexadecimal.
	 */
	public static String hashingToSHA256(String content) {
		return hash(content, "SHA-256");
	}

	/**
	 * Genera un hash del contenido utilizando el algoritmo SHA-384.
	 *
	 * @param content Texto que se desea convertir en hash.
	 * @return Hash SHA-384 representado en formato hexadecimal.
	 */
	public static String hashingToSHA384(String content) {
		return hash(content, "SHA-384");
	}

	/**
	 * Genera un hash del contenido utilizando el algoritmo SHA-512.
	 *
	 * @param content Texto que se desea convertir en hash.
	 * @return Hash SHA-512 representado en formato hexadecimal.
	 */
	public static String hashingToSHA512(String content) {
		return hash(content, "SHA-512");
	}
}