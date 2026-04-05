package jhn.platform_project.domain.upperlimit.validator;

import jhn.platform_project.domain.upperlimit.dto.UpperLimitSearchRequest;
import jhn.platform_project.global.error.UpperLimitErrorCode;
import jhn.platform_project.global.error.UpperLimitException;
import org.springframework.stereotype.Component;

@Component
public class UpperLimitSearchRequestValidator {

    public void validate(UpperLimitSearchRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new UpperLimitException(UpperLimitErrorCode.INVALID_DATE_RANGE);
        }

        validateRange(request.getClosePriceMin(), request.getClosePriceMax(), "종가");
        validateRange(request.getTradingValueMin(), request.getTradingValueMax(), "거래대금");
        validateRange(request.getTradingVolumeMin(), request.getTradingVolumeMax(), "거래량");

        validateMarket(request.getMarket());
    }

    private void validateRange(Long min, Long max, String fieldName) {
        if (min != null && max != null && min > max) {
            throw new UpperLimitException(UpperLimitErrorCode.INVALID_RANGE, fieldName);
        }
    }

    private void validateMarket(String market) {
        if (market == null || market.isBlank() || market.equalsIgnoreCase("ALL")) {
            return;
        }

        String m = market.toUpperCase();
        if (!m.equals("KOSPI") && !m.equals("KOSDAQ")) {
            throw new UpperLimitException(UpperLimitErrorCode.INVALID_MARKET);
        }
    }
}