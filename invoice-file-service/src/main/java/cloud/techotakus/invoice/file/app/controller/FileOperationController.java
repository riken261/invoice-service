package cloud.techotakus.invoice.file.app.controller;

import cloud.techotakus.common.pojo.http.RestRequest;
import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.file.api.model.FileAccessResponseModel;
import cloud.techotakus.invoice.file.api.model.FileUploadRequestModel;
import cloud.techotakus.invoice.file.api.model.FileUploadResponseModel;
import cloud.techotakus.invoice.file.api.surface.FileOperationApi;
import cloud.techotakus.invoice.file.app.mapstruct.FileOperationMapstruct;
import cloud.techotakus.invoice.file.domain.usecase.FileOperationUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "File", description = "File Operation APIs")
public class FileOperationController implements FileOperationApi {

    @Resource
    private FileOperationUseCase useCase;
    @Resource
    private FileOperationMapstruct mapstruct;

    @Override
    public ResponseEntity<RestResponse<FileUploadResponseModel>> upload(RestRequest<FileUploadRequestModel> request) {
        FileUploadRequestModel data = request == null ? null : request.getData();
        return ResponseEntity.ok(RestResponse.success(mapstruct.mapUploadResponse(useCase.upload(mapstruct.map(data)))));
    }

    @Override
    public ResponseEntity<RestResponse<FileAccessResponseModel>> preview(String fileId) {
        return ResponseEntity.ok(RestResponse.success(mapstruct.map(useCase.preview(fileId))));
    }

    @Override
    public ResponseEntity<RestResponse<FileAccessResponseModel>> download(String fileId) {
        return ResponseEntity.ok(RestResponse.success(mapstruct.map(useCase.download(fileId))));
    }

}
