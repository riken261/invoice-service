package cloud.techotakus.invoice.common.api.surface;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.common.api.model.CommonNonceCreateRequestModel;
import cloud.techotakus.invoice.common.api.model.CommonNonceCreateResponseModel;
import cloud.techotakus.invoice.common.api.model.CommonNonceVerifyRequestModel;
import cloud.techotakus.invoice.common.api.model.CommonNonceVerifyResponseModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping
public interface CommonNonceApi {

    @PostMapping("/bff/v1/nonces")
    ResponseEntity<RestResponse<CommonNonceCreateResponseModel>> createNonce(
            @Valid @RequestBody CommonNonceCreateRequestModel request,
            HttpServletRequest httpRequest
    );

    @PostMapping("/internal/v1/nonces/verify-consume")
    ResponseEntity<RestResponse<CommonNonceVerifyResponseModel>> verifyAndConsume(
            @Valid @RequestBody CommonNonceVerifyRequestModel request
    );
}
