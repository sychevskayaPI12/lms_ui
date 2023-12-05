package com.anast.lms.service.security;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Saves unauthenticated requests, so that, once the user is logged in,
 * you can redirect them to the page they were trying to access
 */
public class CustomRequestCache extends HttpSessionRequestCache{

    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        if (!SecurityUtils.isFrameworkInternalRequest(request)) {
            super.saveRequest(request, response);
        }
    }
}
