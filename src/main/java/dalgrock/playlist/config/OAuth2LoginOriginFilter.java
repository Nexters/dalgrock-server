package dalgrock.playlist.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OAuth2LoginOriginFilter extends OncePerRequestFilter {

    static final String SESSION_KEY = "OAUTH2_FRONTEND_ORIGIN";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/oauth2/authorization/")) {
            String frontendOrigin = extractFrontendOrigin(request);
            if (frontendOrigin != null) {
                request.getSession().setAttribute(SESSION_KEY, frontendOrigin);
                logger.info("OAuth2 시작 시 프론트엔드 origin 저장: " + frontendOrigin);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractFrontendOrigin(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isBlank()) {
            return origin;
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            try {
                URI uri = new URI(referer);
                return uri.getScheme() + "://" + uri.getAuthority();
            } catch (URISyntaxException e) {
                logger.warn("Referer 파싱 실패: " + referer);
            }
        }

        return null;
    }
}
