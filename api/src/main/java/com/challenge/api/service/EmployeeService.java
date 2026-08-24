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

@Service
public class EmployeeService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final Map<UUID, Employee> employees = new ConcurrentHashMap<>();

    public List<Employee> getAllEmployees() {
        return List.copyOf(employees.values());
    }

    public Optional<Employee> getEmployeeByUuid(UUID uuid) {
        return Optional.ofNullable(employees.get(uuid));
    }

    public Employee createEmployee(EmployeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        Instant hireDate = request.contractHireDate() == null ? Instant.now() : request.contractHireDate();
        validate(request, hireDate);

        EmployeeModel employee = new EmployeeModel();
        employee.setUuid(UUID.randomUUID());
        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName().trim());
        employee.setFullName(employee.getFirstName() + " " + employee.getLastName());
        employee.setSalary(request.salary());
        employee.setAge(request.age());
        employee.setJobTitle(request.jobTitle().trim());
        employee.setEmail(request.email().trim());
        employee.setContractHireDate(hireDate);
        employee.setContractTerminationDate(request.contractTerminationDate());
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
