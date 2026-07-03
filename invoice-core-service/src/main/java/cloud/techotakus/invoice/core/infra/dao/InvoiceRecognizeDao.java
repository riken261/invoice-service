package cloud.techotakus.invoice.core.infra.dao;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.common.pojo.http.RestRequest;
import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.invoice.core.domain.entity.InvoiceEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceFileAccessEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeSessionEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeUploadFileEntity;
import cloud.techotakus.invoice.core.domain.repository.InvoiceRecognizeRepository;
import cloud.techotakus.invoice.core.infra.client.file.CoreFileAccessResponse;
import cloud.techotakus.invoice.core.infra.client.file.CoreFileClient;
import cloud.techotakus.invoice.core.infra.client.file.CoreFileUploadRequest;
import cloud.techotakus.invoice.core.infra.client.file.CoreFileUploadResponse;
import cloud.techotakus.invoice.core.infra.client.ocr.CoreOcrClient;
import cloud.techotakus.invoice.core.infra.client.ocr.CoreOcrRecognizeRequest;
import cloud.techotakus.invoice.core.infra.client.ocr.CoreOcrRecognizeResponse;
import cloud.techotakus.invoice.core.infra.dto.InvoiceDto;
import cloud.techotakus.invoice.core.infra.mapper.InvoiceMapper;
import cloud.techotakus.invoice.core.infra.dto.InvoiceRecognizeSessionDto;
import cloud.techotakus.invoice.core.infra.mapper.InvoiceRecognizeSessionMapper;
import cloud.techotakus.invoice.core.infra.mapstruct.InvoiceInfraMapstruct;
import cloud.techotakus.invoice.core.infra.mapstruct.InvoiceRecognizeInfraMapstruct;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.List;

@Repository
public class InvoiceRecognizeDao implements InvoiceRecognizeRepository {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    @Resource
    private InvoiceRecognizeSessionMapper mapper;

    @Resource
    private InvoiceMapper invoiceMapper;

    @Resource
    private InvoiceRecognizeInfraMapstruct mapstruct;

    @Resource
    private InvoiceInfraMapstruct invoiceMapstruct;

    @Resource
    private CoreFileClient fileClient;

    @Resource
    private CoreOcrClient ocrClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void saveSession(InvoiceRecognizeSessionEntity session) {
        mapper.insert(mapstruct.map(session));
    }

    @Override
    public void updateSession(InvoiceRecognizeSessionEntity session) {
        mapper.updateById(mapstruct.map(session));
    }

    @Override
    public InvoiceRecognizeSessionEntity findSession(String sessionId) {
        InvoiceRecognizeSessionDto dto = mapper.selectById(sessionId);
        return mapstruct.map(dto);
    }

    @Override
    public List<InvoiceRecognizeSessionEntity> findSessionsByBatchId(String batchId) {
        return mapper.selectList(new LambdaQueryWrapper<InvoiceRecognizeSessionDto>()
                        .eq(InvoiceRecognizeSessionDto::getBatchId, batchId)
                        .eq(InvoiceRecognizeSessionDto::getDeleted, false)
                        .orderByAsc(InvoiceRecognizeSessionDto::getCreatedAt))
                .stream()
                .map(mapstruct::map)
                .toList();
    }

    @Override
    public InvoiceEntity findInvoice(String invoiceId) {
        return invoiceMapstruct.map(invoiceMapper.selectById(invoiceId));
    }

    @Override
    public InvoiceEntity findInvoiceByFileId(String fileId) {
        return invoiceMapstruct.map(invoiceMapper.selectOne(new LambdaQueryWrapper<InvoiceDto>()
                .eq(InvoiceDto::getInvoiceFileId, fileId)
                .last("LIMIT 1")));
    }

    @Override
    public void saveInvoice(InvoiceEntity invoice) {
        invoiceMapper.insert(invoiceMapstruct.map(invoice));
    }

    @Override
    public void updateInvoice(InvoiceEntity invoice) {
        invoiceMapper.updateById(invoiceMapstruct.map(invoice));
    }

    @Override
    public String uploadFile(InvoiceRecognizeSessionEntity session, InvoiceRecognizeUploadFileEntity file) {
        RestRequest<CoreFileUploadRequest> request = new RestRequest<>();
        request.setData(new CoreFileUploadRequest(
                session.getTenantId(),
                session.getOwnerUserId(),
                file.getFileName(),
                file.getMimeType(),
                file.getSize(),
                file.getSha256(),
                file.getObjectKey(),
                file.getContentBase64(),
                session.getIdempotencyKey()
        ));
        CoreFileUploadResponse response = unwrap(fileClient.upload(request), "File upload failed", ErrorCode.FILE_STORAGE_FAILED);
        if (response == null || !StringUtils.hasText(response.id())) {
            throw new ServiceException("File upload did not return file id", ErrorCode.FILE_STORAGE_FAILED);
        }
        return response.id();
    }

    @Override
    public InvoiceFileAccessEntity previewFile(String fileId) {
        return mapAccess(unwrap(fileClient.preview(fileId), "File preview failed", ErrorCode.FILE_STORAGE_FAILED));
    }

    @Override
    public InvoiceRecognizeSessionEntity recognize(
            InvoiceRecognizeSessionEntity session,
            InvoiceRecognizeUploadFileEntity file
    ) {
        CoreOcrRecognizeResponse response = unwrap(ocrClient.recognize(new CoreOcrRecognizeRequest(
                null,
                session.getTenantId(),
                null,
                session.getId(),
                session.getIdempotencyKey(),
                null,
                session.getInvoiceFileId(),
                file.getFileName(),
                file.getMimeType(),
                file.getObjectKey(),
                file.getContentBase64(),
                null,
                file.getInvoiceTypeHint(),
                true,
                null,
                true,
                false,
                true,
                true,
                true
        )), "OCR recognize failed", ErrorCode.OCR_PROVIDER_FAILED);
        session.setProvider(response.providerCode());
        session.setRawRequestId(response.providerRequestId());
        session.setRawResult(response.rawResult());
        session.setNormalizedResult(objectMapper.convertValue(response, MAP_TYPE));
        session.setErrorCode(response.errorCode());
        session.setErrorMessage(response.errorMessage());
        return session;
    }

    private static InvoiceFileAccessEntity mapAccess(CoreFileAccessResponse response) {
        InvoiceFileAccessEntity entity = new InvoiceFileAccessEntity();
        entity.setFileId(response.fileId());
        entity.setUrl(response.url());
        entity.setMethod(response.method());
        entity.setExpiresInSeconds(response.expiresInSeconds());
        entity.setExpiresAt(response.expiresAt());
        entity.setFilename(response.filename());
        entity.setContentType(response.contentType());
        entity.setAccessType(response.accessType());
        return entity;
    }

    private static <T> T unwrap(RestResponse<T> response, String defaultMessage, ErrorCode errorCode) {
        if (response != null && response.isSuccess()) {
            return response.getData();
        }
        String message = response == null || !StringUtils.hasText(response.getError())
                ? defaultMessage
                : response.getError();
        throw new ServiceException(message, errorCode);
    }
}
