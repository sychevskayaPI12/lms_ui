package com.anast.lms.service.external;

import com.anast.lms.client.ProfileRestService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;

@FeignClient(name = "prifile-service", url = "${services.studying-service-url}", configuration = {FeignClientsConfiguration.class})
public interface ProfileServiceClient extends ProfileRestService {
}
