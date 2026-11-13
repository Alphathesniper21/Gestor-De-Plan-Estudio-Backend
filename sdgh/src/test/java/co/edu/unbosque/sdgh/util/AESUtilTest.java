package co.edu.unbosque.sdgh.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para la clase utilitaria {@link AESUtil}.
 * <p>
 * Verifica el correcto funcionamiento de las operaciones de cifrado AES en modo
 * GCM con vector de inicializacion (IV) dinamico, descifrado simetrico y
 * generacion de hashes criptograficos (MD5, SHA-1, SHA-256, SHA-384, SHA-512).
 * Garantiza la consistencia del ciclo encrypt -> decrypt y la longitud estandar
 * de cada algoritmo de resumen criptografico.
 * </p>
 *
 * @author Martin Peña
 * @version 1.0
 * @since 1.0
 * @see AESUtil
 */
class AESUtilTest {

	/** Clave de prueba de 16 caracteres para AES-128. */
	private static final String CLAVE_TEST = "1234567890123456";

	/**
	 * Configura la propiedad del sistema con la clave de cifrado requerida por
	 * {@link AESUtil} antes de ejecutar las pruebas.
	 */
	@BeforeAll
	static void setUpAll() {
		System.setProperty("aes.key", CLAVE_TEST);
	}

	/**
	 * Limpia la propiedad del sistema al finalizar la ejecucion de la suite.
	 */
	@AfterAll
	static void tearDownAll() {
		System.clearProperty("aes.key");
	}

	// ─── ENCRYPT / DECRYPT ──────────────────────────────────────────────────

	/**
	 * Verifica que el ciclo completo encrypt -> decrypt recupera fielmente el
	 * texto plano original para un texto comun.
	 */
	@Test
	void testEncryptDecrypt_TextoComun() {
		String original = "MensajeSecretoParaGestionDeHorarios2026";

		String encriptado = AESUtil.encrypt(original);
		String desencriptado = AESUtil.decrypt(encriptado);

		assertNotNull(encriptado);
		assertNotEquals(original, encriptado);
		assertEquals(original, desencriptado);
	}

	/**
	 * Verifica que {@code decrypt} recupera correctamente el correo electronico
	 * cifrado del usuario.
	 */
	@Test
	void testDecrypt_CorreoElectronico() {
		String correo = "admin@unbosque.edu.co";
		String encriptado = AESUtil.encrypt(correo);

		assertEquals(correo, AESUtil.decrypt(encriptado));
	}

	/**
	 * Verifica que el uso de IV aleatorio produce diferentes salidas cifradas para
	 * el mismo texto de entrada, pero ambas descifran al valor original.
	 */
	@Test
	void testEncrypt_DosCifradosProducenDiferenteSalidaPorIV() {
		String texto = "MismoTextoDosVeces";

		String enc1 = AESUtil.encrypt(texto);
		String enc2 = AESUtil.encrypt(texto);

		assertNotEquals(enc1, enc2);
		assertEquals(texto, AESUtil.decrypt(enc1));
		assertEquals(texto, AESUtil.decrypt(enc2));
	}

	/**
	 * Verifica que se lanza {@link IllegalStateException} con mensaje explicativo
	 * cuando se intenta descifrar una cadena invalida o corrupta.
	 */
	@Test
	void testDecrypt_LanzaExcepcionTextoCorrupto() {
		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> AESUtil.decrypt("TextoQueNoEsBase64NiCifradoValido"));

		assertTrue(ex.getMessage().contains("No se pudo descifrar"));
	}

	// ─── HASHING ──────────────────────────────────────────────────────────

	/**
	 * Verifica que {@code hashingToMD5} genera un hash hexadecimal de 32
	 * caracteres.
	 */
	@Test
	void testHashingToMD5_LongitudCorrecta() {
		String hash = AESUtil.hashingToMD5("admin123");

		assertNotNull(hash);
		assertEquals(32, hash.length());
	}

	/**
	 * Verifica que {@code hashingToSHA1} genera un hash hexadecimal de 40
	 * caracteres.
	 */
	@Test
	void testHashingToSHA1_LongitudCorrecta() {
		String hash = AESUtil.hashingToSHA1("admin123");

		assertNotNull(hash);
		assertEquals(40, hash.length());
	}

	/**
	 * Verifica que {@code hashingToSHA256} genera un hash hexadecimal de 64
	 * caracteres.
	 */
	@Test
	void testHashingToSHA256_LongitudCorrecta() {
		String hash = AESUtil.hashingToSHA256("admin123");

		assertNotNull(hash);
		assertEquals(64, hash.length());
	}

	/**
	 * Verifica que {@code hashingToSHA384} genera un hash hexadecimal de 96
	 * caracteres.
	 */
	@Test
	void testHashingToSHA384_LongitudCorrecta() {
		String hash = AESUtil.hashingToSHA384("admin123");

		assertNotNull(hash);
		assertEquals(96, hash.length());
	}

	/**
	 * Verifica que {@code hashingToSHA512} genera un hash hexadecimal de 128
	 * caracteres.
	 */
	@Test
	void testHashingToSHA512_LongitudCorrecta() {
		String hash = AESUtil.hashingToSHA512("admin123");

		assertNotNull(hash);
		assertEquals(128, hash.length());
	}

	/**
	 * Verifica la consistencia deterministica de los algoritmos de hash (la misma
	 * entrada siempre genera exactamente el mismo resumen).
	 */
	@Test
	void testHashing_ConsistenciaDeterminista() {
		String entrada = "clavePruebaSegura";

		assertEquals(AESUtil.hashingToSHA256(entrada), AESUtil.hashingToSHA256(entrada));
		assertEquals(AESUtil.hashingToMD5(entrada), AESUtil.hashingToMD5(entrada));
	}
}
