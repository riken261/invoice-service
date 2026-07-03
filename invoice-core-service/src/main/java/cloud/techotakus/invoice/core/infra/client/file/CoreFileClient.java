package cloud.techotakus.invoice.core.infra.client.file;

import cloud.techotakus.common.pojo.http.RestRequest;
import cloud.techotakus.common.pojo.http.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "invoice-file-service",
        url = "${invoice.core.file-service.base-url:http://localhost:8081}"
)
public interface CoreFileClient {

    @PostMapping("/internal/v1/files/upload")
    RestResponse<CoreFileUploadResponse> upload(@RequestBody RestRequest<CoreFileUploadRequest> request);

    @GetMapping("/internal/v1/files/{fileId}/preview")
    RestResponse<CoreFileAccessResponse> preview(@PathVariable String fileId);

    @GetMapping("/internal/v1/files/{fileId}/download")
    RestResponse<CoreFileAccessResponse> download(@PathVariable String fileId);
}
