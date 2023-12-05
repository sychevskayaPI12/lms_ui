package com.anast.lms.service.external;

import com.anast.lms.client.UserRestService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;

@FeignClient(name = "user-service", url = "${services.user-service-url}", configuration = {FeignClientsConfiguration.class})
public interface UserServiceClient extends UserRestService {

}
