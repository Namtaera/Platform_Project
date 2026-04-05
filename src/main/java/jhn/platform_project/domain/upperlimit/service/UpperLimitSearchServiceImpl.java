package jhn.platform_project.domain.upperlimit.service;

import jhn.platform_project.domain.upperlimit.calc.UpperLimitSearchCalculator;
import jhn.platform_project.domain.upperlimit.dto.UpperLimitSearchRequest;
import jhn.platform_project.domain.upperlimit.dto.UpperLimitSearchResponse;
import jhn.platform_project.domain.upperlimit.repository.DailyUpperLimitRepository;
import jhn.platform_project.domain.upperlimit.validator.UpperLimitSearchRequestValidator;
import jhn.platform_project.global.error.UpperLimitErrorCode;
import jhn.platform_project.global.error.UpperLimitException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpperLimitSearchServiceImpl implements UpperLimitSearchService {

    private final DailyUpperLimitRepository dailyUpperLimitRepository;
    private final UpperLimitSearchRequestValidator validator;
    private final UpperLimitSearchCalculator calculator;

    private static final DateTimeFormatter BASIC = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public UpperLimitSearchResponse search(UpperLimitSearchRequest request) {
        validator.validate(request);

        Long start = Long.parseLong(request.getStartDate().format(BASIC));
        Long end = Long.parseLong(request.getEndDate().format(BASIC));

        String market = (request.getMarket() == null) ? "ALL" : request.getMarket().toUpperCase();

        List<?> rows;
        List<?> dailyRows;

        List<jhn.platform_project.domain.upperlimit.entity.DailyUpperLimit> fetched;
        if (market.equals("ALL") || market.isBlank()) {
            fetched = dailyUpperLimitRepository.findByBaseDateBetween(start, end);
        } else {
            fetched = dailyUpperLimitRepository.findByBaseDateBetweenAndMarket(start, end, market);
        }

        var snapshots = calculator.buildSnapshots(request, fetched);

        if (snapshots.isEmpty()) {
            throw new UpperLimitException(UpperLimitErrorCode.NO_DATA);
        }

        return UpperLimitSearchResponse.builder()
                .message("상한가 일별 조회가 완료되었습니다.")
                .snapshots(snapshots)
                .build();
    }
}