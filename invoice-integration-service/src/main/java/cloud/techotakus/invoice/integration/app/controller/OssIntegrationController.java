package cloud.techotakus.invoice.integration.app.controller;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.integration.api.model.OssCopyObjectRequestModel;
import cloud.techotakus.invoice.integration.api.model.OssCopyObjectResponseModel;
import cloud.techotakus.invoice.integration.api.model.OssDeleteObjectResponseModel;
import cloud.techotakus.invoice.integration.api.model.OssGetObjectResponseModel;
import cloud.techotakus.invoice.integration.api.model.OssHealthResponseModel;
import cloud.techotakus.invoice.integration.api.model.OssKeyRequestModel;
import cloud.techotakus.invoice.integration.api.model.OssPresignObjectRequestModel;
import cloud.techotakus.invoice.integration.api.model.OssPresignedUrlResponseModel;
import cloud.techotakus.invoice.integration.api.model.OssPutObjectRequestModel;
import cloud.techotakus.invoice.integration.api.model.OssPutObjectResponseModel;
import cloud.techotakus.invoice.integration.api.model.OssHealthRequestModel;
import cloud.techotakus.invoice.integration.api.surface.OssIntegrationApi;
import cloud.techotakus.invoice.integration.domain.usecase.OssIntegrationUseCase;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OssIntegrationController implements OssIntegrationApi {

    @Resource
    private OssIntegrationUseCase useCase;

    @Override
    public ResponseEntity<RestResponse<OssPutObjectResponseModel>> putObject(OssPutObjectRequestModel command) {
        return ResponseEntity.ok(RestResponse.success(useCase.putObject(command)));
    }

    @Override
    public ResponseEntity<RestResponse<OssGetObjectResponseModel>> getObject(OssKeyRequestModel command) {
        return ResponseEntity.ok(RestResponse.success(useCase.getObject(command)));
    }

    @Override
    public ResponseEntity<RestResponse<OssPresignedUrlResponseModel>> presignGetObject(OssPresignObjectRequestModel command) {
        return ResponseEntity.ok(RestResponse.success(useCase.presignGetObject(command)));
    }

    @Override
    public ResponseEntity<RestResponse<OssGetObjectResponseModel>> getObjectMetadata(OssKeyRequestModel command) {
        return ResponseEntity.ok(RestResponse.success(useCase.getObjectMetadata(command)));
    }

    @Override
    public ResponseEntity<RestResponse<OssGetObjectResponseModel>> objectExists(OssKeyRequestModel command) {
        return ResponseEntity.ok(RestResponse.success(useCase.objectExists(command)));
    }

    @Override
    public ResponseEntity<RestResponse<OssDeleteObjectResponseModel>> deleteObject(OssKeyRequestModel command) {
        return ResponseEntity.ok(RestResponse.success(useCase.deleteObject(command)));
    }

    @Override
    public ResponseEntity<RestResponse<OssCopyObjectResponseModel>> copyObject(OssCopyObjectRequestModel command) {
        return ResponseEntity.ok(RestResponse.success(useCase.copyObject(command)));
    }

    @Override
    public ResponseEntity<RestResponse<OssHealthResponseModel>> healthCheck(OssHealthRequestModel command) {
        return ResponseEntity.ok(RestResponse.success(useCase.healthCheck(command)));
    }
}
