package com.example.loanApp.service;

import com.example.loanApp.dtos.CreateGuarantorRequest;
import com.example.loanApp.dtos.CreateRefereeRequest;
import com.example.loanApp.entities.Customer;
import com.example.loanApp.entities.Guarantor;
import com.example.loanApp.entities.Referee;
import com.example.loanApp.entities.User;
import com.example.loanApp.enums.Role;
import com.example.loanApp.repository.CustomerRepository;
import com.example.loanApp.repository.GurantorRepository;
import com.example.loanApp.repository.RefereeRepository;
import com.example.loanApp.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServicesImplTest {
    @Mock CustomerRepository customers;
    @Mock GurantorRepository guarantors;
    @Mock RefereeRepository referees;
    @Mock UserRepository users;
    @InjectMocks CustomerServicesImpl service;
    User officer;
    Customer customer;

    @BeforeEach void setup() {
        officer = new User(); officer.setId(2); officer.setRole(Role.officer);
        customer = new Customer(); customer.setId(12); customer.setUser(officer);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("officer@example.test", "", List.of()));
        when(users.findByEmail("officer@example.test")).thenReturn(Optional.of(officer));
        when(customers.findById(12)).thenReturn(Optional.of(customer));
    }

    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    @Test void savesGuarantorForOwnCustomer() {
        service.addGuarantor(12, CreateGuarantorRequest.builder().name("Grace").nationalId("12345").phoneNumber("0700000000").relationship("Sister").build());
        ArgumentCaptor<Guarantor> saved = ArgumentCaptor.forClass(Guarantor.class);
        verify(guarantors).save(saved.capture());
        assertSame(customer, saved.getValue().getCustomer());
        assertEquals("Grace", saved.getValue().getName());
        assertEquals("12345", saved.getValue().getNationalId());
    }

    @Test void savesRefereeForOwnCustomer() {
        service.addReferee(12, CreateRefereeRequest.builder().name("Peter").idNumber("23456").phoneNumber("0700000001").relationship("Friend").build());
        ArgumentCaptor<Referee> saved = ArgumentCaptor.forClass(Referee.class);
        verify(referees).save(saved.capture());
        assertSame(customer, saved.getValue().getCustomer());
        assertEquals("Peter", saved.getValue().getName());
    }

    @Test void rejectsAnotherOfficersCustomer() {
        User other = new User(); other.setId(3); customer.setUser(other);
        assertThrows(AccessDeniedException.class, () -> service.addReferee(12, CreateRefereeRequest.builder().name("Peter").build()));
        verifyNoInteractions(referees);
    }
}