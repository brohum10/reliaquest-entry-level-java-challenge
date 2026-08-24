package com.challenge.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.challenge.api.model.EmployeeModel;
import com.challenge.api.service.EmployeeService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    // MockMvc calls the controller like an HTTP client without starting a real server.
    @Autowired
    private MockMvc mockMvc;

    // The service is mocked here because this test class focuses only on controller behavior.
    @MockBean
    private EmployeeService employeeService;

    @Test
    void returnsAllEmployees() throws Exception {
        EmployeeModel employee = employee();
        when(employeeService.getAllEmployees()).thenReturn(List.of(employee));

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].uuid").value(employee.getUuid().toString()))
                .andExpect(jsonPath("$[0].fullName").value("Ada Lovelace"));
    }

    @Test
    void returnsEmployeeByUuid() throws Exception {
        EmployeeModel employee = employee();
        when(employeeService.getEmployeeByUuid(employee.getUuid())).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/v1/employee/{uuid}", employee.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada@example.com"));
    }

    @Test
    void returnsNotFoundForUnknownUuid() throws Exception {
        UUID uuid = UUID.randomUUID();
        when(employeeService.getEmployeeByUuid(uuid)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/employee/{uuid}", uuid)).andExpect(status().isNotFound());
    }

    @Test
    void createsEmployee() throws Exception {
        EmployeeModel employee = employee();
        when(employeeService.createEmployee(any())).thenReturn(employee);

        mockMvc.perform(
                        post("/api/v1/employee")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "firstName": "Ada",
                                  "lastName": "Lovelace",
                                  "salary": 100000,
                                  "age": 36,
                                  "jobTitle": "Engineer",
                                  "email": "ada@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value(employee.getUuid().toString()));
    }

    @Test
    void returnsBadRequestForInvalidEmployee() throws Exception {
        when(employeeService.createEmployee(any())).thenThrow(new IllegalArgumentException("firstName is required"));

        mockMvc.perform(
                        post("/api/v1/employee")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "firstName": "",
                                  "lastName": "Lovelace"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private EmployeeModel employee() {
        // A shared fixture keeps each controller test focused on the behavior it is checking.
        EmployeeModel employee = new EmployeeModel();
        employee.setUuid(UUID.randomUUID());
        employee.setFirstName("Ada");
        employee.setLastName("Lovelace");
        employee.setFullName("Ada Lovelace");
        employee.setSalary(100000);
        employee.setAge(36);
        employee.setJobTitle("Engineer");
        employee.setEmail("ada@example.com");
        return employee;
    }
}
