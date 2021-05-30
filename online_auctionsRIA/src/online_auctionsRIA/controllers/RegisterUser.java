package online_auctionsRIA.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import online_auctionsRIA.dao.UserDAO;
import online_auctionsRIA.utils.ConnectionHandler;

@WebServlet("/RegisterUser")
@MultipartConfig
public class RegisterUser extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public void init() throws ServletException {
		
		connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		
		String usrn = null;
		String pwd = null;
		String address = null;
		UserDAO userDAO = new UserDAO(connection);
		
		try {
			usrn = StringEscapeUtils.escapeJava(request.getParameter("newUsername"));
			pwd = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
			address = StringEscapeUtils.escapeJava(request.getParameter("address"));

			if (usrn == null || pwd == null || usrn.isEmpty() || pwd.isEmpty() || address == null || address.isEmpty()) {
				throw new Exception("Missing or empty credential value");
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		//hashing the user's password, so that it is not stored in clear in the database
		String password = String.valueOf(pwd.hashCode());
		int result = 0;
		
		try {
			result = userDAO.registerUser(usrn, password, address);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			return;
			}

		if(result == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		//String path = getServletContext().getContextPath() + "/GoToIndex";
		//response.sendRedirect(path);

		response.setStatus(HttpServletResponse.SC_OK);

		}
	
}
