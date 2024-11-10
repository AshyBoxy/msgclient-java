package xyz.ashyboxy.msgclient;

import com.google.gson.Gson;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Utils {
    // for sha256
    public static final int DIGEST_LENGTH = 32;
    public static final String ALGORITHM = "HmacSHA256";
    private static Gson gson = new Gson();

    public static byte[] getDigest(ByteBuffer data, String key) {
        int position = data.position();

        SecretKeySpec spec = new SecretKeySpec(key.getBytes(), ALGORITHM);

        Mac mac;
        try {
            mac = Mac.getInstance(ALGORITHM);
            mac.init(spec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        byte[] d = new byte[data.remaining()];
        data.get(d);

        data.position(position);
        return mac.doFinal(d);
    }

    public static boolean verifyMessage(ByteBuffer data, String key) {
        int position = data.position();

        if (data.remaining() < DIGEST_LENGTH + 1) return false;

        byte[] digest = new byte[DIGEST_LENGTH];
        data.get(digest);

        boolean correct = Arrays.equals(digest, getDigest(data, key));

        data.position(position);
        return correct;
    }

    public static Message getMessage(ByteBuffer data) {
        int position = data.position();

        data.position(position + DIGEST_LENGTH);
        String message = StandardCharsets.UTF_8.decode(data).toString();

        data.position(position);
        return gson.fromJson(message, Message.class);
    }

    public static ByteBuffer encodeMessage(Message message, String key) {
        ByteBuffer msg = StandardCharsets.UTF_8.encode(gson.toJson(message));
        byte[] digest = getDigest(msg, key);

        ByteBuffer buf = ByteBuffer.allocate(DIGEST_LENGTH + msg.remaining());
        buf.put(digest);
        buf.put(msg);

        buf.rewind();
        return buf;
    }
}
