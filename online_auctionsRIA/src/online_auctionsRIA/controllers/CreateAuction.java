package online_auctionsRIA.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import online_auctionsRIA.beans.UserBean;
import online_auctionsRIA.dao.AuctionDAO;
import online_auctionsRIA.dao.ItemDAO;
import online_auctionsRIA.exceptions.DbIncoherenceException;
import online_auctionsRIA.exceptions.NotUniqueNameException;
import online_auctionsRIA.utils.ConnectionHandler;
import online_auctionsRIA.utils.ImageUtils;

@MultipartConfig
@WebServlet("/CreateAuction")
public class CreateAuction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	private Connection connection = null;


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

		UserBean userBean;
		String name = null;
		String description = null;
		String dateString = null;
		int price = 0;
		int minBid = 0;
		ItemDAO itemDAO = new ItemDAO(connection);
		AuctionDAO auctionDAO = new AuctionDAO(connection);
		Part image = null;
		Instant endingTime = null;
		Instant nowInstant = Instant.now();
		nowInstant = nowInstant.truncatedTo(ChronoUnit.SECONDS);
		nowInstant = nowInstant.plus(Duration.ofHours(2));
		
		try {
		name = request.getParameter("itemName");
		
		description = request.getParameter("description");
		
		image = request.getPart("picture");
		
		price =  Integer.parseInt(request.getParameter("initialPrice"));
		
		minBid =  Integer.parseInt(request.getParameter("minBid"));
		
		dateString = StringEscapeUtils.escapeJava(request.getParameter("expiryTime"));
	
		} catch(Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Error in the retrieval of form fields");
			return;
		}
		
		try {
			
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

		TemporalAccessor temporalAccessor = formatter.parse(dateString);
		LocalDateTime localDateTime = LocalDateTime.from(temporalAccessor);
		localDateTime = localDateTime.plus(Duration.ofHours(2));
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
		endingTime = Instant.from(zonedDateTime);
		
		} catch(Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Invalid date format");
			return;
		}
		
		
		if(endingTime.isBefore(nowInstant)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("The inserted date is not valid");
			return;
		}
			
		if (description == null)
			description = new String("");
		if (name == null || name.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Item name is invalid");
			return;
		}
		else {
			
			InputStream photoContent = image.getInputStream();
			byte[] photoByteArray = ImageUtils.readImage(photoContent);
			
			if (photoByteArray.length == 0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("The selected file is not valid");
				return;
			} else if (price <= 0) {
				
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("The inserted initial price is not valid");
				return;

			}else if(minBid < 0) {
				
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("The inserted minimum bid is not valid");
				return;
				
			} else { //checks on date missing

				HttpServletRequest req = (HttpServletRequest) request;
				HttpSession session = req.getSession();
				userBean = (UserBean) session.getAttribute("user");
				
				try {
					
					auctionDAO.insertNewAuction(userBean.getUserId(), nowInstant, endingTime, price, minBid);
					
					int auctionId = auctionDAO.getAuctionId(nowInstant, userBean.getUserId());
					if(auctionId == -1) throw new SQLException("user not found");
					
					itemDAO.insertItem(name, photoByteArray, description, userBean.getUserId(), auctionId);
					
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
					response.getWriter().println("Error in the database insertion");
					return;
				}catch(NotUniqueNameException e) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println("Item name already used");
					return;
				}catch(DbIncoherenceException e) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println(e.getMessage());
					return;
				}
			}
		}

		response.setStatus(HttpServletResponse.SC_OK);
	}

}
