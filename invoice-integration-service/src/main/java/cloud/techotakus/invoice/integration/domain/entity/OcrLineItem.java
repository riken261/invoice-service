package cloud.techotakus.invoice.integration.domain.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OcrLineItem {
    private String name;
    private String specification;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amountWithoutTax;
    private String taxRate;
    private BigDecimal taxAmount;
    private BigDecimal amountWithTax;
    private String vehicleType;
    private String licensePlate;
    private String dateStart;
    private String dateEnd;
    private String serialNumber;
}
