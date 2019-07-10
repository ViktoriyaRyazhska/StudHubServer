package com.softserve.academy.studhub.security.controller;

import com.softserve.academy.studhub.constants.SuccessMessage;
import com.softserve.academy.studhub.entity.Role;
import com.softserve.academy.studhub.entity.enums.RoleName;
import com.softserve.academy.studhub.security.dto.*;
import com.softserve.academy.studhub.entity.User;
import com.softserve.academy.studhub.security.jwt.JwtProvider;
import com.softserve.academy.studhub.security.services.FacebookService;
import com.softserve.academy.studhub.security.services.GoogleVerifierService;
import com.softserve.academy.studhub.security.entity.ConfirmToken;
import com.softserve.academy.studhub.security.services.ConfirmTokenService;
import com.softserve.academy.studhub.service.EmailService;
import com.softserve.academy.studhub.service.RoleService;
import com.softserve.academy.studhub.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.util.HashSet;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final GoogleVerifierService googleVerifier;
    private final UserService userService;
    private final RoleService roleService;
    private final ConfirmTokenService confirmTokenService;
    private final EmailService emailService;
    private final PasswordEncoder encoder;
    private final JwtProvider jwtProvider;
    private final ModelMapper modelMapper;
    private FacebookService facebookService;


    @PostMapping("/signin")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginForm loginRequest) {

        userService.isUserActivated(loginRequest.getUsername());
        return authenticate(loginRequest);
    }

    @PostMapping("/signup")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signUpRequest) {

        User user = modelMapper.map(signUpRequest, User.class);
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRoles(new HashSet<Role>() {{
            add(roleService.findByName(RoleName.ROLE_USER));
        }});
        userService.add(user);

        ConfirmToken token = new ConfirmToken(user);
        confirmTokenService.save(token);
        emailService.sendConfirmAccountEmail(user, token);

        return ResponseEntity.ok(new MessageResponse(SuccessMessage.SENT_CONFIRM_ACC_LINK + user.getEmail()));
    }

    @PostMapping("/signinGoogle")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> authenticateGoogleUser(@Valid @RequestBody GoogleUserData userData) {

        LoginForm form = googleVerifier.authenticateUser(userData);

        return authenticate(form);
    }

    @PostMapping("/signinFacebook")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> authenticateFacebookUser(@Valid @RequestBody FacebookUserData userData) {
        System.out.println("in controller"+userData);
        LoginForm form = facebookService.authenticateUser(userData);

        return authenticate(form);
    }

    @PostMapping("/confirm-account")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> confirmAccount(@Valid @RequestBody ConfirmDto form) {

        ConfirmToken token = confirmTokenService.findByValidToken(form.getToken());

        User user = token.getUser();
        user.setIsActivated(true);
        userService.update(user);
        confirmTokenService.delete(token);

        return ResponseEntity.ok(new MessageResponse(SuccessMessage.CONFIRM_ACC));
    }

    private ResponseEntity<?> authenticate(LoginForm loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessTokenString = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        return ResponseEntity.ok(new JwtResponse(accessTokenString, refreshToken));
    }
}