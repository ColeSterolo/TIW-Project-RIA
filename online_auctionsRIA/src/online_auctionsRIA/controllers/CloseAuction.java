package online_auctionsRIA.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import online_auctionsRIA.dao.AuctionDAO;
import online_auctionsRIA.utils.ConnectionHandler;

@WebServlet("/CloseAuction")
public class CloseAuction extends HttpServlet{
	
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
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		AuctionDAO auctionDAO = new AuctionDAO(connection);
		
		Integer auctionId = null;
		try {
			auctionId = Integer.parseInt(request.getParameter("auctionId"));
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid auction parameters");
			return;
		}
		

		try {
			auctionDAO.closeAuction(auctionId);
		} catch (SQLException e) {
		
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Could not close auction in the db");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
	
	}

}