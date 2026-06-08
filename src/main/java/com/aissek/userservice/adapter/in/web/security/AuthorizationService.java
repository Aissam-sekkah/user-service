package com.aissek.userservice.adapter.in.web.security;

import com.aissek.userservice.adapter.out.security.UserSecurityDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Authorization helper exposed to SpEL in {@code @PreAuthorize} expressions as {@code @authz}.
 * Performs null- and type-safe ownership checks so that expressions never blow up
 * (and turn into a 500) when the principal is not a {@link UserSecurityDetails}.
 */
@Component("authz")
public class AuthorizationService {

    /**
     * @return true only when the request is made by the user identified by {@code userId}.
     */
    public boolean isSelf(Authentication authentication, String userId) {
        if (authentication == null || userId == null) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserSecurityDetails details) {
            return userId.equals(details.getUser().getId());
        }
        return false;
    }
}
