package jhn.platform_project.domain.upperlimit.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DailyUpperLimitId implements Serializable {

    private Long baseDate;
    private String market;
    private String ticker;
}