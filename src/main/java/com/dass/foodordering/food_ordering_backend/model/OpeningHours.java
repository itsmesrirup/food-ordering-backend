package com.dass.foodordering.food_ordering_backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpeningHours {
    // Key: DayOfWeek (MONDAY, etc.), Value: List of slots
    private Map<String, List<TimeSlot>> schedule;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private String open;  // "11:30"
        private String close; // "14:30"
    }
}