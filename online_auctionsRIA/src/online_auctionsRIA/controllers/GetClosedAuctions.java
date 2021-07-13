package online_auctionsRIA.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
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
import online_auctionsRIA.utils.OfferJoinUser;
import online_auctionsRIA.beans.UserBean;
import online_auctionsRIA.dao.AuctionDAO;
import online_auctionsRIA.utils.AuctionJoinItem;
import online_auctionsRIA.utils.ConnectionHandler;

@WebServlet("/GetClosedAuctions")
public class GetClosedAuctions extends HttpServlet{

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
		List<AuctionJoinItem> closedAuctions = new ArrayList<AuctionJoinItem>();	
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpSession session = req.getSession();
		UserBean user = (UserBean) session.getAttribute("user");
		
		try {
			closedAuctions = auctionDAO.getClosedAuctionsJoinItem(user);
		}catch(SQLException e) {
			
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			return;
		}
		
		if(closedAuctions != null) {
			for(AuctionJoinItem a : closedAuctions) {
				OfferJoinUser maxOffer = null;
				try {
					maxOffer = offerDAO.getAuctionMaxOfferJoinUser(a.getAuction().getAuctionId());
				} catch (SQLException e) {
										
					response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
					return;
				}
				if (maxOffer != null) {
					a.setMaxOffer(maxOffer.getOffer().getOfferId());
					a.setWinner(maxOffer.getUser());
				}
				else {
					a.setMaxOffer(-1);
					a.setWinner(null);
				}
			}
		}
		
		Gson gson = new GsonBuilder().setDateFormat("yyyy MMM dd").create();
		String json = gson.toJson(closedAuctions);
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		
	}
	
}
