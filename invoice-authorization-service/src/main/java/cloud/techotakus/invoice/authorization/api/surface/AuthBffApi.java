package cloud.techotakus.invoice.authorization.api.surface;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.authorization.api.model.AuthLoginUrlResponseModel;
import cloud.techotakus.invoice.authorization.api.model.AuthLogoutResponseModel;
import cloud.techotakus.invoice.authorization.api.model.AuthMeResponseModel;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/bff/v1/auth")
public interface AuthBffApi {

    @GetMapping("/login-url")
    ResponseEntity<RestResponse<AuthLoginUrlResponseModel>> loginUrl(
        @NotBlank @RequestParam String tenantCode,
        @RequestParam(required = false) String redirectUri
    );

    @GetMapping("/callback")
    ResponseEntity<Void> callback(
        @NotBlank @RequestParam String state,
        @NotBlank @RequestParam String code
    );

    @GetMapping("/me")
    ResponseEntity<RestResponse<AuthMeResponseModel>> me();

    @PostMapping("/logout")
    ResponseEntity<RestResponse<AuthLogoutResponseModel>> logout();
}
