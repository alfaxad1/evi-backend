package com.example.loanApp.controller;

import com.example.loanApp.dtos.*;
import com.example.loanApp.enums.LeadStatus;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/api/v1/leads")
public class LeadController {

    private final LeadService leadService;

    /** GET /api/v1/leads?page&size&status&officerId&dueBefore&search */
    @GetMapping
    public ResponseEntity<?> getLeads(
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) Integer officerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueBefore,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size);
        GenericResponse<List<LeadDto>> response =
                leadService.getLeads(pageable, status, officerId, dueBefore, search);

        if (response != null && ResponseStatusEnum.SUCCESS.equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<LeadSummaryDto> getSummary() {
        return ResponseEntity.ok(leadService.getSummary());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadDto> getLead(@PathVariable Integer id) {
        return ResponseEntity.ok(leadService.getLead(id));
    }

    @PostMapping
    public ResponseEntity<LeadDto> createLead(@RequestBody CreateLeadRequest request) {
        return ResponseEntity.ok(leadService.createLead(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeadDto> updateLead(@PathVariable Integer id,
                                              @RequestBody CreateLeadRequest request) {
        return ResponseEntity.ok(leadService.updateLead(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LeadDto> updateStatus(@PathVariable Integer id,
                                                @RequestParam LeadStatus status) {
        return ResponseEntity.ok(leadService.updateStatus(id, status));
    }

    /** Timeline entry: POST /api/v1/leads/{id}/activities */
    @PostMapping("/{id}/activities")
    public ResponseEntity<LeadActivityDto> addActivity(@PathVariable Integer id,
                                                       @RequestBody CreateLeadActivityRequest request) {
        return ResponseEntity.ok(leadService.addActivity(id, request));
    }

    /** Called after the customer registration form succeeds. */
    @PostMapping("/{id}/convert")
    public ResponseEntity<LeadDto> convertLead(@PathVariable Integer id,
                                               @RequestParam(required = false) Integer customerId) {
        return ResponseEntity.ok(leadService.convertLead(id, customerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLead(@PathVariable Integer id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok().body("Lead deleted successfully");
    }
}
