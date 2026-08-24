package com.challenge.api.service;

import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeModel;
import com.challenge.api.model.EmployeeRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Owns employee creation and lookup rules. Storage is intentionally in memory because persistence is outside the
 * scope of the challenge.
 */
@Service
public class EmployeeService {

    // This is intentionally a small boundary check, not an attempt to reproduce the full email specification.
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    // A concurrent map is enough for this no-database challenge and remains safe across simultaneous web requests.
    private final Map<UUID, Employee> employees = new ConcurrentHashMap<>();

    public List<Employee> getAllEmployees() {
        // Return a snapshot so callers cannot modify the service's internal collection.
        return List.copyOf(employees.values());
    }

    public Optional<Employee> getEmployeeByUuid(UUID uuid) {
        return Optional.ofNullable(employees.get(uuid));
    }

    public Employee createEmployee(EmployeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        // Hire date is system-managed when the caller does not provide one.
        Instant hireDate = request.contractHireDate() == null ? Instant.now() : request.contractHireDate();
        validate(request, hireDate);

        EmployeeModel employee = new EmployeeModel();
        employee.setUuid(UUID.randomUUID());

        // Normalize text at the API boundary so stored values stay consistent.
        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName().trim());

        // Full name is derived here so clients cannot send a value that disagrees with the first and last names.
        employee.setFullName(employee.getFirstName() + " " + employee.getLastName());
        employee.setSalary(request.salary());
        employee.setAge(request.age());
        employee.setJobTitle(request.jobTitle().trim());
        employee.setEmail(request.email().trim());
        employee.setContractHireDate(hireDate);
        employee.setContractTerminationDate(request.contractTerminationDate());

        // Saving last ensures a partially constructed employee is never visible to another request.
        employees.put(employee.getUuid(), employee);
        return employee;
    }

    private void validate(EmployeeRequest request, Instant hireDate) {
        requireText(request.firstName(), "firstName");
        requireText(request.lastName(), "lastName");
        requireText(request.jobTitle(), "jobTitle");
        requireText(request.email(), "email");
        if (!EMAIL_PATTERN.matcher(request.email().trim()).matches()) {
            throw new IllegalArgumentException("email must be valid");
        }
        if (request.salary() == null || request.salary() < 0) {
            throw new IllegalArgumentException("salary must be zero or greater");
        }
        if (request.age() == null || request.age() < 16 || request.age() > 120) {
            throw new IllegalArgumentException("age must be between 16 and 120");
        }
        if (request.contractTerminationDate() != null
                && request.contractTerminationDate().isBefore(hireDate)) {
            throw new IllegalArgumentException("contractTerminationDate cannot precede contractHireDate");
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
