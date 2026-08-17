package com.example.loanApp.enums;

public enum ExpenseType {
    DIRECT_BILL,
    OFFICER_REIMBURSEMENT,
    ADVANCE_PAYMENT,

    // legacy values kept so historic rows still deserialize
    office,
    transport,
    bill
}
