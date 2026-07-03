package cloud.techotakus.invoice.integration.domain.repository;

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

public interface OssIntegrationRepository {

    String providerCode();

    String storageType();

    OssPutObjectResponseModel putObject(OssPutObjectRequestModel command);

    OssGetObjectResponseModel getObject(OssKeyRequestModel command);

    OssPresignedUrlResponseModel presignGetObject(OssPresignObjectRequestModel command);

    OssGetObjectResponseModel getObjectMetadata(OssKeyRequestModel command);

    OssGetObjectResponseModel objectExists(OssKeyRequestModel command);

    OssDeleteObjectResponseModel deleteObject(OssKeyRequestModel command);

    OssCopyObjectResponseModel copyObject(OssCopyObjectRequestModel command);

    OssHealthResponseModel healthCheck(OssHealthRequestModel command);
}
