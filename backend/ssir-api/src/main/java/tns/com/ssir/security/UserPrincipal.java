package tns.com.ssir.security;

import tns.com.ssir.core.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String userIdString;
    private final String username;
    private final String email;
    private final String password;
    private final String status;
    private final Long companyId;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String userIdString, String username, String email, 
                         String password, String status, Long companyId,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.userIdString = userIdString;
        this.username = username;
        this.email = email;
        this.password = password;
        this.status = status;
        this.companyId = companyId;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toList());

        Long companyId = user.getCompany() != null ? user.getCompany().getId() : null;

        return new UserPrincipal(
                user.getId(),
                user.getUserIdString(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus(),
                companyId,
                authorities
        );
    }

    public Long getId() { return id; }
    public String getUserIdString() { return userIdString; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
    public Long getCompanyId() { return companyId; }

    @Override
    public String getUsername() { return username; }

    @Override
    public String getPassword() { return password; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return !"LOCKED".equalsIgnoreCase(status); }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return "ACTIVE".equalsIgnoreCase(status); }
}