package com.example.registrationservice.controller;

import com.example.registrationservice.dto.RegistrationRequestDTO;
import com.example.registrationservice.entity.Registration;
import com.example.registrationservice.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Registration register(
            @Valid @RequestBody RegistrationRequestDTO dto
    ) {
        return registrationService.register(dto);
    }

    // Buoi 9:
    // Lay danh sach dang ky cua sinh vien dang dang nhap
    // studentId duoc lay tu JWT, khong nhan tu client
    @GetMapping("/my")
    public List<Registration> getMyRegistrations(
            Authentication authentication
    ) {

        Long studentId =
                (Long) authentication.getCredentials();

        return registrationService
                .getMyRegistrations(studentId);
    }

    @DeleteMapping("/{id}")
    public void cancel(
            @PathVariable Long id
    ) {
        registrationService.cancel(id);
    }
}