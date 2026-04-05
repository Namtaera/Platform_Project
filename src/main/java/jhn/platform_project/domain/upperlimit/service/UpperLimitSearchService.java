package jhn.platform_project.domain.upperlimit.service;

import jhn.platform_project.domain.upperlimit.dto.UpperLimitSearchRequest;
import jhn.platform_project.domain.upperlimit.dto.UpperLimitSearchResponse;

public interface UpperLimitSearchService {
    UpperLimitSearchResponse search(UpperLimitSearchRequest request);
}