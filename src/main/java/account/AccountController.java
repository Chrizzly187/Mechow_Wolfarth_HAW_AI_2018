package account;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Controller
public class AccountController {
	
	  @Value("${spring.datasource.url}")
	  private String dbUrl;

	  @Autowired
	  private DataSource dataSource;

	@PostMapping("/account")
	@ResponseBody
	public String registerAccount(@Valid @ModelAttribute("Account")Account account, Map<String, Object> model) {
		
	    try (Connection connection = dataSource.getConnection()) {
	        Statement stmt = connection.createStatement();
	        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users(userName varchar(255) UNIQUE, userMail varchar(255) UNIQUE, "
	        				+ "	password varchar(255), CONSTRAINT ux_name_mail UNIQUE (userName, userMail))");	   
	        try {
		        stmt.executeUpdate("INSERT INTO users(userName, userMail, password) "
				+ "VALUES ('"+account.getUserName()+"', '"+account.getUserMail()+"', '"+account.getPassword()+"')"
				+ "ON CONFLICT (userName, userMail) DO NOTHING");
			} catch (Exception e) {
				return "Username or email already in use.";
			}	      
	        
	        return "Created User "+account.getUserName()+" with email "+account.getUserMail();
	      
	      } catch (Exception e) {
	        model.put("message", e.getMessage());
	        StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        return sw.toString();
	      }
    }
	
	@GetMapping("/account")
	@ResponseBody
	public String getUser(@CookieValue(value = "login", defaultValue = "false") String loginBool, 
						  @CookieValue(value = "user", defaultValue = "") String loginUser)	{
		String userName = "Not logged in.";
		if(loginBool.equals("true")) {
			try (Connection connection = dataSource.getConnection()) {
				Statement stmt = connection.createStatement();
		        
				 ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE userMail='"+loginUser+"'");
				 if(rs.isBeforeFirst()) {
					 rs.next();
					 userName = rs.getString("userName");
				 }				 
			} catch (SQLException e) {
		        StringWriter sw = new StringWriter();
		        e.printStackTrace(new PrintWriter(sw));
		        return sw.toString();
			}
		}		
		return userName;
	}
	
	@PostMapping("/account/login")
	@ResponseBody
	public String login(@Valid @ModelAttribute("Account")Account account,
			@CookieValue(value = "login", defaultValue = "false") String loginBool, 
			@CookieValue(value = "user", defaultValue = "") String loginUser,  HttpServletResponse response) {
		
		if(loginBool.equals("false") || !loginUser.equals(account.getUserMail())) {			
			try (Connection connection = dataSource.getConnection()) {
				Statement stmt = connection.createStatement();
		        
				 ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE userMail='"+account.getUserMail()+"'");
				 
				 if(rs.isBeforeFirst()) {
					 rs.next();
					 if(rs.getString("password").equals(account.getPassword())) {
						 Cookie loginCookie = new Cookie("login", "true");
						 Cookie userCookie = new Cookie("user", account.getUserMail());
						 userCookie.setMaxAge(600);
						 loginCookie.setMaxAge(600);
						 response.addCookie(userCookie);
						 response.addCookie(loginCookie);
					 } else {
						 return "Invalid password";
					 }
				 } else {
					 return "Invalid email";
				 }
		        
			} catch (Exception e) {
		        StringWriter sw = new StringWriter();
		        e.printStackTrace(new PrintWriter(sw));
		        return sw.toString();
			}
		} else {
			return "already logged in";
		}
		return "login";		
	}
	
	
	@PostMapping("/account/logout")
	@ResponseBody
	public String logout(@Valid @ModelAttribute("Account")Account account,
			@CookieValue(value = "login", defaultValue = "false") String loginBool, 
			@CookieValue(value = "user", defaultValue = "") String loginUser,  HttpServletResponse response) {
				
		if(loginBool.equals("true") && loginUser.equals(account.getUserMail())) {
			 Cookie loginCookie = new Cookie("login", "false");
			 Cookie userCookie = new Cookie("user", "");
			 response.addCookie(userCookie);
			 response.addCookie(loginCookie);
			 return "User "+account.getUserMail()+" logged out.";
		}
		return "Not logged in.";
	}

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }
	
}
