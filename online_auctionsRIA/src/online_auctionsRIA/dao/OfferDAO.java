package online_auctionsRIA.dao;


import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import online_auctionsRIA.beans.*;
import online_auctionsRIA.utils.OfferItem;
import online_auctionsRIA.utils.OfferJoinUser;

public class OfferDAO {

	private Connection con;

	public OfferDAO(Connection con) {
		super();
		this.con = con;
	}

	public List<Offer> getOffers(int auction) throws SQLException {

		List<Offer> offers = new ArrayList<Offer>();
		Offer offer = null;
		String query = "SELECT * FROM offer WHERE offer.auction = ? ORDER BY offerTimestamp DESC";

		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, auction);

			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					offer = new Offer();
					offer.setOfferId(result.getInt("offerId"));
					offer.setAmount(result.getInt("amount"));
					offer.setAuction(result.getInt("auction"));
					offer.setBidder(result.getInt("bidder"));
					offer.setDatetime(result.getTimestamp("offerTimestamp").toInstant());
					offers.add(offer);
				}
			}
		}

		if (!offers.isEmpty())
			return offers;
		else
			return null;
	}


	public int insertOffer(int amount, int auction, int bidder) throws SQLException{
		String query = "INSERT into offer (amount, auction, bidder, winningOffer, offerTimestamp)   "
				+ "VALUES(?, ?, ?, ?, NOW())";
		int code = 0;
		PreparedStatement pstatement = null;
		
		try {
			pstatement = con.prepareStatement(query);

			pstatement.setInt(1, amount);
			pstatement.setInt(2, auction);
			pstatement.setInt(3, bidder);
			pstatement.setBoolean(4, false);
			
			code = pstatement.executeUpdate();
			
		}catch(SQLException e) {
			throw new SQLException(e);
		}finally {
			try {
				pstatement.close();
				}catch(Exception e1) {
					e1.printStackTrace();
				}
			}
		return code;
	}

	
	public List<OfferItem> getWinningOffers(int userId) throws SQLException {

		Offer offer = null;
		Item item = null;
		OfferItem offerItem = null;
		List<OfferItem> offers = new ArrayList<OfferItem>();
		String query = "SELECT * FROM offer off JOIN item itm ON off.auction = itm.auction "
				+ "WHERE bidder = ? "
				+ "AND off.auction IN (SELECT auctionId FROM auction WHERE closedFlag = 1) "
				+ "AND off.amount >= (SELECT max(amount) FROM offer off2 JOIN item itm2 on off2.auction = itm2.auction WHERE off2.auction = off.auction)";

		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, userId);

			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					offer = new Offer();
					item = new Item();
					offerItem = new OfferItem();
					
					offer.setOfferId(result.getInt("offerId"));
					offer.setAmount(result.getInt("amount"));
					offer.setAuction(result.getInt("auction"));
					offer.setBidder(result.getInt("bidder"));
					offer.setDatetime(result.getTimestamp("offerTimestamp").toInstant());
					
					item.setItemId(result.getInt("itemId"));
					item.setDescription(result.getString("description"));
					byte[] imgData = result.getBytes("image");
					String encodedImg=Base64.getEncoder().encodeToString(imgData);
					item.setImage(encodedImg);
					item.setName(result.getString("name"));
					
					offerItem.setItem(item);
					offerItem.setOffer(offer);
					
					offers.add(offerItem);
				}
			}
		}
		
		if(offers.isEmpty()) 
			return null;
		else 
			return offers;
	}
	
	
	public List<OfferJoinUser> getOffersJoinUser(int auction) throws SQLException {

		List<OfferJoinUser> offers = new ArrayList<OfferJoinUser>();
		OfferJoinUser offer = null;
		String query = "SELECT * FROM offer JOIN user_table ON bidder = userId WHERE offer.auction = ? ORDER BY offerTimestamp DESC";

		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, auction);

			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					offer = new OfferJoinUser();
					offer.getOffer().setOfferId(result.getInt("offerId"));
					offer.getOffer().setAmount(result.getInt("amount"));
					offer.getOffer().setAuction(result.getInt("auction"));
					offer.getOffer().setBidder(result.getInt("bidder"));
					
					offer.getOffer().setDatetime(result.getTimestamp("offerTimestamp").toInstant());
					
					offer.getUser().setUserId(result.getInt("userId"));
					offer.getUser().setUsername(result.getString("username"));
					offer.getUser().setAddress(result.getString("address"));
					offers.add(offer);
				}
			}
		}

		if (!offers.isEmpty())
			return offers;
		else
			return null;
	}
	
	public Offer getMaxOffer (int auction) throws SQLException {
		Offer offer = null;
		String query = "SELECT * FROM offer "
				+ "WHERE auction = ? AND amount >= (SELECT max(amount) FROM offer WHERE auction = ?)";

		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, auction);
			pstatement.setInt(2, auction);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.next()) {
					offer = new Offer();
					offer.setOfferId(result.getInt("offerId"));
					offer.setAmount(result.getInt("amount"));
					offer.setAuction(result.getInt("auction"));
					offer.setBidder(result.getInt("bidder"));
					offer.setDatetime(result.getTimestamp("offerTimestamp").toInstant());
				}
				return offer;
			}
		}
	}	
	
	public OfferJoinUser getAuctionMaxOfferJoinUser(int auctionId) throws SQLException {

		OfferJoinUser offer = null;
		
		String query = "SELECT * FROM offer JOIN user_table ON bidder = userId WHERE auction = ? ORDER BY amount DESC";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, auctionId);
			try (ResultSet result = pstatement.executeQuery();) {
				if(result.next()) {
					
					offer = new OfferJoinUser();
					offer.getOffer().setOfferId(result.getInt("offerId"));
					offer.getOffer().setAmount(result.getInt("amount"));
					offer.getOffer().setAuction(result.getInt("auction"));
					offer.getOffer().setBidder(result.getInt("bidder"));
					offer.getOffer().setDatetime(result.getTimestamp("offerTimestamp").toInstant());
					
					offer.getUser().setUserId(result.getInt("userId"));
					offer.getUser().setUsername(result.getString("username"));
					
				}
			}
		}
		return offer;
	}

	
	
	public void setWinningFlag (int auctionId) throws SQLException {
		
			String query1 = "SELECT max(amount) FROM offer where auction = ?";
			int amount = -1;
			String query = "UPDATE offer SET winningOffer = 1 "
					+ "WHERE auction = ? "
					+ "AND amount = ?";
			
		try (PreparedStatement pstatement = con.prepareStatement(query1);) {
			pstatement.setInt(1, auctionId);
			
			try (ResultSet result = pstatement.executeQuery();) {
				if(result.next())
					amount = result.getInt("max(amount)");
			}
		}
		if(amount != -1) {
			try (PreparedStatement pstatement = con.prepareStatement(query);) {
				pstatement.setInt(1, auctionId);
				pstatement.setInt(2, amount);
				pstatement.executeUpdate();
			}
		}
		
		else throw new SQLException("error in setting of winning flag on offers");
		
	}

}
