package ke.co.kenit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ke.co.kenit.model.Asset;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AssetDTO {

    @Data
    public static class CreateRequest {
        @NotBlank
        private String name;

        @NotNull
        private Asset.AssetType type;

        private String brand;
        private String model;
        private String serialNumber;
        private String assignedTo;
        private String department;
        private String location;

        // KES cost — someone in Finance will thank you for this field
        private BigDecimal purchaseCostKes;
        private LocalDate purchaseDate;
        private LocalDate warrantyExpiryDate;
        private String notes;
    }

    @Data
    public static class Response {
        private Long id;
        private String assetTag;
        private String name;
        private Asset.AssetType type;
        private String brand;
        private String model;
        private String serialNumber;
        private Asset.AssetStatus status;
        private String assignedTo;
        private String department;
        private String location;
        private BigDecimal purchaseCostKes;
        private LocalDate purchaseDate;
        private LocalDate warrantyExpiryDate;
        private String ipAddress;
        private String macAddress;
        private String notes;
        private LocalDateTime createdAt;
    }
}
