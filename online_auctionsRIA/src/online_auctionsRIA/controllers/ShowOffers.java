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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import online_auctionsRIA.beans.Item;
import online_auctionsRIA.beans.Offer;
import online_auctionsRIA.dao.ItemDAO;
import online_auctionsRIA.dao.OfferDAO;
import online_auctionsRIA.utils.ConnectionHandler;

@WebServlet("/ShowOffers")
public class ShowOffers extends HttpServlet {

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
		OfferDAO offerDAO = new OfferDAO(connection);
		ItemDAO itemDAO = new ItemDAO(connection);
		String reqAuction = request.getParameter("auction");
		
		List<Item> items = new ArrayList<Item>();
		List<Offer> offers = new ArrayList<Offer>();
		
		try {
			items = itemDAO.getItems(Integer.parseInt(reqAuction));
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Error retrieving the item from DB");	
			return;
		} catch (NumberFormatException nfe) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid non-numeric input");
			return;
		}
		
		if (items == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().println("No auction with such id");
			return;
		}
		
		try {
			offers = offerDAO.getOffers(Integer.parseInt(reqAuction));
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Error retrieving the offers from DB");
			return;
		}
		
		
		Gson gson = new GsonBuilder().setDateFormat("yyyy MMM dd").create();
		String jsonItems = gson.toJson(items);
		String jsonOffers = gson.toJson(offers);
		
 		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		String json = "[" + jsonItems + ", " + jsonOffers + "]";
		response.getWriter().write(json);
	}


}
