package co.edu.unbosque.sdgh.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AESUtilTest {

	private static final String CLAVE_TEST = "1234567890123456"; // 16 bytes para AES-128

	@BeforeAll
	static void setUp() {
		System.setProperty("aes.key", CLAVE_TEST);
	}

	@AfterAll
	static void tearDown() {
		System.clearProperty("aes.key");
	}

	@Test
	@DisplayName("Cifrado y Descifrado: Debe encriptar y desencriptar un texto recuperando el original")
	void debeCifrarYDescifrarTextoCorrectamente() {
		String textoOriginal = "MensajeSecretoParaGestionDeHorarios2026";

		String cifrado = AESUtil.encrypt(textoOriginal);
		assertNotNull(cifrado);
		assertNotEquals(textoOriginal, cifrado);

		String descifrado = AESUtil.decrypt(cifrado);
		assertEquals(textoOriginal, descifrado);
	}

	@Test
	@DisplayName("Cifrado GCM: Dos cifrados del mismo texto deben producir resultados diferentes por el IV aleatorio")
	void dosCifradosDebenGenerarDiferenteTextoPorIV() {
		String texto = "MismoTextoDosVeces";

		String cifrado1 = AESUtil.encrypt(texto);
		String cifrado2 = AESUtil.encrypt(texto);

		assertNotEquals(cifrado1, cifrado2);
		assertEquals(texto, AESUtil.decrypt(cifrado1));
		assertEquals(texto, AESUtil.decrypt(cifrado2));
	}

	@Test
	@DisplayName("Descifrado Invalido: Debe lanzar excepcion cuando el texto cifrado es invalido o corrupto")
	void debeFallarDescifradoConTextoCorrupto() {
		assertThrows(IllegalStateException.class, () -> AESUtil.decrypt("TextoQueNoEsBase64NiCifradoValido"));
	}

	@Test
	@DisplayName("Hashing SHA-256: Debe generar un hash deterministico de 64 caracteres")
	void debeGenerarHashSHA256Correcto() {
		String hash = AESUtil.hashingToSHA256("admin123");
		assertNotNull(hash);
		assertEquals(64, hash.length());
		assertEquals(hash, AESUtil.hashingToSHA256("admin123"));
	}

	@Test
	@DisplayName("Hashing MD5 y SHA-1: Deben generar hashes con longitud esperada")
	void debeGenerarHashesMD5YSHA1() {
		String md5 = AESUtil.hashingToMD5("admin123");
		assertNotNull(md5);
		assertEquals(32, md5.length());

		String sha1 = AESUtil.hashingToSHA1("admin123");
		assertNotNull(sha1);
		assertEquals(40, sha1.length());
	}
}
