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

//this owns employee creation and lookup rules
// storage is intentionally in memory because persistence is outside the scope of the challenge

@Service
    
public class EmployeeService {

    // small boundary check.
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final Map<UUID, Employee> employees = new ConcurrentHashMap<>();

    public List<Employee> getAllEmployees() {
        // returns snapshot of callers ..
        return List.copyOf(employees.values());
    }

    public Optional<Employee> getEmployeeByUuid(UUID uuid) {
        return Optional.ofNullable(employees.get(uuid));
    }

    public Employee createEmployee(EmployeeRequest request) {
        //added this to stop early if the request body is missing instead of trying to create an empty employee.
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        Instant hireDate = request.contractHireDate() == null ? Instant.now() : request.contractHireDate();

        //to validate every input before building and saving the employee
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

    //to keep all input rules together so createEmployee can actually stay focused on constructing the object
    private void validate(EmployeeRequest request, Instant hireDate) {
        requireText(request.firstName(), "firstName");
        requireText(request.lastName(), "lastName");
        requireText(request.jobTitle(), "jobTitle");
        requireText(request.email(), "email");

        // will make sure the email will have the normal format
        if (!EMAIL_PATTERN.matcher(request.email().trim()).matches()) {
            throw new IllegalArgumentException("email must be valid");
        }

        // Salary cant be negative
        if (request.salary() == null || request.salary() < 0) {
            throw new IllegalArgumentException("salary must be zero or greater");
        }

        // age in the range chosen for this
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
