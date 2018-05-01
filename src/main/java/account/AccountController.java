package account;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import javax.sql.DataSource;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
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
		System.out.println(account.getUserName());
		System.out.println(account.getUserMail());
		System.out.println(account.getPassword());
		
		
	    try (Connection connection = dataSource.getConnection()) {
	        Statement stmt = connection.createStatement();
	        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (userName varchar(255), userMail varchar(255), password varchar(255))");
	        stmt.executeUpdate("INSERT INTO users VALUES (account.getUserName(), account.getUserMail(), account.getPassword)");
	        ResultSet rs = stmt.executeQuery("SELECT userName, userMail, password FROM users");

	        ArrayList<String> output = new ArrayList<String>();
	        while (rs.next()) {
	          output.add("Read from DB: " + rs.getString("userName"));
	          output.add("Read from DB: " + rs.getString("userMail"));
	          output.add("Read from DB: " + rs.getString("password"));
	        }
	        model.put("records", output);
	        return "db";
	      } catch (Exception e) {
	        model.put("message", e.getMessage());
	        return "error";
	      }
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
