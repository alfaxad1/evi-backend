package com.example.loanApp.service;

import com.example.loanApp.dtos.*;
import com.example.loanApp.enums.LeadStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface LeadService {

    GenericResponse<List<LeadDto>> getLeads(Pageable pageable,
                                            LeadStatus status,
                                            Integer officerId,
                                            LocalDate dueBefore,
                                            String search);

    LeadDto getLead(Integer id);

    LeadDto createLead(CreateLeadRequest request);

    LeadDto updateLead(Integer id, CreateLeadRequest request);

    LeadDto updateStatus(Integer id, LeadStatus status);

    LeadActivityDto addActivity(Integer leadId, CreateLeadActivityRequest request);

    LeadDto convertLead(Integer leadId, Integer customerId);

    void deleteLead(Integer id);

    LeadSummaryDto getSummary();
}
