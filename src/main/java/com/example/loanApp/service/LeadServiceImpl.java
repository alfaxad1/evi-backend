package com.example.loanApp.service;

import com.example.loanApp.context.BranchContext;
import com.example.loanApp.dtos.*;
import com.example.loanApp.entities.Branch;
import com.example.loanApp.entities.Lead;
import com.example.loanApp.entities.LeadActivity;
import com.example.loanApp.entities.User;
import com.example.loanApp.enums.InteractionType;
import com.example.loanApp.enums.LeadStatus;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.repository.BranchRepository;
import com.example.loanApp.repository.LeadActivityRepository;
import com.example.loanApp.repository.LeadRepository;
import com.example.loanApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final LeadActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

    /* ─────────────── helpers ─────────────── */

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    private Integer resolveBranchId(Integer requested) {
        if (requested != null) return requested;
        Integer fromContext = BranchContext.get();
        if (fromContext != null) return fromContext;
        User user = currentUser();
        return user != null && user.getBranch() != null ? user.getBranch().getId() : null;
    }

    private static String fullName(User user) {
        if (user == null) return null;
        return String.join(" ",
                user.getFirstName() == null ? "" : user.getFirstName(),
                user.getLastName() == null ? "" : user.getLastName()).trim();
    }

    private LeadActivityDto toDto(LeadActivity activity) {
        return LeadActivityDto.builder()
                .id(activity.getId())
                .leadId(activity.getLead() == null ? null : activity.getLead().getId())
                .interactionDate(activity.getInteractionDate())
                .interactionType(activity.getInteractionType())
                .notes(activity.getNotes())
                .nextActionDate(activity.getNextActionDate())
                .createdById(activity.getCreatedBy() == null ? null : activity.getCreatedBy().getId())
                .createdByName(fullName(activity.getCreatedBy()))
                .createdAt(activity.getCreatedAt())
                .build();
    }

    private LeadDto toDto(Lead lead, boolean withActivities) {
        LocalDate today = LocalDate.now();
        boolean open = lead.getStatus() == LeadStatus.OPEN || lead.getStatus() == LeadStatus.CONTACTED;
        boolean due = open && lead.getNextActionDate() != null && !lead.getNextActionDate().isAfter(today);
        int overdue = due ? (int) ChronoUnit.DAYS.between(lead.getNextActionDate(), today) : 0;

        LeadDto.LeadDtoBuilder builder = LeadDto.builder()
                .id(lead.getId())
                .name(lead.getName())
                .phoneNumber(lead.getPhoneNumber())
                .status(lead.getStatus())
                .source(lead.getSource())
                .location(lead.getLocation())
                .branchId(lead.getBranch() == null ? null : lead.getBranch().getId())
                .branch(lead.getBranch() == null ? null : lead.getBranch().getName())
                .assignedOfficerId(lead.getAssignedOfficer() == null ? null : lead.getAssignedOfficer().getId())
                .assignedOfficer(fullName(lead.getAssignedOfficer()))
                .nextActionDate(lead.getNextActionDate())
                .lastInteractionDate(lead.getLastInteractionDate())
                .followUpDue(due)
                .daysOverdue(overdue)
                .convertedCustomerId(lead.getConvertedCustomerId())
                .convertedAt(lead.getConvertedAt())
                .createdAt(lead.getCreatedAt());

        if (withActivities) {
            List<LeadActivityDto> activities = activityRepository
                    .findByLeadIdOrderByInteractionDateDescIdDesc(lead.getId())
                    .stream().map(this::toDto).collect(Collectors.toList());
            builder.activities(activities).activityCount(activities.size());
        } else {
            builder.activityCount((int) activityRepository.countByLeadId(lead.getId()));
        }

        return builder.build();
    }

    /** Keeps the denormalised follow-up columns on the lead in sync. */
    private void applyActivityToLead(Lead lead, LeadActivity activity) {
        lead.setLastInteractionDate(activity.getInteractionDate());
        lead.setNextActionDate(activity.getNextActionDate());
        if (lead.getStatus() == LeadStatus.OPEN) lead.setStatus(LeadStatus.CONTACTED);
    }

    /* ─────────────── commands ─────────────── */

    @Override
    @Transactional
    public LeadDto createLead(CreateLeadRequest request) {
        if (request.getName() == null || request.getName().isBlank())
            throw new RuntimeException("Lead name is required");
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank())
            throw new RuntimeException("Phone number is required");

        User creator = currentUser();

        Lead lead = new Lead();
        lead.setName(request.getName().trim());
        lead.setPhoneNumber(request.getPhoneNumber().trim());
        lead.setStatus(request.getStatus() == null ? LeadStatus.OPEN : request.getStatus());
        lead.setSource(request.getSource());
        lead.setLocation(request.getLocation());
        lead.setNextActionDate(request.getNextActionDate());
        lead.setLastInteractionDate(request.getInteractionDate() == null ? LocalDate.now() : request.getInteractionDate());
        if (creator != null) lead.setCreatedBy(creator.getId());

        Integer branchId = resolveBranchId(request.getBranchId());
        if (branchId != null) {
            Branch branch = branchRepository.findById(branchId).orElse(null);
            lead.setBranch(branch);
        }

        Integer officerId = request.getAssignedOfficerId();
        User officer = officerId != null
                ? userRepository.findById(officerId).orElse(null)
                : creator;
        lead.setAssignedOfficer(officer);

        Lead saved = leadRepository.save(lead);

        // The first interaction (the field visit that produced the lead).
        LeadActivity first = new LeadActivity();
        first.setLead(saved);
        first.setInteractionDate(request.getInteractionDate() == null ? LocalDate.now() : request.getInteractionDate());
        first.setInteractionType(request.getInteractionType() == null ? InteractionType.FIELD_VISIT : request.getInteractionType());
        first.setNotes(request.getNotes() == null || request.getNotes().isBlank()
                ? "Lead captured in the field." : request.getNotes());
        first.setNextActionDate(request.getNextActionDate());
        first.setCreatedBy(creator);
        activityRepository.save(first);

        return toDto(saved, true);
    }

    @Override
    @Transactional
    public LeadDto updateLead(Integer id, CreateLeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        if (request.getName() != null && !request.getName().isBlank()) lead.setName(request.getName().trim());
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank())
            lead.setPhoneNumber(request.getPhoneNumber().trim());
        if (request.getStatus() != null) lead.setStatus(request.getStatus());
        if (request.getSource() != null) lead.setSource(request.getSource());
        if (request.getLocation() != null) lead.setLocation(request.getLocation());
        if (request.getNextActionDate() != null) lead.setNextActionDate(request.getNextActionDate());
        if (request.getAssignedOfficerId() != null) {
            userRepository.findById(request.getAssignedOfficerId()).ifPresent(lead::setAssignedOfficer);
        }
        if (request.getBranchId() != null) {
            branchRepository.findById(request.getBranchId()).ifPresent(lead::setBranch);
        }

        return toDto(leadRepository.save(lead), true);
    }

    @Override
    @Transactional
    public LeadDto updateStatus(Integer id, LeadStatus status) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        lead.setStatus(status);
        if (status == LeadStatus.CONVERTED || status == LeadStatus.DEAD) {
            lead.setNextActionDate(null);
        }
        return toDto(leadRepository.save(lead), true);
    }

    @Override
    @Transactional
    public LeadActivityDto addActivity(Integer leadId, CreateLeadActivityRequest request) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        LeadActivity activity = new LeadActivity();
        activity.setLead(lead);
        activity.setInteractionDate(request.getInteractionDate() == null ? LocalDate.now() : request.getInteractionDate());
        activity.setInteractionType(request.getInteractionType() == null ? InteractionType.PHONE_CALL : request.getInteractionType());
        activity.setNotes(request.getNotes());
        activity.setNextActionDate(request.getNextActionDate());
        activity.setCreatedBy(currentUser());

        LeadActivity saved = activityRepository.save(activity);

        applyActivityToLead(lead, saved);
        if (request.getStatus() != null) {
            lead.setStatus(request.getStatus());
            if (request.getStatus() == LeadStatus.DEAD || request.getStatus() == LeadStatus.CONVERTED) {
                lead.setNextActionDate(null);
            }
        }
        leadRepository.save(lead);

        return toDto(saved);
    }

    /**
     * Called by the frontend right after the customer registration form
     * succeeds: marks the lead converted so it drops off the active list.
     */
    @Override
    @Transactional
    public LeadDto convertLead(Integer leadId, Integer customerId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));

        lead.setStatus(LeadStatus.CONVERTED);
        lead.setConvertedCustomerId(customerId);
        lead.setConvertedAt(LocalDateTime.now());
        lead.setNextActionDate(null);

        LeadActivity activity = new LeadActivity();
        activity.setLead(lead);
        activity.setInteractionDate(LocalDate.now());
        activity.setInteractionType(InteractionType.OTHER);
        activity.setNotes("Lead converted into a registered customer"
                + (customerId == null ? "." : " (customer #" + customerId + ")."));
        activity.setCreatedBy(currentUser());
        activityRepository.save(activity);

        lead.setLastInteractionDate(LocalDate.now());
        return toDto(leadRepository.save(lead), true);
    }

    @Override
    @Transactional
    public void deleteLead(Integer id) {
        leadRepository.deleteById(id);
    }

    /* ─────────────── queries ─────────────── */

    @Override
    public LeadDto getLead(Integer id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        return toDto(lead, true);
    }

    @Override
    public GenericResponse<List<LeadDto>> getLeads(Pageable pageable,
                                                   LeadStatus status,
                                                   Integer officerId,
                                                   LocalDate dueBefore,
                                                   String search) {
        Integer branchId = resolveBranchId(null);
        String term = (search == null || search.isBlank()) ? null : search.trim();

        Page<Lead> leads = leadRepository.search(branchId, officerId, status, dueBefore, term, pageable);

        ResponseMetaData meta = ResponseMetaData.builder()
                .page(leads.getNumber())
                .totalElements(leads.getTotalElements())
                .totalPages(leads.getTotalPages())
                .limit(leads.getSize())
                .build();

        return GenericResponse.<List<LeadDto>>builder()
                .data(leads.getContent().stream().map(l -> toDto(l, false)).collect(Collectors.toList()))
                .message("leads fetched successfully")
                .status(ResponseStatusEnum.SUCCESS)
                .metaData(meta)
                .build();
    }

    @Override
    public LeadSummaryDto getSummary() {
        LocalDate today = LocalDate.now();
        long open = leadRepository.countByStatus(LeadStatus.OPEN)
                + leadRepository.countByStatus(LeadStatus.CONTACTED);
        long converted = leadRepository.countByStatus(LeadStatus.CONVERTED);
        long dead = leadRepository.countByStatus(LeadStatus.DEAD);
        long total = open + converted + dead;

        return LeadSummaryDto.builder()
                .openLeads(open)
                .dueToday(leadRepository.countDueOn(today))
                .overdue(leadRepository.countOverdue(today))
                .convertedThisMonth(leadRepository.countConvertedSince(
                        today.withDayOfMonth(1).atStartOfDay()))
                .deadLeads(dead)
                .totalLeads(total)
                .conversionRate(total == 0 ? 0d : (converted * 100d) / total)
                .build();
    }
}
