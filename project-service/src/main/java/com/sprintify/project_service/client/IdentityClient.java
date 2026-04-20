package com.sprintify.project_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sprintify.project_service.dto.IdentityUserLookupResponseDTO;

@FeignClient(name = "identity-service")
public interface IdentityClient {

    @GetMapping("/api/v1/users/resolve")
    IdentityUserLookupResponseDTO getUserByEmail(@RequestParam("email") String email);
}
