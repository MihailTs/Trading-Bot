package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.model.TrainingPrice;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
public class ModeManager {

    public enum Mode {
        LIVE, TRAINING
    }

    @Getter
    private Mode currentMode = Mode.LIVE;
    @Getter
    private LocalDate trainingStartDate;
    @Getter
    private LocalDate trainingEndDate;
    @Getter
    private LocalDate currentTrainingDate;
    @Getter
    private LocalDate lastFetchedDay;
    @Getter
    private int currentBatchPointer;
    // each batch contains the prices for one calendar day
    private List<TrainingPrice> trainingBatch;
    @Getter
    private int totalDaysProcessed;
    private final TrainingAssetService trainingAssetService;
    private final TrainingPriceService trainingPriceService;
    private final TrainingWalletService trainingWalletService;
    private final TrainingTransactionService trainingTransactionService;

    public ModeManager(TrainingTransactionService trainingTransactionService,
                       TrainingWalletService trainingWalletService,
                       TrainingAssetService trainingAssetService,
                       TrainingPriceService trainingPriceService) {
        this.trainingAssetService = trainingAssetService;
        this.trainingPriceService = trainingPriceService;
        this.trainingWalletService = trainingWalletService;
        this.trainingTransactionService = trainingTransactionService;
    }

    private final List<Consumer<Mode>> modeChangeListeners = new ArrayList<>();

    public void setMode(Mode mode) {
        this.currentMode = mode;
        System.out.println("Mode switched to: " + mode);

        if(mode == Mode.TRAINING) {
            clearTrainingData();
            trainingWalletService.addMoneyToWallet("USD", BigDecimal.valueOf(1000));
        }

        modeChangeListeners.forEach(listener -> listener.accept(mode));
    }

    public void setTrainingDates(LocalDate startDate, LocalDate endDate) {
        this.trainingStartDate = startDate;
        this.trainingEndDate = endDate;
        this.currentTrainingDate = startDate;
        this.lastFetchedDay = startDate.minusDays(1);
        this.currentBatchPointer = 0;
        this.totalDaysProcessed = 0;
        this.trainingBatch = null;
        System.out.println("Training dates set - Start: " + startDate + ", End: " + endDate);
    }

    public boolean isLiveMode() {
        return currentMode == Mode.LIVE;
    }

    public boolean isTrainingMode() {
        return currentMode == Mode.TRAINING;
    }

    public void initializeDayBatch(List<TrainingPrice> batch) {
        this.trainingBatch = batch;
        this.currentBatchPointer = 0;
        System.out.println("Batch initialized with " + (batch != null ? batch.size() : 0) + " prices");
    }

    public TrainingPrice getNextTrainingPrice() {
        if (trainingBatch == null || trainingBatch.isEmpty()) {
            System.out.println("Training batch is null or empty");
            return null;
        }

        if (currentBatchPointer >= trainingBatch.size()) {
            System.out.println("Batch exhausted - pointer: " + currentBatchPointer + ", size: " + trainingBatch.size());
            return null;
        }

        TrainingPrice price = trainingBatch.get(currentBatchPointer);
        currentBatchPointer++;

        return price;
    }

    public boolean isBatchExhausted() {
        boolean exhausted = (trainingBatch == null || currentBatchPointer >= trainingBatch.size());
        if (exhausted && trainingBatch != null) {
            System.out.println("Batch exhausted check - pointer: " + currentBatchPointer + ", batch size: " + trainingBatch.size());
        }
        return exhausted;
    }

    public void markDayComplete() {
        totalDaysProcessed++;
        currentBatchPointer = 0;
        trainingBatch = null;
        currentTrainingDate = currentTrainingDate.plusDays(1);
        System.out.println("Day marked complete. Total days processed: " + totalDaysProcessed + ", Moving to: " + currentTrainingDate);
    }

    public boolean hasReachedTrainingEnd() {
        return currentTrainingDate != null && currentTrainingDate.isAfter(trainingEndDate);
    }

    public void markDayFetched() {
        lastFetchedDay = lastFetchedDay.plusDays(1);
        System.out.println("Day fetched: " + lastFetchedDay);
    }

    public void clearTrainingData() {
        trainingTransactionService.clearData();
        trainingWalletService.clearData();
        trainingPriceService.clearData();
        trainingAssetService.clearData();
    }

}