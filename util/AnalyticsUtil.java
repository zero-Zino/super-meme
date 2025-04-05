package com.example.demo.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class AnalyticsUtil {
    
    // Format playtime in hours and minutes
    public String formatPlaytime(int minutes) {
        if (minutes < 60) {
            return minutes + " minutes";
        }
        
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        
        if (remainingMinutes == 0) {
            return hours + " hours";
        } else {
            return hours + " hours, " + remainingMinutes + " minutes";
        }
    }
    
    // Create date ranges for analytics reports
    public Map<String, Object> createDateRanges() {
        Map<String, Object> ranges = new HashMap<>();
        
        LocalDate now = LocalDate.now();
        
        // Past 7 days
        LocalDate sevenDaysAgo = now.minusDays(7);
        ranges.put("past7Days", Map.of(
            "start", sevenDaysAgo,
            "end", now,
            "label", "Past 7 Days"
        ));
        
        // Past 30 days
        LocalDate thirtyDaysAgo = now.minusDays(30);
        ranges.put("past30Days", Map.of(
            "start", thirtyDaysAgo,
            "end", now,
            "label", "Past 30 Days"
        ));
        
        // This month
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        ranges.put("thisMonth", Map.of(
            "start", firstDayOfMonth,
            "end", now,
            "label", "This Month"
        ));
        
        // Year to date
        LocalDate firstDayOfYear = now.withDayOfYear(1);
        ranges.put("yearToDate", Map.of(
            "start", firstDayOfYear,
            "end", now,
            "label", "Year to Date"
        ));
        
        // All time (limit to 5 years back for performance)
        LocalDate fiveYearsAgo = now.minusYears(5);
        ranges.put("allTime", Map.of(
            "start", fiveYearsAgo,
            "end", now,
            "label", "All Time"
        ));
        
        return ranges;
    }
    
    // Generate chart data
    public Map<String, Object> generateChartData(String type, Map<String, Number> data) {
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("type", type);
        
        List<String> labels = new ArrayList<>();
        List<Number> values = new ArrayList<>();
        
        for (Map.Entry<String, Number> entry : data.entrySet()) {
            labels.add(entry.getKey());
            values.add(entry.getValue());
        }
        
        chartData.put("labels", labels);
        chartData.put("values", values);
        
        return chartData;
    }
    
    // Calculate percentage
    public double calculatePercentage(int value, int total) {
        if (total == 0) return 0;
        return Math.round((value / (double) total) * 100 * 10) / 10.0; // Round to 1 decimal place
    }
    
    // Format date for display
    public String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a");
        return dateTime.format(formatter);
    }
    
    // Format date for display (date only)
    public String formatDate(LocalDate date) {
        if (date == null) return "";
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        return date.format(formatter);
    }
}