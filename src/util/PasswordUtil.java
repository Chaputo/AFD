package util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de contraseñas con PBKDF2-HmacSHA256 y salt aleatorio.
 * Formato guardado en usuarios.password_hash: pbkdf2_sha256$iteraciones$salt$hash (Base64)
 */
public class PasswordUtil {

    private static final String PREFIJO = "pbkdf2_sha256";
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 600_000;
    private static final int LARGO_SALT = 16;
    private static final int LARGO_HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String hashear(String password) {
        byte[] salt = new byte[LARGO_SALT];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIJO + "$" + ITERACIONES + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(hash);
    }

    public static boolean verificar(String password, String guardado) {
        if (password == null || guardado == null) return false;

        // Contraseñas viejas guardadas en texto plano
        if (!esHash(guardado)) {
            return MessageDigest.isEqual(
                password.getBytes(StandardCharsets.UTF_8),
                guardado.getBytes(StandardCharsets.UTF_8));
        }

        String[] partes = guardado.split("\\$");
        if (partes.length != 4) return false;
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] calculado = pbkdf2(password.toCharArray(), salt, iteraciones);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean esHash(String guardado) {
        return guardado != null && guardado.startsWith(PREFIJO + "$");
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iteraciones, LARGO_HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        } finally {
            spec.clearPassword();
        }
    }
}
