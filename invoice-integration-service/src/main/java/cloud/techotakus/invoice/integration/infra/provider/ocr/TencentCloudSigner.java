package cloud.techotakus.invoice.integration.infra.provider.ocr;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class TencentCloudSigner {

    private static final String ALGORITHM = "TC3-HMAC-SHA256";
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public SignedTencentRequest sign(
            String action,
            String payload,
            Instant timestamp,
            TencentOcrOptions options
    ) {
        String date = DATE_FORMATTER.format(timestamp.atOffset(ZoneOffset.UTC));
        String hashedPayload = sha256Hex(payload);
        String canonicalHeaders = "content-type:application/json; charset=utf-8\n"
                + "host:" + options.host() + "\n";
        String signedHeaders = "content-type;host";
        String canonicalRequest = "POST\n"
                + "/\n"
                + "\n"
                + canonicalHeaders
                + "\n"
                + signedHeaders
                + "\n"
                + hashedPayload;
        String credentialScope = date + "/" + options.service() + "/tc3_request";
        String stringToSign = ALGORITHM + "\n"
                + timestamp.getEpochSecond() + "\n"
                + credentialScope + "\n"
                + sha256Hex(canonicalRequest);

        byte[] secretDate = hmac256(("TC3" + options.secretKey()).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmac256(secretDate, options.service());
        byte[] secretSigning = hmac256(secretService, "tc3_request");
        String signature = HexFormat.of().formatHex(hmac256(secretSigning, stringToSign));
        String authorization = ALGORITHM
                + " Credential=" + options.secretId() + "/" + credentialScope
                + ", SignedHeaders=" + signedHeaders
                + ", Signature=" + signature;

        return new SignedTencentRequest(
                authorization,
                action,
                options.version(),
                timestamp.getEpochSecond(),
                options.region(),
                options.language(),
                options.token()
        );
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to calculate SHA-256 digest", ex);
        }
    }

    private static byte[] hmac256(byte[] key, String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(key, HMAC_SHA256));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to calculate HMAC-SHA256 signature", ex);
        }
    }

    public record TencentOcrOptions(
            String endpoint,
            String host,
            String service,
            String version,
            String region,
            String language,
            String secretId,
            String secretKey,
            String token
    ) {
    }

    public record SignedTencentRequest(
            String authorization,
            String action,
            String version,
            long timestamp,
            String region,
            String language,
            String token
    ) {
    }
}
