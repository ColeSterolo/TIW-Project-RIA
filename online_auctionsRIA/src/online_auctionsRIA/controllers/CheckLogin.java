package online_auctionsRIA.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

import javax.security.auth.login.CredentialException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import online_auctionsRIA.beans.*;
import online_auctionsRIA.dao.UserDAO;
import online_auctionsRIA.utils.ConnectionHandler;

@WebServlet("/CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CheckLogin() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// obtain and escape parameters
		String usrn = null;
		String pwd = null;
		usrn = StringEscapeUtils.escapeJava(request.getParameter("username"));
		pwd = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
		if (usrn == null || pwd == null || usrn.isEmpty() || pwd.isEmpty() ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Credentials must be not null");
			return;
		}
		// query db to authenticate for user
		UserDAO userDao = new UserDAO(connection);
		UserBean user = null;
		String hashedPw = String.valueOf(pwd.hashCode());
		try {
			user = userDao.checkCredentials(usrn, hashedPw);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal server error, retry later");
			return;
		} catch (CredentialException e) {
			user = null;
		}

		// If the user exists, add info to the session and go to home page, otherwise
		// return an error status code and message
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Incorrect credentials");
		} else {
			
			Instant loginTime = Instant.now();
			loginTime = loginTime.plus(Duration.ofHours(2));
			
			request.getSession().setAttribute("user", user);
			request.getSession().setAttribute("loginTime", loginTime);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(usrn);
		}
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
