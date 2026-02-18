package controller;

import java.io.IOException;
import java.io.PrintWriter;

import model.User;
import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import security.PasswordUtil;
import security.TokenUtil;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
       
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();

        String name = req.getParameter("name");
        String pass = req.getParameter("password");

        // 1. Find the user in the MySQL database 
        User existingUser = UserDAO.findByName(name);

        if (existingUser != null) {
            boolean isMatch = PasswordUtil.verifypassword(pass, existingUser.getPassword());

            if (isMatch) {
            	
                String token = TokenUtil.generateToken(name);

                res.setHeader("Authorization", "Bearer " + token);
                
                out.print("{");
                out.print("\"status\": \"success\",");
                out.print("\"message\": \"Login Successful\",");
                out.print("\"token\": \"" + token + "\"");
                out.print("}");
            } else {
                // 401 Unauthorized for wrong password
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{ \"status\": \"error\", \"message\": \"Invalid Password!\" }");
            }
        } else {
            // 404 Not Found for non-existent username
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{ \"status\": \"error\", \"message\": \"User not found!\" }");
        }
        
        out.flush();
    }
}