package com.thejungleblog.notes.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.thejungleblog.notes.domain.UserEntity;
import com.thejungleblog.notes.form.Registration;
import com.thejungleblog.notes.persistence.IUserDao;

/**
 * Controller used for handling authentication and registration requests
 * 
 * @author marty
 *
 */
@Controller
public class AuthController {
	
	private static Logger logger = Logger.getLogger(AuthController.class);
	
	@Autowired
	private IUserDao userDao;
	
	@Autowired
    @Qualifier("registrationValidator")
    private Validator validator;
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout,
			Model model, HttpServletRequest req) {
	 
		if (error != null) {
			model.addAttribute("error", "Invalid username and password");
		}
 
		if (logout != null) {
			req.getSession().invalidate();
			model.addAttribute("msg", "You've been logged out successfully");
		}
		return "login";
 
	}
	 
	 @RequestMapping(value = "/logout", method = RequestMethod.GET)
	 public String getLogoutPage(Model model, HttpServletRequest req) {
		 req.getSession().invalidate();
		 
		 return "logout";
	 }
	 
	 @RequestMapping(value = "/register", method = RequestMethod.GET)
	 public String getRegister(Model model, HttpServletRequest req) {
		 
		 // provide a new registration form to the user
		 model.addAttribute("registration", new Registration());
		 
		 return "register";
	 }
	 
	 @RequestMapping(value = "/register", method = RequestMethod.POST)
	 public String postRegister(
			 @Valid Registration registration,
	         BindingResult bindingResult, Model model) {
		 
		 // validate the registration form submitted
		 validator.validate(registration, bindingResult);
		 if (bindingResult.hasErrors()) {
			 logger.info("Registration was unsuccessful. Displaying errors.");
			 return "register";
		 }
		 
		 // registration was successfully validated, now persist the new user's data
		 logger.info("Registration successful. Saving new user's information.");
		 
		 UserEntity newUser = registration.createUser();
		 userDao.addUser(newUser);
		 
		 logger.info("User successfully added.");

		 // redirect to the login page
		 return "redirect:login";
	 }
}
