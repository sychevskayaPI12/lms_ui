package com.anast.lms.service.external;

import com.anast.lms.client.StudyRestService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;

@FeignClient(name = "study-service", url = "${services.studying-service-url}", configuration = {FeignClientsConfiguration.class})
public interface StudyServiceClient extends StudyRestService {
}
