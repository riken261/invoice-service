package cloud.techotakus.invoice.integration.domain.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OcrSealInfo {
    private String companySealMark;
    private String supervisionSealMark;
    private List<String> companySealMarkInfo = new ArrayList<>();
    private List<String> supervisionSealMarkInfo = new ArrayList<>();
}
