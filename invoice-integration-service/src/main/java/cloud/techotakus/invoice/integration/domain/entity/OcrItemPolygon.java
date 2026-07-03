package cloud.techotakus.invoice.integration.domain.entity;

import lombok.Data;

@Data
public class OcrItemPolygon {
    private String key;
    private OcrPolygon polygon;
    private Integer row;
}
