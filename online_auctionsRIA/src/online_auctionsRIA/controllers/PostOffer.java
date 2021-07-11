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
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().println("Missing value");
			return;
		}
		
		
		try {
			auction = Integer.parseInt(auctionStr);
			amount = Integer.parseInt(amountStr);
			OfferDAO offerDAO = new OfferDAO(connection);
			AuctionDAO auctionDAO = new AuctionDAO(connection);
			if(auctionDAO.isExpired(auction)) {
				resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
				resp.getWriter().println("This auction is expired");
				return;
			}
			minBid = auctionDAO.getMinBid(auction);
			try {
				maxOffer = offerDAO.getMaxOffer(auction).getAmount();
				if(amount - maxOffer < minBid) {
					resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
					resp.getWriter().println("This offer is too low: the next offer "
							+ "needs to be at least " + (maxOffer + minBid));
					return;
				}
			} catch (NullPointerException npe) {
				maxOffer = auctionDAO.getInitialPrice(auction);
				if(amount - maxOffer < minBid) {
					resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
					resp.getWriter().println("This offer is too low: the next offer "
							+ "needs to be at least " + (maxOffer + minBid));
					return;
				}
			}
				
			try {
				offerDAO.insertOffer(amount, auction, user.getUserId());
			} catch (SQLException e) {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				resp.getWriter().println("Error inserting the new offer into the DB");
				return;
			}
		} catch (SQLException e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp.getWriter().println("Error verifying the validity of the new offer into the DB");
			return;
		} catch (NumberFormatException nfe) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().println("Invalid non-numeric input");
			return;
		}		
		
		resp.setStatus(HttpServletResponse.SC_OK);


	}

}
