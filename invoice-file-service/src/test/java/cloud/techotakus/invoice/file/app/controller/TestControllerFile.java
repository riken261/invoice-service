package cloud.techotakus.invoice.file.app.controller;

import cloud.techotakus.invoice.file.app.mapstruct.FileOperationMapstruct;
import cloud.techotakus.invoice.file.domain.usecase.FileOperationUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestControllerFile {

    @Mock
    private FileOperationUseCase useCase;

    @Mock
    private FileOperationMapstruct mapstruct;

    @InjectMocks
    private FileOperationController controller;

    @Test
    void testReturnsMappedResponse() {

    }
}
