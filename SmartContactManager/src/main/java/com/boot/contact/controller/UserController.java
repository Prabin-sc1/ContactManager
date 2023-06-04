package com.boot.contact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.boot.contact.dao.ContactRepository;
import com.boot.contact.dao.UserRepository;
import com.boot.contact.entities.Contact;
import com.boot.contact.entities.User;
import com.boot.contact.helper.MyMessage;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String name = principal.getName();
		System.out.println("User name " + name);
		User user = this.userRepository.getUserByUserName(name);
		System.out.println("USER " + user);
		model.addAttribute("user", user);
	}

	@RequestMapping("/index")
	public String index(Model model) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact";
	}

	// processing contact
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam() MultipartFile img,
			Principal principal, HttpSession session) {
		try {

			String name = principal.getName();
			User user = userRepository.getUserByUserName(name);

			// processing and uploading image
			if (img.isEmpty()) {
				// if image file is empty then write your message
				System.out.println("file is empty");
			} else {
				// upload the file to folder and update the name to contact
				contact.setImage(img.getOriginalFilename());
				File tempFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(tempFile.getAbsolutePath() + File.separator + img.getOriginalFilename());
				Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}

			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
//			System.out.println("Data " + contact);
//			System.out.println("Added to database");
			session.setAttribute("msg", new MyMessage("Contact Added", "success"));
		} catch (Exception e) {
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();
			session.setAttribute("msg", new MyMessage("Unable to add Contact", "danger"));
		}
		System.out.println(img.getOriginalFilename());
		return "normal/add_contact";

	}

	// show contacts handler

	@GetMapping("/show-contact")
	public String showContact(Model m, Principal p) {
		m.addAttribute("title", "Show user contacts");
		/*
		 * this is one of the way to get list of contact but one thing we have to keep
		 * in mind is we have to manage pagination too so there is more better way than
		 * this so why not String name = p.getName(); User tempUser =
		 * this.userRepository.getUserByUserName(name); List<Contact> contacts =
		 * tempUser.getContacts(); m.addAttribute("contact",contacts);
		 */
		String name = p.getName();
		User tempUser = this.userRepository.getUserByUserName(name);
		List<Contact> contacts = this.contactRepository.findContactByUser(tempUser.getId());
		m.addAttribute("contacts", contacts);
		return "normal/show_contact";
	}

}
