package guru.sfg.brewery.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class RestHeaderAuthFiler extends AbstractAuthenticationProcessingFilter {


    public RestHeaderAuthFiler(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Request is to process authentication");
            }

        // check an unAuthorized request
        try {

            Authentication authResult = attemptAuthentication(request, response);
            if (authResult != null) {
                successfulAuthentication(request, response, chain, authResult);
            } else {
                chain.doFilter(request, response);
            }
        } catch (AuthenticationException e) {
            log.error("Authentication Failer ", e);
            unsuccessfulAuthentication(request, response, e);
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {
        SecurityContextHolder.clearContext();

        if (log.isDebugEnabled()) {
            log.debug("Authentication request failed: " + failed.toString(), failed);
            log.debug("Updated SecurityContextHolder to contain null Authentication");

        }

        // if client is unAuthorize response with http status and exception
        response.sendError(HttpStatus.UNAUTHORIZED.value(),
                           HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest,
                                                HttpServletResponse httpServletResponse) throws AuthenticationException,
                                                                                         IOException, ServletException {

        String userName = getUserName(httpServletRequest);
        String password = getPassword(httpServletRequest);

        if (userName == null) {
            userName = "";
        }
        if (password == null) {
            password = "";
        }
        log.debug("Authenticating user: " + userName);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userName, password);

        if (!StringUtils.isEmpty(userName)) {
            return this.getAuthenticationManager().authenticate(token);
        } else {
            return null;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Authentication success. Updating SecurityContextHolder to contain: " + authResult);
        }

        SecurityContextHolder.getContext().setAuthentication(authResult);

    }


    private String getPassword(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getHeader("Api-Secret");
    }

    private String getUserName(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getHeader("Api-Key");
    }


}
