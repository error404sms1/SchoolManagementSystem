package SchoolManagementSystem;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class SmsAuthController {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;
    private final PasswordEncoder passwords;
    private final AuthenticationManager manager;
    private final String bootstrapSecret;

    public SmsAuthController(JdbcTemplate jdbc, TransactionTemplate transaction,
            PasswordEncoder passwords, AuthenticationManager manager,
            @Value("${sms.bootstrap.secret:}") String bootstrapSecret) {
        this.jdbc = jdbc;
        this.transaction = transaction;
        this.passwords = passwords;
        this.manager = manager;
        this.bootstrapSecret = bootstrapSecret;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @GetMapping("/bootstrap-status")
    public Map<String, Boolean> bootstrapStatus() {
        Boolean finished = jdbc.queryForObject(
            "SELECT admin_bootstrap_completed FROM SYSTEM_SETUP WHERE setup_id=1", Boolean.class);
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM APP_USER WHERE role='ADMIN'", Integer.class);
        return Map.of("available", !Boolean.TRUE.equals(finished) && (count == null || count == 0));
    }

    // This endpoint creates ONLY the very first ADMIN, once, with a secret
    // from an environment variable. Further admins require admin login.
    @PostMapping("/bootstrap")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> bootstrap(@RequestHeader(value="X-Bootstrap-Secret", required=false) String secret,
                                         @RequestBody SmsDtos.Register request) {
        if (bootstrapSecret.isBlank() || secret == null ||
                !MessageDigest.isEqual(secret.getBytes(StandardCharsets.UTF_8),
                    bootstrapSecret.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bootstrap is not authorized");
        }
        String username = validUsername(request.username());
        String hashed = passwords.encode(validPassword(request.password()));
        return transaction.execute(status -> {
            jdbc.queryForObject("SELECT setup_id FROM SYSTEM_SETUP WHERE setup_id=1 FOR UPDATE", Integer.class);
            Boolean completed = jdbc.queryForObject(
                "SELECT admin_bootstrap_completed FROM SYSTEM_SETUP WHERE setup_id=1", Boolean.class);
            Integer admins = jdbc.queryForObject(
                "SELECT COUNT(*) FROM APP_USER WHERE role='ADMIN'", Integer.class);
            if (Boolean.TRUE.equals(completed) || (admins != null && admins != 0)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "An administrator already exists; ask an admin to create additional accounts");
            }
            jdbc.update("INSERT INTO APP_USER(username,password_hash,role) VALUES(?,?,'ADMIN')",
                username, hashed);
            jdbc.update("UPDATE SYSTEM_SETUP SET admin_bootstrap_completed=TRUE,completed_at=CURRENT_TIMESTAMP(6) WHERE setup_id=1");
            return Map.of("message", "First administrator registered", "username", username);
        });
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody SmsDtos.Login login, HttpServletRequest request, HttpServletResponse response) {
        if (login.username() == null || login.password() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        try {
            Authentication authentication = manager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                    login.username().trim().toLowerCase(Locale.ROOT), login.password()));
            // Prevent session fixation; save manually because login is JSON-based.
            if (request.getSession(false) != null) {
                request.changeSessionId();
            } else {
                request.getSession(true);
            }
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            new HttpSessionSecurityContextRepository().saveContext(context, request, response);
            return Map.of("username", authentication.getName(),
                "roles", authentication.getAuthorities().stream().map(Object::toString).toList());
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Invalid username or password");
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Invalid username or password");
        }
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of("username", authentication.getName(),
            "roles", authentication.getAuthorities().stream().map(Object::toString).toList());
    }

    public static String validUsername(String input) {
        if (input == null || !input.matches("[A-Za-z0-9._-]{3,80}"))
            throw new IllegalArgumentException("Username must be 3–80 letters, numbers, dots, _ or -");
        return input.toLowerCase(Locale.ROOT);
    }

    public static String validPassword(String password) {
        if (password == null || password.length() < 12 ||
                password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Password must be at least 12 characters and at most 72 UTF-8 bytes");
        return password;
    }
}
