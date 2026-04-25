package com.gbsw.snapy.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.global.exception.ErrorResponse;
import com.gbsw.snapy.global.security.CustomUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@RequiredArgsConstructor
public class PhoneRegistrationFilter extends OncePerRequestFilter {

    private static final Set<String> PHONE_EXEMPT_PATHS = Set.of(
            "/api/users/me/phone"
    );

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            if (!principal.hasPhone() && !isPhoneExempt(request)) {
                response.setStatus(ErrorCode.PHONE_NOT_REGISTERED.getStatus().value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                        objectMapper.writeValueAsString(ErrorResponse.of(ErrorCode.PHONE_NOT_REGISTERED))
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPhoneExempt(HttpServletRequest request) {
        return PHONE_EXEMPT_PATHS.contains(request.getRequestURI());
    }
}
