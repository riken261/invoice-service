package cloud.techotakus.invoice.integration.api.surface;

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
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/internal/v1/integrations/storage")
public interface OssIntegrationApi {

    @PostMapping("/objects")
    ResponseEntity<RestResponse<OssPutObjectResponseModel>> putObject(
            @Valid @RequestBody OssPutObjectRequestModel command
    );

    @PostMapping("/objects/get")
    ResponseEntity<RestResponse<OssGetObjectResponseModel>> getObject(
            @Valid @RequestBody OssKeyRequestModel command
    );

    @PostMapping("/objects/presign-get")
    ResponseEntity<RestResponse<OssPresignedUrlResponseModel>> presignGetObject(
            @Valid @RequestBody OssPresignObjectRequestModel command
    );

    @PostMapping("/objects/metadata")
    ResponseEntity<RestResponse<OssGetObjectResponseModel>> getObjectMetadata(
            @Valid @RequestBody OssKeyRequestModel command
    );

    @PostMapping("/objects/exists")
    ResponseEntity<RestResponse<OssGetObjectResponseModel>> objectExists(
            @Valid @RequestBody OssKeyRequestModel command
    );

    @PostMapping("/objects/delete")
    ResponseEntity<RestResponse<OssDeleteObjectResponseModel>> deleteObject(
            @Valid @RequestBody OssKeyRequestModel command
    );

    @PostMapping("/objects/copy")
    ResponseEntity<RestResponse<OssCopyObjectResponseModel>> copyObject(
            @Valid @RequestBody OssCopyObjectRequestModel command
    );

    @PostMapping("/health-check")
    ResponseEntity<RestResponse<OssHealthResponseModel>> healthCheck(
            @Valid @RequestBody OssHealthRequestModel command
    );
}
