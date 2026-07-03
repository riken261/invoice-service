package cloud.techotakus.invoice.authorization.app.controller;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.authorization.api.model.AuthLoginUrlResponseModel;
import cloud.techotakus.invoice.authorization.api.model.AuthLogoutResponseModel;
import cloud.techotakus.invoice.authorization.api.model.AuthMeResponseModel;
import cloud.techotakus.invoice.authorization.api.surface.AuthBffApi;
import cloud.techotakus.invoice.authorization.app.mapstruct.AuthBffMapstruct;
import cloud.techotakus.invoice.authorization.domain.usecase.AuthBffUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Auth", description = "BFF authentication APIs")
public class AuthBffController implements AuthBffApi {

    @Resource
    private AuthBffUseCase useCase;

    @Resource
    private AuthBffMapstruct mapstruct;

    @Override
    public ResponseEntity<RestResponse<AuthLoginUrlResponseModel>> loginUrl(String tenantCode, String redirectUri) {
        return ResponseEntity.ok(RestResponse.success(mapstruct.map(useCase.loginUrl(tenantCode, redirectUri))));
    }

    @Override
    public ResponseEntity<Void> callback(String state, String code) {
        var result = useCase.callback(state, code);
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(result.redirectUrl()))
            .build();
    }

    @Override
    public ResponseEntity<RestResponse<AuthMeResponseModel>> me() {
        return ResponseEntity.ok(RestResponse.success(mapstruct.map(useCase.me())));
    }

    @Override
    public ResponseEntity<RestResponse<AuthLogoutResponseModel>> logout() {
        return ResponseEntity.ok(RestResponse.success(mapstruct.map(useCase.logout())));
    }
}
