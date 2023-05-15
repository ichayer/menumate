package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.exception.UserNotFoundException;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.TokenService;
import ar.edu.itba.paw.service.UserService;
import ar.edu.itba.paw.webapp.form.EmailForm;
import ar.edu.itba.paw.webapp.form.RegisterForm;
import ar.edu.itba.paw.webapp.form.ResetPasswordForm;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import javax.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @RequestMapping(value = "/auth/register", method = RequestMethod.GET)
    public ModelAndView registerForm(@ModelAttribute("registerForm") final RegisterForm registerForm) {
        return new ModelAndView("auth/register");
    }

    @RequestMapping(value = "/auth/register", method = RequestMethod.POST)
    public ModelAndView register(
            @Valid @ModelAttribute("registerForm") final RegisterForm registerForm,
            final BindingResult errors
    ) {
        if (errors.hasErrors()) {
            return registerForm(registerForm);
        }

        final User user = userService.create(registerForm.getEmail(), registerForm.getPassword(), registerForm.getName());
        try {
            tokenService.generateVerificationToken(user.getUserId());
        } catch (MessagingException e) {
            return new ModelAndView("redirect:/auth/login?error=mailer_error");
        }
        return new ModelAndView("redirect:/auth/login?type=verify-emailed");
    }

    @RequestMapping(value = "/auth/login", method = RequestMethod.GET)
    public ModelAndView loginForm(@RequestParam(value = "type", required = false) String type) {
        return new ModelAndView("auth/login").addObject("type", type);
    }

    @RequestMapping(value = {"/auth/{actionType}"}, method = RequestMethod.GET)
    public ModelAndView getEmailForm(
            @PathVariable("actionType") final String actionType,
            @RequestParam(value = "token", required = false) @Length(min = 32, max = 32) final String token,
            @ModelAttribute("emailForm") final EmailForm emailForm
    ) {
        if (!actionType.matches("verify|reset-password"))
            return new ModelAndView("redirect:/errors/404");

        if (token != null) {
            if (actionType.equals("verify") && tokenService.verifyUserAndDeleteVerificationToken(token))
                return new ModelAndView("redirect:/auth/login?type=verified");
            else if (actionType.equals("reset-password") && tokenService.isValidResetPasswordToken(token))
                return new ModelAndView("redirect:/auth/reset-password-form?token=" + token);
            else
                return new ModelAndView("redirect:/auth/login?error=request-error");
        } else {
            return new ModelAndView("auth/email_form")
                    .addObject("actionType", actionType)
                    .addObject("url", "/auth/" + actionType);
        }
    }

    @RequestMapping(value = "/auth/{actionType}", method = RequestMethod.POST)
    public ModelAndView sendToken(
            @PathVariable("actionType") final String actionType,
            @Valid @ModelAttribute("emailForm") final EmailForm emailForm,
            final BindingResult errors
    ) {
        if (!actionType.matches("verify|reset-password"))
            return new ModelAndView("redirect:/errors/404");

        if (errors.hasErrors()) {
            return getEmailForm(actionType, null, emailForm);
        }

        try {
            User user = userService.getByEmail(emailForm.getEmail()).orElseThrow(UserNotFoundException::new);

            if (actionType.equals("verify")) {
                if (user.getIsActive()) {
                    return new ModelAndView("redirect:/auth/login?type=" + actionType + "-emailed");
                }
                tokenService.generateVerificationToken(user.getUserId());
            } else {
                tokenService.generatePasswordResetToken(user.getUserId());
            }
            return new ModelAndView("redirect:/auth/login?type=" + actionType + "-emailed");
        } catch (UserNotFoundException e) {
            return new ModelAndView("redirect:/auth/login?type=" + actionType + "-emailed");
        } catch (MessagingException e) {
            return new ModelAndView("redirect:/auth/login?error=mailer_error");
        }
    }

    @RequestMapping(value = "/auth/reset-password-form", method = RequestMethod.GET)
    public ModelAndView resetPasswordForm(
            @RequestParam(value = "token", required = false) @Length(min = 32, max = 32) final String token,
            @ModelAttribute("resetPasswordForm") final ResetPasswordForm resetPasswordForm
    ) {
        if (token != null && tokenService.isValidResetPasswordToken(token))
            return new ModelAndView("auth/reset_password").addObject("token", token);
        else
            return new ModelAndView("redirect:/auth/login?error=mailer_error");
    }

    @RequestMapping(value = "/auth/reset-password-form", method = RequestMethod.POST)
    public ModelAndView resetPassword(
            @RequestParam(value = "token", required = true) @Length(min = 32, max = 32) final String token,
            @Valid @ModelAttribute("resetPasswordForm") final ResetPasswordForm resetPasswordForm,
            final BindingResult errors
    ) {
        if (errors.hasErrors()) {
            return resetPasswordForm(token, resetPasswordForm);
        }

        String newPassword = userService.encodePassword(resetPasswordForm.getPassword());

        if (tokenService.updatePasswordAndDeleteResetPasswordToken(token, newPassword)) {
            return new ModelAndView("redirect:/auth/login?type=reset-password-success");
        } else {
            return new ModelAndView("redirect:/auth/login?error=password-reset-error");
        }
    }
}
