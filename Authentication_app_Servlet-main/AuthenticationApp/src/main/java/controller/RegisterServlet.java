package controller;

import java.io.IOException;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import security.CryptoUtil;
import security.PasswordUtil;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
 
    public RegisterServlet() {
        super();
    }
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        res.setCharacterEncoding("UTF-8");
        
        // Simple verification message to confirm the servlet is accessible
        User user ;
        res.getWriter().println("Auth Service is online. Please use POST for Login or Registration.");
    }
    
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String name = req.getParameter("name");
		String email = req.getParameter("email");
		String pass = req.getParameter("password");
		
		try {
	        // 1. Hash the password 
	        String hashedPass = PasswordUtil.hashedPassword(pass);

	        // 2. Encrypt the email
	        String encryptedEmail = CryptoUtil.encrypt(email);

	        // 3. Create User object and save
	        
	    	   User newUser = new User(name, encryptedEmail, hashedPass);
		        UserDAO.save(newUser);

		        // 4. Send success response
		        res.getWriter().println("User Registered Successfully!");
		        res.setStatus(201);
		   
	    } 
		catch (Exception e) {
//	        res.setStatus(500);
	        res.getWriter().println("Registration Error: " + e.getMessage());
	    }
		
		
	
	}

}
