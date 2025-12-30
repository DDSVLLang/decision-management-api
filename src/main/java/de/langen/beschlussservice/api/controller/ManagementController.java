package de.langen.beschlussservice.api.controller;


import de.langen.beschlussservice.api.dto.response.ApiResponse;
import de.langen.beschlussservice.api.dto.response.DepartmentResponse;
import de.langen.beschlussservice.api.dto.response.TopicResponse;
import de.langen.beschlussservice.api.dto.response.UserResponse;
import de.langen.beschlussservice.application.service.ManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/management")
@RequiredArgsConstructor
@Tag(name = "Management API", description = "Master data management")
public class ManagementController {

    private final ManagementService managementService;

    @GetMapping("/topic")
    // @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get all topics")
    public ApiResponse<List<TopicResponse>> getAllTopics() {
        return ApiResponse.success(managementService.getAllTopics());
    }

    @GetMapping("/department")
    // @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get all departments")
    public ApiResponse<List<DepartmentResponse>> getAllDepartments() {
        return ApiResponse.success(managementService.getAllDepartments());
    }

    @GetMapping("/user")
    // @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users")
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.success(managementService.getAllUsers());
    }
}
