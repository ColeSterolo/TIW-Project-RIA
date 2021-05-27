package online_auctionsRIA.beans;


import java.time.Instant;

public class Offer {
	
	private int offerId;
	
	private int bidder;
	
	private int auction;
	
	private int amount;
	
	private Instant datetime;
	
	public int getBidder() {
		return bidder;
	}

	public void setBidder(int bidder) {
		this.bidder = bidder;
	}

	public int getAuction() {
		return auction;
	}

	public void setAuction(int auction) {
		this.auction = auction;
	}

	
	public int getOfferId() {
		return offerId;
	}

	public void setOfferId(int offerId) {
		this.offerId = offerId;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public Instant getDatetime() {
		return datetime;
	}

	public void setDatetime(Instant datetime) {
		this.datetime = datetime;
	}
	
}
