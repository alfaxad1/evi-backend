package com.example.loanApp.utility;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

public class GetResponseHandler {
    public static ResponseEntity<Object> responseBuilder(
            String message,
            HttpStatus httpStatus,
            Object responseObject,
            Object meta
//            int page,
//            Long total,
//            int limit,
//            int totalPages
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("HttpStatus", httpStatus);
        response.put("data", responseObject);
        response.put("meta", meta);
//        response.put("page", page);
//        response.put("total", total);
//        response.put("limit", limit);
//        response.put("totalPages", totalPages);

        return new ResponseEntity<>(response, httpStatus);
    }

}
