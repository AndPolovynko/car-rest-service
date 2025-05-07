package ua.foxminded.carservice.dto;

import java.util.List;

public record CarSearchParameters(String manufacturerName, String model, List<String> categoryNames,
    String minProductionYear, String maxProductionYear) {
}
