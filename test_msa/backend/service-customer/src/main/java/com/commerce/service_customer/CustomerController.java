package com.commerce.service_customer;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerRepository customers;

    public CustomerController(CustomerRepository customers) {
        this.customers = customers;
    }

    @GetMapping("/customers")
    public List<Customer> list() {
        return customers.findAll();
    }

    @PostMapping("/customers")
    public Customer create(@RequestBody CreateCustomer request) {
        return customers.save(new Customer(request.name(), request.email()));
    }

    public record CreateCustomer(@NotBlank String name, @NotBlank String email) {}
}
