package cloud.techotakus.invoice.file.infra.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Component
public class AuditHandler implements MetaObjectHandler {

    private static final String USER_ID_MDC_KEY = "userId";
    private static final String TRACE_ID_MDC_KEY = "traceId";
    private static final String SYSTEM_ACTOR = "SYSTEM";
    private static final String UNKNOWN_TRACE = "UNKNOWN_TRACE";

    @Override
    public void insertFill(MetaObject metaObject) {
        OffsetDateTime now = OffsetDateTime.now();
        String userId = currentUserId();
        String traceId = currentTraceId();
        setIfPresent(metaObject, "createdAt", now);
        setIfPresent(metaObject, "createdBy", userId);
        setIfPresent(metaObject, "createdTrace", traceId);
        setIfPresent(metaObject, "updatedAt", now);
        setIfPresent(metaObject, "updatedBy", userId);
        setIfPresent(metaObject, "updatedTrace", traceId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        setIfPresent(metaObject, "updatedAt", OffsetDateTime.now());
        setIfPresent(metaObject, "updatedBy", currentUserId());
        setIfPresent(metaObject, "updatedTrace", currentTraceId());
    }

    private static void setIfPresent(MetaObject metaObject, String fieldName, Object value) {
        if (metaObject.hasSetter(fieldName)) {
            metaObject.setValue(fieldName, value);
        }
    }

    private static String currentUserId() {
        String value = MDC.get(USER_ID_MDC_KEY);
        return StringUtils.hasText(value) ? value : SYSTEM_ACTOR;
    }

    private static String currentTraceId() {
        String value = MDC.get(TRACE_ID_MDC_KEY);
        return StringUtils.hasText(value) ? value : UNKNOWN_TRACE;
    }
}
