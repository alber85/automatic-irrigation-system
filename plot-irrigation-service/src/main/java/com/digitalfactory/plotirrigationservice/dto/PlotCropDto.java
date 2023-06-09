package com.digitalfactory.plotirrigationservice.dto;

import com.digitalfactory.baseservice.dto.BaseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlotCropDto extends BaseDto {

    private Long id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @NotNull(message = "This field is required")
    private Long plotId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @NotNull(message = "This field is required")
    private Long cropId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private PlotDto plot;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private CropDto crop;

    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @AssertTrue(message = "End date must be greater than start date")
    private boolean isEndDateAfterStartDate() {
        return endDate == null || endDate.isAfter(startDate);
    }

    @NotNull(message = "This field is required")
    @DecimalMin(value = "1", message = "minimum area is 1")
    private BigDecimal cultivatedArea;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PlotIrrigationSlotDto> plotIrrigationSlots;
}
