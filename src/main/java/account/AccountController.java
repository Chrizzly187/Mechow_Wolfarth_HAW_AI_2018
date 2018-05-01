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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
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
	
	@PostMapping("/account/login")
	@ResponseBody
	public String login(@Valid @ModelAttribute("Account")Account account,
			@CookieValue(value = "login", defaultValue = "false") String login, HttpServletResponse response) {
		
		if(login.equals("false")) {			
			try (Connection connection = dataSource.getConnection()) {
				Statement stmt = connection.createStatement();
		        
				 ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE userMail='"+account.getUserMail()+"'");
				 
				 if(rs.isBeforeFirst()) {
					 if(rs.getString("password").equals(account.getPassword())) {
						 Cookie cookie = new Cookie("login", "true");
						 response.addCookie(cookie);
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
		}		
		return "login";		
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
