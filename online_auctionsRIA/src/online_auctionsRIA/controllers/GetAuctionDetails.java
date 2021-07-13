package online_auctionsRIA.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
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
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import online_auctionsRIA.dao.AuctionDAO;
import online_auctionsRIA.dao.OfferDAO;
import online_auctionsRIA.exceptions.DbIncoherenceException;
import online_auctionsRIA.utils.AuctionJoinItem;
import online_auctionsRIA.utils.ConnectionHandler;
import online_auctionsRIA.utils.OfferJoinUser;

@WebServlet("/GetOpenAuctionDetails")
public class GetAuctionDetails extends HttpServlet{

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
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		AuctionDAO auctionDAO = new AuctionDAO(connection);
		OfferDAO offerDAO = new OfferDAO(connection);
		AuctionJoinItem auction = null;
		List<OfferJoinUser> offers = new ArrayList<OfferJoinUser>();
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpSession session = req.getSession();
		
		Integer auctionId = null;
		try {
			auctionId = Integer.parseInt(request.getParameter("auctionId"));
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid auction parameter");
			return;
		}
		
		try {
			auction = auctionDAO.getAuctionJoinItem(auctionId);
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Error in the retrieval of auction join item");
			return;
		} catch (DbIncoherenceException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println(e.getMessage());
			return;
		}
		
		Instant loginTime = (Instant) session.getAttribute("loginTime");
		auction.getAuction().setRemainingTime(loginTime);
		
		try {
			offers = offerDAO.getOffersJoinUser(auctionId);
		} catch (SQLException e) {
			
			//for debugging
			e.printStackTrace();
			
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Error in the retrieval of offers");
			return;
		}
		

		Gson gson = new GsonBuilder().setDateFormat("yyyy MMM dd").create();
		String json = gson.toJson(offers);
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		
	}
	
}
