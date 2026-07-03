package cloud.techotakus.invoice.common.app.mapstruct;

import cloud.techotakus.invoice.common.api.model.CommonNonceCreateRequestModel;
import cloud.techotakus.invoice.common.api.model.CommonNonceCreateResponseModel;
import cloud.techotakus.invoice.common.api.model.CommonNonceVerifyRequestModel;
import cloud.techotakus.invoice.common.api.model.CommonNonceVerifyResponseModel;
import cloud.techotakus.invoice.common.domain.entity.CommonNonceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommonNonceMapstruct {

    @Mapping(target = "nonce", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "ownerUserId", ignore = true)
    @Mapping(target = "sessionId", ignore = true)
    CommonNonceEntity map(CommonNonceCreateRequestModel model);

    @Mapping(target = "expiresAt", ignore = true)
    CommonNonceEntity map(CommonNonceVerifyRequestModel model);

    CommonNonceCreateResponseModel mapCreate(CommonNonceEntity entity);

    default CommonNonceVerifyResponseModel mapVerify(CommonNonceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CommonNonceVerifyResponseModel(true, entity.getOperation(), entity.getExpiresAt());
    }
}
