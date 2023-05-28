package com.boot.contact.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.boot.contact.dao.UserRepository;
import com.boot.contact.entities.User;
import com.boot.contact.helper.MyMessage;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home() {
		return "home";
	}

	@RequestMapping("/about")
	public String about() {
		return "about";
	}

	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("user", new User());
		return "signup";
	}

	@RequestMapping("/login")
	public String login() {
		return "login";
	}

	// handler for registering user
	@PostMapping("/register")
	public String registerUser(@ModelAttribute("user") User user,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {

		

		try {
			if (!agreement) {
				System.out.println("You haven't agreed terms and conditions.");
				throw new Exception("You haven't agreed terms and conditions.");
			}
			user.setRole("Role_user");
			user.setEnabled(true);

			System.out.println("Agreement " + agreement);
			System.out.println("User " + user);
			this.userRepository.save(user);
			model.addAttribute("user", new User());
			model.addAttribute("session", session);

			session.setAttribute("message", new MyMessage("Successfully Registered!", "alert-success"));
			return "signup";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new MyMessage("Something went wrong!" + e.getMessage(), "alert-danger"));
			return "signup";
		}

	}

}
