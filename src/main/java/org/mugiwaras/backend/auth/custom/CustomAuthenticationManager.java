package org.mugiwaras.backend.auth.custom;

import java.util.Collection;

import org.mugiwaras.backend.auth.IUserBusiness;
import org.mugiwaras.backend.auth.User;
import org.mugiwaras.backend.model.business.BusinessException;
import org.mugiwaras.backend.model.business.NotFoundException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomAuthenticationManager implements AuthenticationManager {

    public CustomAuthenticationManager(PasswordEncoder pEncoder, IUserBusiness userBusiness) {
        this.pEncoder = pEncoder;
        this.userBusiness = userBusiness;
    }

    private IUserBusiness userBusiness;

    private PasswordEncoder pEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        User user = null;
        try {
            user = userBusiness.load(username);
        } catch (NotFoundException e) {
            throw new BadCredentialsException("No user registered with this details");
        } catch (BusinessException e) {
            e.printStackTrace();
            throw new AuthenticationServiceException(e.getMessage());
        }
        String validation = user.validate();
        if (validation.equals(User.VALIDATION_ACCOUNT_EXPIRED))
            throw new AccountExpiredException(User.VALIDATION_ACCOUNT_EXPIRED);
        if (validation.equals(User.VALIDATION_CREDENTIALS_EXPIRED))
            throw new CredentialsExpiredException(User.VALIDATION_CREDENTIALS_EXPIRED);
        if (validation.equals(User.VALIDATION_DISABLED))
            throw new DisabledException(User.VALIDATION_DISABLED);
        if (validation.equals(User.VALIDATION_LOCKED))
            throw new LockedException(User.VALIDATION_LOCKED);

        //TODO: arreglar contraseña hasheada, metodo HMAC512 plis
        if (!pEncoder.matches(password, user.getPassword()))
            throw new BadCredentialsException("Invalid password");

        return new UsernamePasswordAuthenticationToken(user, null);

    }
    public Authentication AuthWrap(String name, String pass) {
        return new Authentication() {
            @Override
            public String getName() {
                return name;
            }
            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }
            @Override
            public boolean isAuthenticated() {
                return false;
            }
            @Override
            public Object getPrincipal() {
                return null;
            }
            @Override
            public Object getDetails() {
                return null;
            }
            @Override
            public Object getCredentials() {
                return pass;
            }
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }
        };
    }

}