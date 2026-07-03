package cloud.techotakus.invoice.integration.domain.entity;

import lombok.Data;

@Data
public class OcrPolygon {
    private OcrPoint leftTop;
    private OcrPoint rightTop;
    private OcrPoint rightBottom;
    private OcrPoint leftBottom;
}
