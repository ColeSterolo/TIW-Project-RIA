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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import online_auctionsRIA.beans.UserBean;
import online_auctionsRIA.dao.AuctionDAO;
import online_auctionsRIA.dao.OfferDAO;
import online_auctionsRIA.utils.ConnectionHandler;

@WebServlet("/PostOffer")
@MultipartConfig
public class PostOffer extends HttpServlet {

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


	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		UserBean user = (UserBean) req.getSession().getAttribute("user");
		String auctionStr = req.getParameter("auction");
		String amountStr = req.getParameter("amount") ;
		int auction;
		int amount;
		int maxOffer;
		int minBid;
		
		if (user.getUsername() == null || auctionStr == null || amountStr == null || 
				user.getUsername().isEmpty() || auctionStr.isEmpty() || amountStr.isEmpty()) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing value");
			return;
		}
		
		auction = Integer.parseInt(auctionStr);
		amount = Integer.parseInt(amountStr);
		OfferDAO offerDAO = new OfferDAO(connection);
		AuctionDAO auctionDAO = new AuctionDAO(connection);
		
		
		try {
			if(auctionDAO.isExpired(auction)) {
				resp.sendError(HttpServletResponse.SC_FORBIDDEN, 
						"This auction is expired");
				return;
			}
			maxOffer = offerDAO.getMaxOffer(auction);
			minBid = auctionDAO.getMinBid(auction);
			if(amount - maxOffer < minBid) {
				resp.sendError(HttpServletResponse.SC_FORBIDDEN,
						"This offer is too low: the next offer needs to be at least " + (maxOffer + minBid));
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Error verifying the validity of the new offer into the DB");
			return;
		}		
				
		
		try {
			offerDAO.insertOffer(amount, auction, user.getUserId());
		} catch (SQLException e) {
			e.printStackTrace();
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Error inserting the new offer into the DB");
			return;
		}
		
		resp.setStatus(HttpServletResponse.SC_OK);


	}

}
