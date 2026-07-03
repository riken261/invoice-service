package cloud.techotakus.invoice.core.infra.mapstruct;

import cloud.techotakus.invoice.core.domain.entity.InvoiceRecognizeSessionEntity;
import cloud.techotakus.invoice.core.infra.dto.InvoiceRecognizeSessionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvoiceRecognizeInfraMapstruct {

    @Mapping(target = "nonce", ignore = true)
    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "files", ignore = true)
    InvoiceRecognizeSessionEntity map(InvoiceRecognizeSessionDto dto);

    InvoiceRecognizeSessionDto map(InvoiceRecognizeSessionEntity entity);
}
