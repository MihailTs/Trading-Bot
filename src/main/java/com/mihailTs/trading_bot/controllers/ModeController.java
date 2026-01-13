package com.mihailTs.trading_bot.controllers;

import com.mihailTs.trading_bot.service.ModeManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
            LocalDateTime start = LocalDateTime.parse(startDate, formatter);
            LocalDateTime end = LocalDateTime.parse(endDate, formatter);

            if (start.isAfter(end)) {
                return ResponseEntity.badRequest()
                        .body("Start date must be before end date");
            }

            // validate max 3 days range for training data
            long days = end.toLocalDate().toEpochDay() - start.toLocalDate().toEpochDay();
            if (days > 3) {
                return ResponseEntity.badRequest()
                        .body("Training data range cannot exceed 3 days");
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
        String response = "Current mode: " + modeManager.getMode();
        if (modeManager.isTrainingMode()) {
            response += " | Start: " + modeManager.getTrainingStartDate() +
                    " | End: " + modeManager.getTrainingEndDate();
        }
        return ResponseEntity.ok(response);
    }
}