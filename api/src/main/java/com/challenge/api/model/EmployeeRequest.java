package com.challenge.api.model;

import java.time.Instant;

public record EmployeeRequest(
        String firstName,
        String lastName,
        Integer salary,
        Integer age,
        String jobTitle,
        String email,
        Instant contractHireDate,
        Instant contractTerminationDate) {}
