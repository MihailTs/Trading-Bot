package com.mihailTs.trading_bot.controllers;

import com.mihailTs.trading_bot.service.ModeManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/mode")
@RequiredArgsConstructor
public class ModeController {

    private final ModeManager modeManager;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @PostMapping("/set")
    public ResponseEntity<String> setMode(@RequestParam String mode) {
        try {
            ModeManager.Mode newMode = ModeManager.Mode.valueOf(mode.toUpperCase());
            modeManager.setMode(newMode);
            return ResponseEntity.ok(newMode.toString());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid mode. Use 'LIVE' or 'TRAINING'");
        }
    }

    @PostMapping("/training")
    public ResponseEntity<String> setTrainingMode(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            if (start.isAfter(end)) {
                return ResponseEntity.badRequest()
                        .body("Start date must be before end date");
            }

            // max 360 days range for training data
            long days = end.toEpochDay() - start.toEpochDay();
            if (days > 360) {
                return ResponseEntity.badRequest()
                        .body("Training data range cannot exceed 365 days");
            }

            modeManager.setTrainingDates(start, end);
            modeManager.setMode(ModeManager.Mode.TRAINING);
            return ResponseEntity.ok("Training mode set");
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid date format. Use yyyy-mm-ddTHH:mm:ss");
        }
    }

    @GetMapping("/current")
    public ResponseEntity<String> getCurrentMode() {
        String response = "Current mode: " + modeManager.getCurrentMode();
        if (modeManager.isTrainingMode()) {
            response += " | Start: " + modeManager.getTrainingStartDate() +
                    " | End: " + modeManager.getTrainingEndDate();
        }
        return ResponseEntity.ok(response);
    }
}