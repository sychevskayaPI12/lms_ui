package com.anast.lms.service.external;

import com.anast.lms.client.ModerationRestService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;

@FeignClient(name = "moderation-service", url = "${services.studying-service-url}",
        configuration = {FeignClientsConfiguration.class})
public interface ModerationServiceClient extends ModerationRestService {
}
