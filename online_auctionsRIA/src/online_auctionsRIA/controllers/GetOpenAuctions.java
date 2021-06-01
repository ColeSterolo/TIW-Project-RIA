package online_auctionsRIA.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import online_auctionsRIA.dao.OfferDAO;
import online_auctionsRIA.beans.Offer;
import online_auctionsRIA.beans.UserBean;
import online_auctionsRIA.dao.AuctionDAO;
import online_auctionsRIA.utils.AuctionJoinItem;
import online_auctionsRIA.utils.ConnectionHandler;

@WebServlet("/GetOpenAuctions")
public class GetOpenAuctions extends HttpServlet{

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
	
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		
		OfferDAO offerDAO = new OfferDAO(connection);
		AuctionDAO auctionDAO = new AuctionDAO(connection);
		List<AuctionJoinItem> openAuctions = new ArrayList<AuctionJoinItem>();
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpSession session = req.getSession();
		UserBean user = (UserBean) session.getAttribute("user");
		
		try {
			openAuctions = auctionDAO.getOpenAuctionsJoinItem(user);
		}catch(SQLException e) {
			
			//for debugging
			e.printStackTrace();
			
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Error in the retrieval of open auctions");
			return;
		}
		
		
		
		if (openAuctions != null) {

			Instant loginTime = (Instant) session.getAttribute("loginTime");

			for (AuctionJoinItem a : openAuctions) {
				
				if (loginTime.isAfter(a.getAuction().getEndingTime())) {
					a.setRemainingHours(0);
					a.setRemainingHours(0);
					a.setRemainingMinutes(0);

				} else {

					int minutes = (int) loginTime.until(a.getAuction().getEndingTime(), ChronoUnit.MINUTES);

					int hours = minutes / 60;
					int days = hours / 24;

					if (days > 0) {
						hours = hours % 24;
						minutes = minutes % 60;
					} else if (hours > 0) {
						minutes = minutes % 60;
					}

					a.setRemainingDays(days);
					a.setRemainingHours(hours);
					a.setRemainingMinutes(minutes);
				}
				
				Offer maxOffer = null;
				try {
					maxOffer = offerDAO.getAuctionMaxOffer(a.getAuction().getAuctionId());
				} catch (SQLException e) {
										
					response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
					response.getWriter().println("Error in the retrieval of max offer");
					return;
				}

				if (maxOffer != null)
					a.setMaxOffer(maxOffer.getOfferId());
				else
					a.setMaxOffer(-1);
			
			}
		}
		
		Gson gson = new GsonBuilder().setDateFormat("yyyy MMM dd").create();
		String json = gson.toJson(openAuctions);
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		
	}
		
	
}
