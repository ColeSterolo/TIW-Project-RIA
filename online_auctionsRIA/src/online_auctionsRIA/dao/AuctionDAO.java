package online_auctionsRIA.dao;


import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import online_auctionsRIA.beans.AuctionBean;
import online_auctionsRIA.beans.UserBean;
import online_auctionsRIA.exceptions.DbIncoherenceException;
import online_auctionsRIA.utils.AuctionJoinItem;

public class AuctionDAO {
	private Connection con;

	public AuctionDAO(Connection connection) {
		this.con = connection;

	}

	public int insertNewAuction(int vendor,Instant startingTime, Instant endingTime, int initialPrice, int minBid) throws SQLException {

		String query = "INSERT into auction (vendor, startingTime, endingTime, "
				+ "closedFlag, initialPrice, minimumBid) VALUES(?, ?, ?, ?, ?, ?)";
		int code = 0;

		try (PreparedStatement pstatement = con.prepareStatement(query);) {

			Timestamp startTimestamp = Timestamp.from(startingTime);
			Timestamp endTimestamp = Timestamp.from(endingTime);

			pstatement.setInt(1, vendor);
			pstatement.setTimestamp(2, startTimestamp);
			pstatement.setTimestamp(3, endTimestamp);
			pstatement.setBoolean(4, false);
			pstatement.setInt(5, initialPrice);
			pstatement.setInt(6, minBid);
			code = pstatement.executeUpdate();
		}

		return code;

	}

