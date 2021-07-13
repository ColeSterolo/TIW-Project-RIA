package online_auctionsRIA.utils;


import online_auctionsRIA.beans.Item;
import online_auctionsRIA.beans.UserBean;

public class AuctionJoinItem {
	
	private Item item;
	
	private AuctionUtil auction;
	
	private int remainingDays;
	
	private int remainingHours;
	
	private int remainingMinutes;
	
	private UserBean winner;
	
	private int maxOffer;
	
	public AuctionJoinItem() {
		item = new Item();
		auction = new AuctionUtil();
	}
	
	public int getMaxOffer() {
		return maxOffer;
	}

	public void setMaxOffer(int maxOffer) {
		this.maxOffer = maxOffer;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public AuctionUtil getAuction() {
		return auction;
	}

	public void setAuction(AuctionUtil auction) {
		this.auction = auction;
	}

	public UserBean getWinner() {
		return winner;
	}

	public void setWinner(UserBean winner) {
		this.winner = winner;
	}

	public int getRemainingDays() {
		return remainingDays;
	}

	public void setRemainingDays(int remainingDays) {
		this.remainingDays = remainingDays;
	}

	public int getRemainingHours() {
		return remainingHours;
	}

	public void setRemainingHours(int remainingHours) {
		this.remainingHours = remainingHours;
	}

	public int getRemainingMinutes() {
		return remainingMinutes;
	}

	public void setRemainingMinutes(int remainingMinutes) {
		this.remainingMinutes = remainingMinutes;
	}


}
