package com.anast.lms.service.external;

import com.anast.lms.client.CourseRestService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;

@FeignClient(name = "course-service", url = "${services.studying-service-url}", configuration = {FeignClientsConfiguration.class})
public interface CourseServiceClient extends CourseRestService {
}
