package com.example.marketsignal.user;

import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 인증 컨텍스트에서 현재 사용자 정보를 컨트롤러 인자로 주입한다.
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = resolveAuthentication(webRequest);
        if (authentication == null) {
            throw new InsufficientAuthenticationException("인증 정보가 없습니다.");
        }
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return new CurrentUser(principal.id(), principal.getUsername());
    }

    @Nullable
    private Authentication resolveAuthentication(NativeWebRequest webRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication;
        }

        if (webRequest.getUserPrincipal() instanceof Authentication requestAuthentication) {
            return requestAuthentication;
        }

        return null;
    }
}
