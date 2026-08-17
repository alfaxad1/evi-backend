package com.example.loanApp.service;

import com.example.loanApp.Mappers.ExpenseMapper;
import com.example.loanApp.context.BranchContext;
import com.example.loanApp.dtos.*;
import com.example.loanApp.entities.Branch;
import com.example.loanApp.entities.Expense;
import com.example.loanApp.entities.User;
import com.example.loanApp.enums.ExpenseCategory;
import com.example.loanApp.enums.ExpenseStatus;
import com.example.loanApp.enums.ExpenseType;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.repository.BranchRepository;
import com.example.loanApp.repository.ExpenseRepository;
import com.example.loanApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ExpenseMapper expenseMapper;
    private final ExpenseRepository expenseRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    /* ─────────────── helpers ─────────────── */

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

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

    /** total_cost falls back to unit_cost * no_of_items when not supplied. */
    private BigDecimal computeTotal(CreateExpenseRequest request) {
        if (request.getTotalCost() != null) return request.getTotalCost();
        if (request.getUnitCost() != null) {
            BigDecimal qty = request.getNoOfItems() == null ? BigDecimal.ONE : request.getNoOfItems();
            return request.getUnitCost().multiply(qty);
        }
        return BigDecimal.ZERO;
    }

    private ExpensesDto toDto(Expense expense) {
        ExpensesDto dto = expenseMapper.toDto(expense);
        dto.setGrandTotal(nz(expense.getTotalCost()).add(nz(expense.getTransactionCost())));
        return dto;
    }

    /* ─────────────── commands ─────────────── */

    @Override
    @Transactional
    public ExpensesDto createExpense(CreateExpenseRequest request) {
        Integer branchId = resolveBranchId(request.getBranchId());
        if (branchId == null) throw new RuntimeException("Branch is required to record an expense");

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        Expense expense = expenseMapper.toEntity(request);
        expense.setBranch(branch);
        expense.setExpenseDate(request.getExpenseDate() == null ? LocalDate.now() : request.getExpenseDate());
        expense.setCategory(request.getCategory() == null ? ExpenseCategory.OTHER : request.getCategory());
        expense.setExpenseType(request.getExpenseType() == null ? ExpenseType.DIRECT_BILL : request.getExpenseType());
        expense.setStatus(request.getStatus() == null ? ExpenseStatus.PENDING : request.getStatus());
        expense.setTransactionCost(nz(request.getTransactionCost()));
        expense.setTotalCost(computeTotal(request));

        if (request.getRecipientUserId() != null) {
            expense.setRecipientUser(userRepository.findById(request.getRecipientUserId()).orElse(null));
        }

        User creator = currentUser();
        if (creator != null) expense.setCreatedBy(creator.getId());

        return toDto(expenseRepository.save(expense));
    }

    @Override
    @Transactional
    public ExpensesDto updateExpense(Integer id, CreateExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        expenseMapper.updateEntity(request, expense);

        if (request.getBranchId() != null) {
            branchRepository.findById(request.getBranchId()).ifPresent(expense::setBranch);
        }
        if (request.getRecipientUserId() != null) {
            expense.setRecipientUser(userRepository.findById(request.getRecipientUserId()).orElse(null));
        }
        if (request.getTotalCost() != null || request.getUnitCost() != null) {
            expense.setTotalCost(computeTotal(request));
        }

        return toDto(expenseRepository.save(expense));
    }

    @Override
    @Transactional
    public ExpensesDto updateStatus(Integer id, ExpenseStatus status) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        expense.setStatus(status);
        return toDto(expenseRepository.save(expense));
    }

    @Override
    @Transactional
    public void deleteExpense(Integer id) {
        expenseRepository.deleteById(id);
    }

    /* ─────────────── queries ─────────────── */

    @Override
    public GenericResponse<List<ExpensesDto>> getExpenses(Pageable pageable,
                                                          ExpenseCategory category,
                                                          ExpenseStatus status,
                                                          ExpenseType expenseType,
                                                          LocalDate from,
                                                          LocalDate to,
                                                          String search) {
        Integer branchId = resolveBranchId(null);
        String term = (search == null || search.isBlank()) ? null : search.trim();

        Page<Expense> expenses = expenseRepository.search(
                branchId, category, status, expenseType, from, to, term, pageable);

        ResponseMetaData meta = ResponseMetaData.builder()
                .page(expenses.getNumber())
                .totalElements(expenses.getTotalElements())
                .totalPages(expenses.getTotalPages())
                .limit(expenses.getSize())
                .build();

        return GenericResponse.<List<ExpensesDto>>builder()
                .data(expenses.getContent().stream().map(this::toDto).collect(Collectors.toList()))
                .message("expenses fetched successfully")
                .status(ResponseStatusEnum.SUCCESS)
                .metaData(meta)
                .build();
    }

    @Override
    public ExpenseSummaryDto getSummary(LocalDate month) {
        Integer branchId = resolveBranchId(null);
        LocalDate anchor = month == null ? LocalDate.now() : month;
        LocalDate start = anchor.withDayOfMonth(1);
        LocalDate end = anchor.withDayOfMonth(anchor.lengthOfMonth());
        LocalDate prevStart = start.minusMonths(1);
        LocalDate prevEnd = prevStart.withDayOfMonth(prevStart.lengthOfMonth());

        List<Expense> current = expenseRepository.findInPeriod(branchId, start, end);
        List<Expense> previous = expenseRepository.findInPeriod(branchId, prevStart, prevEnd);
        List<Expense> reimbursements = expenseRepository.findPendingReimbursements(branchId);

        BigDecimal mtd = current.stream()
                .map(e -> nz(e.getTotalCost()).add(nz(e.getTransactionCost())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal prev = previous.stream()
                .map(e -> nz(e.getTotalCost()).add(nz(e.getTransactionCost())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal txnCosts = current.stream()
                .map(e -> nz(e.getTransactionCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingReimbursement = reimbursements.stream()
                .map(e -> nz(e.getTotalCost()).add(nz(e.getTransactionCost())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        Map<String, BigDecimal> byDay = new TreeMap<>();
        for (Expense e : current) {
            BigDecimal amount = nz(e.getTotalCost()).add(nz(e.getTransactionCost()));
            String cat = e.getCategory() == null ? "OTHER" : e.getCategory().name();
            byCategory.merge(cat, amount, BigDecimal::add);
            if (e.getExpenseDate() != null) {
                byDay.merge(e.getExpenseDate().format(DAY), amount, BigDecimal::add);
            }
        }

        return ExpenseSummaryDto.builder()
                .monthToDateTotal(mtd)
                .previousMonthTotal(prev)
                .transactionCostTotal(txnCosts)
                .monthToDateCount(current.size())
                .pendingReimbursementTotal(pendingReimbursement)
                .pendingReimbursementCount(reimbursements.size())
                .byCategory(byCategory.entrySet().stream()
                        .map(en -> ExpenseSummaryDto.CategoryTotal.builder()
                                .category(en.getKey()).total(en.getValue()).build())
                        .sorted(Comparator.comparing(ExpenseSummaryDto.CategoryTotal::getTotal).reversed())
                        .collect(Collectors.toList()))
                .dailyTrend(byDay.entrySet().stream()
                        .map(en -> ExpenseSummaryDto.DailyTotal.builder()
                                .date(en.getKey()).total(en.getValue()).build())
                        .collect(Collectors.toList()))
                .recent(current.stream().limit(6).map(this::toDto).collect(Collectors.toList()))
                .build();
    }
}