	public List<AuctionJoinItem> getOpenAuctionsJoinItem(UserBean user) throws SQLException {

		String query = "SELECT * FROM auction JOIN item ON auctionId = auction WHERE closedFlag = 0 AND vendor = ?";
		List <AuctionJoinItem> openAuctions = new ArrayList<AuctionJoinItem>();

		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, user.getUserId());
			try(ResultSet result = pstatement.executeQuery();) {

				while (result.next()) {
					AuctionJoinItem auction = new AuctionJoinItem();
					auction.getAuction().setAuctionId(result.getInt("auctionId"));
					Timestamp timestamp = result.getTimestamp("endingTime");
					Instant instant = timestamp.toInstant();
					auction.getAuction().setEndingTime(instant);
					auction.getAuction().setInitialPrice(result.getInt("initialPrice"));
					auction.getAuction().setMinimumBid(result.getInt("minimumBid"));

					auction.getItem().setDescription(result.getString("description"));
					auction.getItem().setItemId(result.getInt("itemId"));
					auction.getItem().setName(result.getString("name"));

					auction.getItem().setImage(Base64.getMimeEncoder().encodeToString(result.getBytes("image")));

					openAuctions.add(auction);				
				}
			}
		}
		if(openAuctions.isEmpty())
			return null;
		else
			return openAuctions;
	}


	public List<AuctionJoinItem> getClosedAuctionsJoinItem(UserBean user) throws SQLException {

		String query = "SELECT * FROM auction JOIN item ON auctionId = auction WHERE closedFlag != 0 AND vendor = ?";
		List <AuctionJoinItem> closedAuctions = new ArrayList<AuctionJoinItem>();

		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, user.getUserId());
			try(ResultSet result = pstatement.executeQuery();) {

				while (result.next()) {
					AuctionJoinItem auction = new AuctionJoinItem();
					auction.getAuction().setAuctionId(result.getInt("auctionId"));
					Timestamp timestamp = result.getTimestamp("endingTime");
					Instant instant = timestamp.toInstant();
					auction.getAuction().setEndingTime(instant);
					auction.getAuction().setInitialPrice(result.getInt("initialPrice"));
					auction.getAuction().setMinimumBid(result.getInt("minimumBid"));

					auction.getItem().setDescription(result.getString("description"));
					auction.getItem().setItemId(result.getInt("itemId"));
					auction.getItem().setName(result.getString("name"));

					auction.getItem().setImage(Base64.getMimeEncoder().encodeToString(result.getBytes("image")));

					closedAuctions.add(auction);				
				}
			}
		}
		if(closedAuctions.isEmpty())
			return null;
		else
			return closedAuctions;
	}


	public void closeAuction (int auctionId) throws SQLException {		
		String query = "UPDATE auction SET closedFlag = 1 WHERE auctionId = ?";
		con.setAutoCommit(false);

		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, auctionId);
			pstatement.executeUpdate();
			con.commit();
		} catch (SQLException e) {
			con.rollback();
			throw e;
		} 
	}

	public List<AuctionBean> searchAuction (String keyword) throws SQLException {
		String query = 
				"SELECT * FROM auction JOIN item ON auctionId = auction "
						+ "WHERE closedFlag = 0 "
						+ "AND endingTime > (SELECT NOW())"
						+ "AND itemId IN ("
						+ "SELECT itemId FROM item "
						+ "WHERE (name LIKE CONCAT('%', ? ,'%')	OR description LIKE CONCAT('%', ? ,'%') ) ) "	
						+ "ORDER BY endingTime DESC ";

		List<AuctionBean> openAuctions = new ArrayList<AuctionBean>();

		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, keyword);
			pstatement.setString(2, keyword);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					AuctionBean auction = new AuctionBean();
					auction.setAuctionId(result.getInt("auctionId"));
					auction.setVendor(result.getInt("vendor"));
					auction.setStartingTime(result.getTimestamp("startingTime").toInstant());
					auction.setEndingTime(result.getTimestamp("endingTime").toInstant());
					auction.setClosedFlag(result.getBoolean("closedFlag"));
					auction.setInitialPrice(result.getInt("initialPrice"));
					auction.setMinimumBid(result.getInt("minimumBid"));

					openAuctions.add(auction);
				}
			}
		}
		return openAuctions;
	}


	public int getAuctionId(Instant startingTime, int user) throws SQLException, DbIncoherenceException {

		Timestamp startTimestamp = Timestamp.from(startingTime);
		String query = "SELECT auctionId FROM auction where startingTime = ? AND vendor = ?";
		int auctionId = -1;

		try (PreparedStatement pstatement = con.prepareStatement(query);) {

			pstatement.setTimestamp(1, startTimestamp);
			pstatement.setInt(2, user);

			try(ResultSet result = pstatement.executeQuery();) {
				if(result.next()) 
					auctionId = result.getInt("auctionId");
				if(result.next())
					throw new DbIncoherenceException("Two different items have the same auction id");
			}
		}

		return auctionId;
	}


	public AuctionJoinItem getAuctionJoinItem(int auctionId) throws SQLException, DbIncoherenceException {


		AuctionJoinItem auction = null;
		String query = "SELECT * FROM auction JOIN item ON auctionId = auction WHERE auctionId = ?";

		try (PreparedStatement pstatement = con.prepareStatement(query);) {

			pstatement.setInt(1, auctionId);

			try(ResultSet result = pstatement.executeQuery();) {
				if(result.next()) {

					auction = new AuctionJoinItem();
					auction.getAuction().setAuctionId(result.getInt("auctionId"));
					Timestamp timestamp = result.getTimestamp("endingTime");
					Instant instant = timestamp.toInstant();
					auction.getAuction().setEndingTime(instant);
					auction.getAuction().setInitialPrice(result.getInt("initialPrice"));
					auction.getAuction().setMinimumBid(result.getInt("minimumBid"));
					auction.getAuction().setVendor(result.getInt("vendor"));

					auction.getItem().setDescription(result.getString("description"));
					auction.getItem().setItemId(result.getInt("itemId"));
					auction.getItem().setName(result.getString("name"));
					auction.getItem().setImage(Base64.getMimeEncoder().encodeToString(result.getBytes("image")));
				}

				if(result.next())
					throw new DbIncoherenceException("Two different items have the same auction id");
			}
			return auction;
		}
	}

	public Boolean isExpired(int auctionId) throws SQLException {
		String query = "SELECT * FROM auction "
				+ "WHERE auctionId = ? AND endingTime > (SELECT NOW())";

		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, auctionId);
			try(ResultSet result = pstatement.executeQuery();) {
				if(result.next()) {
					return false;
				}
				return true;
			}
		}
	}

	public int getMinBid(int auctionId) throws SQLException {
		String query = "SELECT minimumBid FROM auction "
				+ "WHERE auctionId = ? ";
		int minBid = 0;
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, auctionId);
			try(ResultSet result = pstatement.executeQuery();) {
				if(result.next()) {
					minBid = result.getInt("minimumBid");
				}			
			}
		}
		return minBid;
	}
	
	public List<AuctionBean> getAuctionsById(List<Integer> ids) throws SQLException{
		
		int idCounter;
		List<AuctionBean> auctions = new ArrayList<AuctionBean>();
		
		if(ids != null && !ids.isEmpty()) {
			String queryString = "SELECT * FROM auction WHERE closedFlag = false AND auctionId in (?";
			
			for(idCounter = 1; idCounter < ids.size(); idCounter++) {
				queryString = queryString + ", ?";
			}
			queryString = queryString + ")";
			
			try (PreparedStatement pstatement = con.prepareStatement(queryString);) {
				for(idCounter = 0; idCounter < ids.size(); idCounter++) {
					pstatement.setInt(idCounter + 1, ids.get(idCounter));
				}
				
				try(ResultSet result = pstatement.executeQuery();) {
					while(result.next()) {
						AuctionBean auction = new AuctionBean();
						auction.setAuctionId(result.getInt("auctionId"));
						auction.setVendor(result.getInt("vendor"));
						auction.setStartingTime(result.getTimestamp("startingTime").toInstant());
						auction.setEndingTime(result.getTimestamp("endingTime").toInstant());
						auction.setClosedFlag(result.getBoolean("closedFlag"));
						auction.setInitialPrice(result.getInt("initialPrice"));
						auction.setMinimumBid(result.getInt("minimumBid"));

						auctions.add(auction);
					}
				}
				
				if(!auctions.isEmpty()) 
					return auctions;
				else 
					return null;
				
			}
		}
		else return null;
	}
	
	public int getInitialPrice(int auctionId) throws SQLException {
		String query = "SELECT initialPrice FROM auction "
				+ "WHERE auctionId = ? ";
		int initialPrice = 0;
		
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, auctionId);
			try(ResultSet result = pstatement.executeQuery();) {
				if(result.next()) {
					initialPrice = result.getInt("initialPrice");
				}			
			}
		}
		return initialPrice;
	}

}

