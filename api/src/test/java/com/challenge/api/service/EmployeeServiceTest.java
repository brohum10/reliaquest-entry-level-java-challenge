package com.challenge.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeRequest;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class EmployeeServiceTest {

    private final EmployeeService employeeService = new EmployeeService();

    @Test
    void createsAndRetrievesEmployee() {
        EmployeeRequest request =
                new EmployeeRequest("Ada", "Lovelace", 100000, 36, "Engineer", "ada@example.com", Instant.now(), null);

        Employee created = employeeService.createEmployee(request);

        assertNotNull(created.getUuid());
        assertEquals("Ada Lovelace", created.getFullName());
        assertEquals(
                created, employeeService.getEmployeeByUuid(created.getUuid()).orElseThrow());
        assertEquals(1, employeeService.getAllEmployees().size());
    }

    @Test
    void suppliesHireDateWhenItIsNotProvided() {
        EmployeeRequest request =
                new EmployeeRequest("Grace", "Hopper", 90000, 40, "Engineer", "grace@example.com", null, null);

        Employee created = employeeService.createEmployee(request);

        assertNotNull(created.getContractHireDate());
    }

    @Test
    void rejectsInvalidEmployee() {
        EmployeeRequest request = new EmployeeRequest("", "Hopper", -1, 10, "Engineer", "invalid", Instant.now(), null);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(request));

        assertEquals("firstName is required", exception.getMessage());
        assertTrue(employeeService.getAllEmployees().isEmpty());
    }

    @Test
    void rejectsTerminationBeforeGeneratedHireDate() {
        EmployeeRequest request =
                new EmployeeRequest("Grace", "Hopper", 90000, 40, "Engineer", "grace@example.com", null, Instant.EPOCH);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(request));

        assertEquals("contractTerminationDate cannot precede contractHireDate", exception.getMessage());
    }
}
