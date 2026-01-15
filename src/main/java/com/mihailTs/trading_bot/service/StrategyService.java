package com.mihailTs.trading_bot.service;

import com.mihailTs.trading_bot.entity.ActionEnum;
import org.springframework.stereotype.Service;
import smile.timeseries.ARMA;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StrategyService {

    // if some predicted future price is some percentage larger -> BUY
    // if some predicted future price is some percentage smaller -> SELL
    // else -> HOLD
    public ActionEnum nextAction(List<Double> timeSeries) {
        double[] series = timeSeries.stream().mapToDouble(Double::doubleValue).toArray();
        ARMA model = ARMA.fit(series, 1, 3);

        double[] forecast = model.forecast(20);

        for (double v : forecast) {
            if (percentIncrease(series[series.length - 1], v) > 0.0002) {
                return ActionEnum.BUY;
            }
            if (percentIncrease(series[series.length - 1], v) < -0.0002) {
                return ActionEnum.SELL;
            }
        }

        return ActionEnum.HOLD;
    }

    private double percentIncrease(double original, double later) {
        return ((later - original) / original);
    }

}
