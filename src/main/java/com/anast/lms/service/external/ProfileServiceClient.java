package com.anast.lms.service.external;

import com.anast.lms.client.ProfileRestService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;

@FeignClient(name = "profile-service", url = "${services.studying-service-url}", configuration = {FeignClientsConfiguration.class})
public interface ProfileServiceClient extends ProfileRestService {
}
