package cloud.techotakus.invoice.authorization.app.mapstruct;

import cloud.techotakus.invoice.authorization.api.model.AuthLoginUrlResponseModel;
import cloud.techotakus.invoice.authorization.api.model.AuthLogoutResponseModel;
import cloud.techotakus.invoice.authorization.api.model.AuthMeResponseModel;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationBffSessionEntity;
import cloud.techotakus.invoice.authorization.domain.entity.AuthorizationLoginStateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthBffMapstruct {

    @Mapping(target = "expiresInSeconds", expression = "java(java.time.Duration.between(java.time.Instant.now(), entity.expiresAt()).toSeconds())")
    AuthLoginUrlResponseModel map(AuthorizationLoginStateEntity entity);

    AuthMeResponseModel map(AuthorizationBffSessionEntity entity);

    default AuthLogoutResponseModel map(Boolean success) {
        return new AuthLogoutResponseModel(Boolean.TRUE.equals(success));
    }
}
