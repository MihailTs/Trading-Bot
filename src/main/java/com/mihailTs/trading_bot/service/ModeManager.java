package com.mihailTs.trading_bot.service;

import lombok.Getter;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ModeManager {

    public enum Mode {
        LIVE, TRAINING
    }

    private Mode currentMode = Mode.LIVE;
    @Getter
    private LocalDateTime trainingStartDate;
    @Getter
    private LocalDateTime trainingEndDate;

    public Mode getMode() {
        return currentMode;
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
        System.out.println("Mode switched to: " + mode);
    }

    public void setTrainingDates(LocalDateTime startDate, LocalDateTime endDate) {
        this.trainingStartDate = startDate;
        this.trainingEndDate = endDate;
        System.out.println("Training dates set - Start: " + startDate + ", End: " + endDate);
    }

    public boolean isLiveMode() {
        return currentMode == Mode.LIVE;
    }

    public boolean isTrainingMode() {
        return currentMode == Mode.TRAINING;
    }
}