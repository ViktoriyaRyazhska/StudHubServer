package com.softserve.academy.studhub.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@CrossOrigin
@RestController
public class TestRestAPIs {
	
	@GetMapping("/api/test/user")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public String userAccess() {
		return ">>> User Contents!";
	}

	@GetMapping("/api/test/moderator")
	@PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
	public String moderatorAccess() {
		return ">>> Moderator Contents";
	}

	@GetMapping("/api/test/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public String adminAccess() {
		return ">>> Admin Contents";
	}
}