package dev.chirag45.breeze_core.controllers;

import dev.chirag45.breeze_core.dto.response.UserProvisioningResponse;
import dev.chirag45.breeze_core.dto.response.wrapper.ApiResponse;
import dev.chirag45.breeze_core.entities.UserEntity;
import dev.chirag45.breeze_core.services.UserProvisioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserProvisioningController {

    private static final Logger log = LoggerFactory.getLogger(UserProvisioningController.class);

    private final UserProvisioningService userProvisioningService;

    public UserProvisioningController(UserProvisioningService userProvisioningService) {
        this.userProvisioningService = userProvisioningService;
    }

    @PostMapping("/provision")
    public ResponseEntity<ApiResponse<UserProvisioningResponse>> provision(
            JwtAuthenticationToken authentication
    ) {
        log.info("user_provisioning_requested");
        UserEntity user = userProvisioningService.provision(authentication.getToken().getSubject());

        log.info("user_provisioning_completed");
        log.trace("user_provisioning_response_building");

        return ResponseEntity.ok(ApiResponse.success(
                UserProvisioningResponse.from(user),
                "Core user is provisioned."
        ));
    }
}
