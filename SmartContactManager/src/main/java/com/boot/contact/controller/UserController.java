package com.boot.contact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
				contact.setImage("eagle.jpg");
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

	@GetMapping("/show-contact/{page}")
	public String showContact(@PathVariable("page") Integer page, Model m, Principal p) {
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
		Pageable of = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepository.findContactByUser(tempUser.getId(), of);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contact";
	}

	// working of single contact
	@RequestMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cid, Model model, Principal p) {
		System.out.println("CID " + cid);
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		String name = p.getName();
		User user = this.userRepository.getUserByUserName(name);
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_detail";
	}

	// deleting contact
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, HttpSession session) {
//		Optional<Contact> optionalContact = this.contactRepository.findById(cid);
//		Contact contact = optionalContact.get()
		Contact contact = this.contactRepository.findById(cid).get();
		contact.setUser(null);
//		int id = contact.getUser().getId();
		this.contactRepository.delete(contact);
		session.setAttribute("msg", new MyMessage("Contact deleted successfully ", "success"));
		return "redirect:/user/show-contact/0";

	}

	// code for opening update form
	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid, Model m) {
		m.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);
		return "normal/update_form";
	}

	// update contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("img") MultipartFile img, Model model,
			HttpSession session, Principal principal) {
		try {
			Contact oldContact = this.contactRepository.findById(contact.getCid()).get();
			if (!img.isEmpty()) {
				// delete old one
				File deleteFile = new ClassPathResource("static/img").getFile();
				File f = new File(deleteFile, oldContact.getImage());
				f.delete();
				// add new one
				File tempFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(tempFile.getAbsolutePath() + File.separator + img.getOriginalFilename());
				Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(img.getOriginalFilename());
			} else {
				contact.setImage(oldContact.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("msg", new MyMessage("Your contact is updated successfully!", "success"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/"+contact.getCid()+"/contact";
	}

}
