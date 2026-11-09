package com.ganjithon.handeulpan.user.auth;

import com.ganjithon.handeulpan.pasture.CoreError;
import com.ganjithon.handeulpan.user.User;
import com.ganjithon.handeulpan.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String DEVICE_TOKEN_HEADER = "X-Device-Token";

    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && User.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public User resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String token = webRequest.getHeader(DEVICE_TOKEN_HEADER);
        if (!StringUtils.hasText(token)) {
            throw CoreError.INVALID_DEVICE_TOKEN.exception();
        }
        return userRepository.findByDeviceToken(token.trim())
                .orElseThrow(CoreError.INVALID_DEVICE_TOKEN::exception);
    }
}
