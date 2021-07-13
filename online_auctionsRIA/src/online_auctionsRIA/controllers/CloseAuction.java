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

import online_auctionsRIA.beans.UserBean;
import online_auctionsRIA.exceptions.DbIncoherenceException;
import online_auctionsRIA.utils.AuctionJoinItem;
import online_auctionsRIA.dao.AuctionDAO;
import online_auctionsRIA.utils.ConnectionHandler;

@WebServlet("/CloseAuction")
public class CloseAuction extends HttpServlet {

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
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid parameters");
			return;
		}

		try {
			UserBean user = (UserBean) request.getSession().getAttribute("user");
			AuctionJoinItem auction = auctionDAO.getAuctionJoinItem(auctionId);
			if (auctionDAO.isExpired(auctionId) && auction.getAuction().getVendor() == user.getUserId()) {
				auctionDAO.closeAuction(auctionId);
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("This auction cannot be closed");
				return;
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Error in the closure of an auction in the db");
			return;
		} catch (DbIncoherenceException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println(e.getMessage());
			return;
		}

		response.setStatus(HttpServletResponse.SC_OK);

	}

}