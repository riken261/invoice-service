package cloud.techotakus.invoice.integration.domain.entity;

import lombok.Data;

@Data
public class OcrField {
    private String name;
    private String normalizedName;
    private String value;
    private Double confidence;
    private Boolean lowConfidence;
    private String rawKey;
    private Integer row;
    private OcrPolygon polygon;
}
