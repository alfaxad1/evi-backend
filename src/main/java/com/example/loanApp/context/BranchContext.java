package com.example.loanApp.context;

public class BranchContext {
    private static final ThreadLocal<Integer> BRANCH_ID = new ThreadLocal<>();

    public static void set(Integer branchId) {
        BRANCH_ID.set(branchId);
    }

    public static Integer get() {
        return BRANCH_ID.get();
    }

    public static void clear() {
        BRANCH_ID.remove();
    }
}
