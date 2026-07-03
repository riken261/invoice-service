package cloud.techotakus.invoice.integration.infra.provider.ocr;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class OcrTencentProviderTest {

    @Test
    void safeErrorMessageFixesTencentUtf8Mojibake() {
        String message = ReflectionTestUtils.invokeMethod(
                OcrTencentProvider.class,
                "safeErrorMessage",
                "å¼ç¥¨æ¥ææ ¼å¼éè¯¯"
        );

        assertThat(message).isEqualTo("开票日期格式错误");
    }
}
