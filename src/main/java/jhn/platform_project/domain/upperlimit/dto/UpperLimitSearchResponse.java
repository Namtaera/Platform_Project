package jhn.platform_project.domain.upperlimit.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpperLimitSearchResponse {
    private String message;
    private List<DailySnapshotDto> snapshots;
}