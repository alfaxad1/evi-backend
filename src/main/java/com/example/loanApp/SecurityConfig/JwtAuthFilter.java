package com.example.loanApp.SecurityConfig;

import com.example.loanApp.context.BranchContext;
import com.example.loanApp.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String jwtToken = authHeader.substring(7);
            String username = jwtUtils.extractUsername(jwtToken);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userService.loadUserByUsername(username);

                if (jwtUtils.isTokenValid(jwtToken, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource()
                            .buildDetails(request));

                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);

                    // ===============================
                    // 🔥 BRANCH RESOLUTION LOGIC
                    // ===============================
                    String role = jwtUtils.extractRole(jwtToken);
                    Integer branchId;

                    if ("ADMIN".equalsIgnoreCase(role)) {
                        String headerBranchId = request.getHeader("X-BranchId");
                        branchId = headerBranchId != null
                                ? Integer.valueOf(headerBranchId)
                                : null;
                    } else {
                        branchId = jwtUtils.extractBranchId(jwtToken);
                    }

                    if (branchId == null && !"ADMIN".equalsIgnoreCase(role)) {
                        throw new RuntimeException("Branch not resolved for user");
                    }

                    BranchContext.set(branchId);
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            // IMPORTANT: clear thread-local after request
            BranchContext.clear();
        }
    }
}
