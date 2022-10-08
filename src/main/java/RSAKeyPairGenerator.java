import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Objects;

public class RSAKeyPairGenerator implements Runnable {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final String KEY_ALGORITHM = "RSA";
    private static final String KEY_PUB_FILENAME = "id_rsa.pub";
    private static final String KEY_PRI_FILENAME = "id_rsa";

    private static final String KEY_PRI_HEADER = "-----BEGIN ENCRYPTED PRIVATE KEY-----";
    private static final String KEY_PRI_FOOTER = "-----END ENCRYPTED PRIVATE KEY-----";
    private static final String KEY_PUB_PREFIX = "ssh-rsa";

    private static final String PASSPHRASE_ALGORITHM = "PBEWithSHA1AndDESede";
    private static final String PASSPHRASE_SALT = "SGVsbG8sIFdvcmxkIQ==";
    private static final int PASSPHRASE_ITERATION = 20;

    private final byte[] seed;
    private final char[] passphrase;
    private final int keySize;
    private final boolean isVerbose;

    public RSAKeyPairGenerator(RSAKeyPairParams params) {
        Objects.requireNonNull(params);
        this.seed = params.seed().getBytes(CHARSET);
        this.passphrase = params.passphrase().toCharArray();
        this.keySize = params.keySize();
        this.isVerbose = params.isVerbose();
    }

    @Override
    public void run() {
        try {
            // Init random object with seed
            var random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(seed);

            if (isVerbose)
                System.out.println("Source of randomness initialized with seed");

            // Generate original key-pair with seeded random object
            var generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            generator.initialize(keySize, random);
            var keyPair = generator.generateKeyPair();

            if (isVerbose)
                System.out.println("Original %s key pair generated".formatted(KEY_ALGORITHM));

            // Extract RSA key-pair
            var privateKey = (RSAPrivateKey) keyPair.getPrivate();
            var publicKey = (RSAPublicKey) keyPair.getPublic();

            // Generate required params & keys for PBE
            var salt = PASSPHRASE_SALT.getBytes(CHARSET);
            var paramSpec = new PBEParameterSpec(salt, PASSPHRASE_ITERATION);
            var keySpec = new PBEKeySpec(passphrase);
            var keyFactory = SecretKeyFactory.getInstance(PASSPHRASE_ALGORITHM);
            var key = keyFactory.generateSecret(keySpec);

            if (isVerbose)
                System.out.println("Prepared for password based encryption of private key");

            // Encrypting the private key
            var cipher = Cipher.getInstance(PASSPHRASE_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            var cipherText = cipher.doFinal(privateKey.getEncoded());

            if (isVerbose)
                System.out.println("Private key encrypted");

            // Generate encrypted private key
            var algorithmParameters = AlgorithmParameters.getInstance(PASSPHRASE_ALGORITHM);
            algorithmParameters.init(paramSpec);
            var encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(algorithmParameters, cipherText);
            var encryptedPrivateKeyEncoded = encryptedPrivateKeyInfo.getEncoded();

            if (isVerbose)
                System.out.println("Private key formatted");

            // Write keys to file system
            writePrivateKeyDataToFile(encryptedPrivateKeyEncoded);
            writePublicKeyDataToFile(
                    publicKey.getPublicExponent().toByteArray(),
                    publicKey.getModulus().toByteArray(),
                    System.getProperty("user.name"));

            if (isVerbose)
                System.out.println("Key-pair written to file system");

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void writePrivateKeyDataToFile(byte[] privateKeyBytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(KEY_PRI_FILENAME)) {
            var privateKeyEncoded = Base64.getMimeEncoder().encodeToString(privateKeyBytes);
            fos.write(KEY_PRI_HEADER.getBytes(CHARSET));
            fos.write(System.lineSeparator().getBytes(CHARSET));
            fos.write(privateKeyEncoded.getBytes(CHARSET));
            fos.write(System.lineSeparator().getBytes(CHARSET));
            fos.write(KEY_PRI_FOOTER.getBytes(CHARSET));
        }
    }

    private void writePublicKeyDataToFile(byte[] publicExponentBytes, byte[] modulusBytes, String username) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(KEY_PUB_FILENAME)) {
            var publicKeyBytes = new ByteArrayOutputStream();
            var dos = new DataOutputStream(publicKeyBytes);
            dos.writeInt(KEY_PUB_PREFIX.getBytes().length);
            dos.write(KEY_PUB_PREFIX.getBytes());
            dos.writeInt(publicExponentBytes.length);
            dos.write(publicExponentBytes);
            dos.writeInt(modulusBytes.length);
            dos.write(modulusBytes);

            var publicKeyEncoded = Base64.getEncoder().encode(publicKeyBytes.toByteArray());
            fos.write(KEY_PUB_PREFIX.getBytes(CHARSET));
            fos.write(" ".getBytes(CHARSET));
            fos.write(publicKeyEncoded);
            fos.write(" ".getBytes(CHARSET));
            fos.write(username.getBytes(CHARSET));
        }
    }
}
