package com.example.loanApp.controller;

import com.example.loanApp.dtos.*;
import com.example.loanApp.enums.ApprovalStatus;
import com.example.loanApp.enums.LoanStatus;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.loanApp.utility.ResponseHandler;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/loan")
@Tag(name = "Loan Management", description = "All endpoint for all the loan operations")
public class LoanController {
    @Autowired
    private LoanService loanService;

    @GetMapping("/{id}")
    @Operation(summary = "loan with id", description = "get a loan with a loan id")
    @ApiResponse(responseCode = "200", description = "loan fetched successfully")
    public ResponseEntity<Object> getLoanWithId(@PathVariable int id) {
        GenericResponse<LoanDetailsDto> response = loanService.getLoanWithId(id);
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/apply")
    @Operation(summary = "apply loan", description = "create a loan application")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "successfully created loan"),
        @ApiResponse(responseCode = "404", description = "Customer, product, or officer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> applyLoan(
            @RequestBody
            @Valid LoanApplicationRequest loanApplicationRequest)
    {
        loanService.applyLoan(loanApplicationRequest);
        return ResponseHandler.responseBuilder("successfully created loan", HttpStatus.CREATED, null);
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/approve")
    @Operation(summary = "approve loan", description = "approve a loan application")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "loan approved successfully"),
            @ApiResponse(responseCode = "404", description = "no loan found")
    })
    public ResponseEntity<?> approveLoan(@RequestParam Integer loanId)
    {
        loanService.approveLoan(loanId);
        return ResponseHandler.responseBuilder("loan approved successfully", HttpStatus.OK, null);
    }

    @PutMapping("/reject")
    @Operation(summary = "reject loan", description = "reject a loan application")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "loan rejected successfully"),
            @ApiResponse(responseCode = "404", description = "no loan found")
    })
    public ResponseEntity<?> rejectLoan(
            @RequestBody @Valid LoanRejectionRequest loanRejectionRequest)
    {
        loanService.rejectLoan(loanRejectionRequest);
        return ResponseHandler.responseBuilder("loan rejected successfully", HttpStatus.OK, null);
    }

    // get pending loans
    @GetMapping("/approval-status/{userId}")
    @Operation(summary = "get pending loans", description = "get a list of paginated pending loans")
    @ApiResponse(responseCode = "200", description = "pending loans fetched successfully")
    public ResponseEntity<Object> getLoansWithApprovalStatus(@RequestParam(required = false) String search, @RequestParam ApprovalStatus approvalStatus, @RequestParam int page, @RequestParam int size, @PathVariable int userId) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        GenericResponse<List<PendingLoansDto>> response = loanService.getLoansWithApprovalStatus(search, approvalStatus, userId, pageable);
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }

    }

    @GetMapping("/status/{userId}")
    @Operation(summary = "get loans with status", description = "get loans depending on the loan status and user id")
    @ApiResponse(responseCode = "200", description = "loans fetched successfully")
    public ResponseEntity<?> getLoanWithStatus(
            @RequestParam(required = false) String search,
            @RequestParam List<LoanStatus> status,
            @RequestParam int page,
            @RequestParam int size,
            @PathVariable int userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        GenericResponse<List<LoansDto>> response = loanService.getLoansWithStatus(search, status, userId, pageable);
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/due/{userId}")
    public ResponseEntity<?> getDueLoans(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate day,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable int userId){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        GenericResponse<List<DueLoansDto>> response = loanService.getDueLoansByDay(day, userId, search, pageable);
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/due-range/{userId}")
    public ResponseEntity<?> getDueLoansByRange(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable int userId){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        GenericResponse<List<DueLoansDto>> response = loanService.getDueLoansByRange(fromDate, toDate, userId, search, pageable);
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/dashboard-summary/{userId}")
    public ResponseEntity<?> getLoansSummary(@PathVariable int userId){
        GenericResponse<DashboardSummaryDto> response = loanService.getLoansSummary(userId);
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/rolled-over-loans")
    public ResponseEntity<?> getRolledOverLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String search,
            @RequestParam int userId
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        GenericResponse<List<RolledOverLoansDto>> response = loanService.getRolledOverLoans(userId, search, pageable);
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }

    //disburse loan
    @PutMapping("/disburse")
    public ResponseEntity<?> disburseLoan(@RequestBody LoanDisbursermentRequest loanDisbursermentRequest){
        loanService.disburseLoan(loanDisbursermentRequest);
        return ResponseHandler.responseBuilder("loan disbursed successfully", HttpStatus.OK, null);
    }

    @GetMapping("/monthly-data")
    public ResponseEntity<?> getMonthlyData( @RequestParam String type, @RequestParam Long month, @RequestParam Long year) {

        GenericResponse<List<MonthlyDataDto>> response = loanService.getMonthlyData(month, year, type);

        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }

    }

    //@PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/roll-over")
    public ResponseEntity<?> rollOver(@RequestParam int loanId, @RequestParam float amount){
       loanService.rollOverLoan(loanId, amount);
        return ResponseHandler.responseBuilder("loan rolled over successfully", HttpStatus.OK, null);
    }

    /**
     * Endpoint to fetch monthly Collections and Disbursement trends.
     * Maps to: GET /api/v1/analytics/monthly-trends?userId=1
     *
     * @return A list of MonthlyTrendResponse objects.
     */
    @GetMapping("/monthly-trends")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyTrends(@RequestParam Integer userId) {

        List<MonthlyTrendResponse> trends = loanService.getRolling12MonthTrends(userId);

        return ResponseEntity.ok(trends);
    }

    @GetMapping("/all-loans")
    public ResponseEntity<?> getAllLoans(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<LoanStatus> status,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) LocalDate dueDay,
            @RequestParam(required = false) LocalDate dueFrom,
            @RequestParam(required = false) LocalDate dueTo,
            @RequestParam(required = false) LocalDate appliedDate,
            @RequestParam(required = false) LocalDate disbursedDate,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) int page,
            @RequestParam(required = false) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok().body(loanService.getAllLoans(search, status, userId, branchId, dueDay, dueFrom, dueTo, appliedDate, disbursedDate, customerId, pageable));
    }

    @PostMapping("/clear/{id}")
    public ResponseEntity<?> clearLoans(@PathVariable int id){
        loanService.clearLoan(id);
        return ResponseEntity.ok().body("Loan has been cleared successfully");
    }
    
    @GetMapping("/report/collection")
    public ResponseEntity<?> getCollectionReport(
            @RequestParam Integer officerId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ){
        return ResponseEntity.ok().body(loanService.getAllCollections(officerId, startDate, endDate));
    }

    @GetMapping("/report/disbursement")
    public ResponseEntity<?> getDisbursementReport(
            @RequestParam Integer officerId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ){
        return ResponseEntity.ok().body(loanService.getAllDisbursements(officerId, startDate, endDate));
    }

}
