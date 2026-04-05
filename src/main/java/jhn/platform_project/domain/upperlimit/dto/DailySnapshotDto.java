package jhn.platform_project.domain.upperlimit.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailySnapshotDto {
    private LocalDate baseDate;     // 날짜(yyyy-MM-dd)
    private Integer count;          // 해당 날짜 결과 수
    private List<UpperLimitRowDto> rows;
}