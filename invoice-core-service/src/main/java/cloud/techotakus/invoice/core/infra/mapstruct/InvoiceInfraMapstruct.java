package cloud.techotakus.invoice.core.infra.mapstruct;

import cloud.techotakus.invoice.core.domain.entity.InvoiceEntity;
import cloud.techotakus.invoice.core.infra.dto.InvoiceDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvoiceInfraMapstruct {

    InvoiceEntity map(InvoiceDto dto);

    InvoiceDto map(InvoiceEntity entity);
}
