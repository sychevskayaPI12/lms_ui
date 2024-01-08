package com.anast.lms.service.security;

import com.vaadin.flow.server.HandlerHelper.RequestType;
import com.vaadin.flow.shared.ApplicationConstants;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Stream;

public final class SecurityUtils {

    private static final String REGISTRATION_URL = "/registration";

    public SecurityUtils() { }

    /**
     * determines if a request is internal to Vaadin
     */
    static boolean isFrameworkInternalRequest(HttpServletRequest request) {
        final String parameterValue = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
        return parameterValue != null
                && Stream.of(RequestType.values())
                .anyMatch(r -> r.getIdentifier().equals(parameterValue));
    }

    static boolean isPermittedToUnauthorized(HttpServletRequest request) {
        return request.getRequestURI().equals(REGISTRATION_URL);
    }

    /**
     * checks if the current user is logged in
     */
    static boolean isUserLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();
    }
}
