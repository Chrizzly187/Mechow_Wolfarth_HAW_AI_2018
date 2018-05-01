package account;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AccountController {

	@PostMapping("/account")
	public String registerAccount(@Valid @ModelAttribute("Account")Account account) {
		System.out.println(account.getUserName());
		System.out.println(account.getUserMail());
		System.out.println(account.getPassword());
		return "";
    }
	
}
