package com.example.loanApp.controller;

import com.example.loanApp.dtos.GenericResponse;
import com.example.loanApp.dtos.PaymentDto;
import com.example.loanApp.dtos.RepaymentRequest;
import com.example.loanApp.entities.Loan;
import com.example.loanApp.enums.ResponseStatusEnum;
import com.example.loanApp.service.RepaymentServiceImpl;
import com.example.loanApp.utility.DefaultChecker;
import com.example.loanApp.utility.MissedPaymentChecker;
import com.example.loanApp.utility.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/repayment")
public class RepaymentController {
    private final RepaymentServiceImpl repaymentService;
    private final DefaultChecker defaultChecker;
    private final MissedPaymentChecker missedPaymentChecker;

    RepaymentController(RepaymentServiceImpl repaymentService, DefaultChecker defaultChecker, MissedPaymentChecker missedPaymentChecker){
        this.repaymentService = repaymentService;
        this.defaultChecker = defaultChecker;
        this.missedPaymentChecker = missedPaymentChecker;
    }

    @PostMapping("/repay")
    public ResponseEntity<?> repay(@RequestBody RepaymentRequest request){
        repaymentService.repayment(request);

        return ResponseHandler.responseBuilder("repayment created successfully", HttpStatus.CREATED, null);
    }

    @PostMapping("/manual-repay")
    public ResponseEntity<?> manualRepay(
            @RequestParam Float amount,
            @RequestParam LocalDateTime date,
            @RequestParam String transactionCode,
            @RequestParam Integer loanId
    ){
        repaymentService.manualRepayment(amount, date, transactionCode, loanId);
        return ResponseHandler.responseBuilder("repayment recorded successfully", HttpStatus.CREATED, null);
    }

    @PostMapping("/resolve")
    @Operation(summary = "resolve payment", description = "resolve a payment whose phone number doesnt belong to any customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "loan resolved successfully"),
            @ApiResponse(responseCode = "404", description = "No loan or repayment found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> resolvePayment(@RequestParam int loanId, @RequestParam int repaymentId){
        repaymentService.resolvePayment(loanId, repaymentId);
        return ResponseHandler.responseBuilder("payment resolved successfully", HttpStatus.OK, null);
    }

    //@Scheduled(cron = "0 * * * * *")
    @PostMapping("/check-defaults")
    public ResponseEntity<?> checkDefaults() {
        List<Loan> loans = defaultChecker.checkLoanDefaults();
        return ResponseHandler.responseBuilder("found " + loans.toArray().length + " default(s)", HttpStatus.OK, null);
    }

    @PostMapping("/check-missed")
    public ResponseEntity<?> checkMissed(){
        List<Loan> loans = missedPaymentChecker.checkMissedPayments();
        return ResponseHandler.responseBuilder(
                "found " + loans.toArray().length + " missed payment(s)",
                HttpStatus.OK,
                null
        );
    }

//    @GetMapping("/loan/{loanId}")
//    public ResponseEntity<?> getLoanRepayments(@PathVariable int loanId){
//        List<PaymentDto> response = repaymentService.getLoanRepayments(loanId);
//            return ResponseEntity.ok().body(response);
//    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        GenericResponse<List<PaymentDto>> response = repaymentService.getPendingPayments(search, pageable);
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/approved/{userId}")
    public ResponseEntity<?> getApprovedPayments(
            @PathVariable int userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        GenericResponse<List<PaymentDto>> response = repaymentService.getApprovedPayments(userId, search, pageable);
        if(response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)){
            return ResponseEntity.ok().body(response);
        }else{
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/weekly-stats")
    public ResponseEntity<?> getWeeklyStats(@RequestParam String targetDate, Integer userId){
        return ResponseEntity.ok().body(repaymentService.getWeeklyStats(targetDate, userId));
    }

}
