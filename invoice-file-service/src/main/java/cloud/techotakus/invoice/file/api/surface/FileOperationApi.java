package cloud.techotakus.invoice.file.api.surface;

import cloud.techotakus.common.pojo.http.RestRequest;
import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.file.api.model.FileAccessResponseModel;
import cloud.techotakus.invoice.file.api.model.FileUploadRequestModel;
import cloud.techotakus.invoice.file.api.model.FileUploadResponseModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/internal/v1/files")
public interface FileOperationApi {

    @PostMapping("/upload")
    ResponseEntity<RestResponse<FileUploadResponseModel>> upload(@RequestBody RestRequest<FileUploadRequestModel> request);

    @GetMapping("/{fileId}/preview")
    ResponseEntity<RestResponse<FileAccessResponseModel>> preview(@PathVariable("fileId") String fileId);

    @GetMapping("/{fileId}/download")
    ResponseEntity<RestResponse<FileAccessResponseModel>> download(@PathVariable("fileId") String fileId);
}
