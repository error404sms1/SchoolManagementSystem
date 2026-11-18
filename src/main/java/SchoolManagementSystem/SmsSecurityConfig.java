package SchoolManagementSystem;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SmsSecurityConfig {

    @Bean
    PasswordEncoder smsPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    UserDetailsService smsUserDetailsService(JdbcTemplate jdbc) {
        return username -> {
            List<org.springframework.security.core.userdetails.UserDetails> result = jdbc.query(
                "SELECT username,password_hash,role,enabled FROM APP_USER WHERE username=?",
                (rs, row) -> User.withUsername(rs.getString("username"))
                    .password(rs.getString("password_hash"))
                    .roles(rs.getString("role"))
                    .disabled(!rs.getBoolean("enabled"))
                    .build(),
                username == null ? "" : username.trim().toLowerCase(java.util.Locale.ROOT)
            );
            if (result.isEmpty()) throw new UsernameNotFoundException("Unknown account");
            return result.get(0);
        };
    }

    @Bean
    AuthenticationManager smsAuthenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain smsSecurityFilterChain(HttpSecurity http, JdbcTemplate jdbc) throws Exception {
        http
          .csrf(csrf -> csrf
              .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
              .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/sms.html", "/sms.css", "/sms.js", "/login.html", "/favicon.ico",
                  "/assets/**", "/css/**", "/js/**", "/*.css", "/*.js",
                  "/api/auth/csrf", "/api/auth/bootstrap", "/api/auth/bootstrap-status", "/api/auth/login").permitAll()
              .requestMatchers("/api/auth/**").authenticated()
              .requestMatchers("/", "/index.html", "/app.js", "/styles.css").hasRole("ADMIN")
              .requestMatchers("/api/teacher/**").hasAnyRole("TEACHER", "ADMIN")
              .requestMatchers("/api/**").hasRole("ADMIN")
              .anyRequest().authenticated())
          .formLogin(form -> form.disable())
          .httpBasic(basic -> basic.disable())
          .exceptionHandling(errors -> errors
              .authenticationEntryPoint((req, res, ex) -> {
                  res.setStatus(HttpStatus.UNAUTHORIZED.value());
                  res.setContentType("application/json");
                  res.getWriter().write("{\"error\":\"Login required\"}");
              })
              .accessDeniedHandler((req, res, ex) -> {
                  res.setStatus(HttpStatus.FORBIDDEN.value());
                  res.setContentType("application/json");
                  res.getWriter().write("{\"error\":\"Not allowed for this account\"}");
              }))
          .addFilterAfter(new SmsAccountStatusFilter(jdbc), SecurityContextHolderFilter.class)
          .logout(logout -> logout.logoutUrl("/api/auth/logout")
              .invalidateHttpSession(true).deleteCookies("JSESSIONID")
              .logoutSuccessHandler((req, res, auth) -> res.setStatus(204)));
        return http.build();
    }
}
