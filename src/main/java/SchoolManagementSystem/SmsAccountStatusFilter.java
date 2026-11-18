package SchoolManagementSystem;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

// Re-check active status of accounts on every authenticated request. Disabling
// a user revokes their already-established session on its next request.
public class SmsAccountStatusFilter extends OncePerRequestFilter {
    private final JdbcTemplate jdbc;
    public SmsAccountStatusFilter(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication a=SecurityContextHolder.getContext().getAuthentication();
        if(a!=null && a.isAuthenticated() && !"anonymousUser".equals(a.getPrincipal())) {
            Integer enabled=jdbc.queryForObject(
                "SELECT COUNT(*) FROM APP_USER WHERE username=? AND enabled=TRUE",
                Integer.class,a.getName());
            if(enabled==null || enabled==0) {
                if(request.getSession(false)!=null) request.getSession(false).invalidate();
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Account disabled\"}");
                return;
            }
        }
        chain.doFilter(request,response);
    }
}
