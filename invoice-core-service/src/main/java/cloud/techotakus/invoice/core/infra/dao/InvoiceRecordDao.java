package cloud.techotakus.invoice.core.infra.dao;

import cloud.techotakus.common.pojo.enums.ErrorCode;
import cloud.techotakus.common.pojo.exception.ServiceException;
import cloud.techotakus.common.pojo.http.RestResponse;
import cloud.techotakus.common.pojo.page.PageResponse;
import cloud.techotakus.invoice.core.domain.entity.InvoiceEntity;
import cloud.techotakus.invoice.core.domain.entity.InvoiceFileAccessEntity;
import cloud.techotakus.invoice.core.domain.repository.InvoiceRecordRepository;
import cloud.techotakus.invoice.core.infra.client.file.CoreFileAccessResponse;
import cloud.techotakus.invoice.core.infra.client.file.CoreFileClient;
import cloud.techotakus.invoice.core.infra.dto.InvoiceDto;
import cloud.techotakus.invoice.core.infra.mapper.InvoiceMapper;
import cloud.techotakus.invoice.core.infra.mapstruct.InvoiceInfraMapstruct;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class InvoiceRecordDao implements InvoiceRecordRepository {

    @Resource
    private InvoiceMapper invoiceMapper;

    @Resource
    private InvoiceInfraMapstruct invoiceMapstruct;

    @Resource
    private CoreFileClient fileClient;

    @Override
    public PageResponse<InvoiceEntity> list(String tenantId, String userId, int page, int pageSize) {
        IPage<InvoiceDto> result = invoiceMapper.selectPage(
                Page.of(page, pageSize),
                new LambdaQueryWrapper<InvoiceDto>()
                        .eq(InvoiceDto::getTenantId, tenantId)
                        .eq(InvoiceDto::getOwnerUserId, userId)
                        .eq(InvoiceDto::getDeleted, false)
                        .orderByDesc(InvoiceDto::getCreatedAt)
        );
        List<InvoiceEntity> items = result.getRecords().stream()
                .map(invoiceMapstruct::map)
                .toList();
        return PageResponse.success(items, page, pageSize, result.getTotal());
    }

    @Override
    public InvoiceEntity findOne(String tenantId, String userId, String invoiceId) {
        InvoiceDto dto = invoiceMapper.selectOne(new LambdaQueryWrapper<InvoiceDto>()
                .eq(InvoiceDto::getId, invoiceId)
                .eq(InvoiceDto::getTenantId, tenantId)
                .eq(InvoiceDto::getOwnerUserId, userId)
                .last("LIMIT 1"));
        return invoiceMapstruct.map(dto);
    }

    @Override
    public InvoiceFileAccessEntity previewFile(String fileId) {
        return mapAccess(unwrap(fileClient.preview(fileId), "File preview failed"));
    }

    @Override
    public InvoiceFileAccessEntity downloadFile(String fileId) {
        return mapAccess(unwrap(fileClient.download(fileId), "File download failed"));
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

    private static CoreFileAccessResponse unwrap(RestResponse<CoreFileAccessResponse> response, String defaultMessage) {
        if (response != null && response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        String message = response == null || !StringUtils.hasText(response.getError())
                ? defaultMessage
                : response.getError();
        throw new ServiceException(message, ErrorCode.FILE_STORAGE_FAILED);
    }
}
