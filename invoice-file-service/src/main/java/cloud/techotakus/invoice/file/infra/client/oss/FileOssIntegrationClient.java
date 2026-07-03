package cloud.techotakus.invoice.file.infra.client.oss;

import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.file.infra.client.oss.model.FileOssPresignObjectRequest;
import cloud.techotakus.invoice.file.infra.client.oss.model.FileOssPresignedUrlResponse;
import cloud.techotakus.invoice.file.infra.client.oss.model.FileOssPutObjectRequest;
import cloud.techotakus.invoice.file.infra.client.oss.model.FileOssPutObjectResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "invoice-integration-service",
        url = "${invoice.file.integration.base-url:http://localhost:8086}"
)
public interface FileOssIntegrationClient {

    @PostMapping("/internal/v1/integrations/storage/objects")
    RestResponse<FileOssPutObjectResponse> putObject(@RequestBody FileOssPutObjectRequest request);

    @PostMapping("/internal/v1/integrations/storage/objects/presign-get")
    RestResponse<FileOssPresignedUrlResponse> presignGetObject(@RequestBody FileOssPresignObjectRequest request);
}
