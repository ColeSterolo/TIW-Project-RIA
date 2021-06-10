package online_auctionsRIA.beans;

import java.time.Instant;

public class AuctionBean {
	
	private int auctionId;
	private int vendor;
	private Instant startingTime;
	private Instant endingTime;
	private Boolean closedFlag;
	private int initialPrice;
	private int minimumBid;
	private int item;
	
	public int getItem() {
		return item;
	}

	public void setItem(int item) {
		this.item = item;
	}

	//auctionId getter and setter
	public int getAuctionId() {
		return auctionId;
	}
	
	public void setAuctionId(int auctionId) {
		this.auctionId = auctionId;
	}
	
	//vendor getter and setter
	public int getVendor() {
		return vendor;
	}
	
	public void setVendor(int vendor) {
		this.vendor = vendor;
	}
	
	//startingTime getter and setter
	public Instant getStartingTime() {
		return startingTime;
	}
	
	public void setStartingTime(Instant startingTime) {
		this.startingTime = startingTime;
	}
	
	//edingTime getter and setter
	public Instant getEndingTime() {
		return endingTime;
	}
	
	public void setEndingTime(Instant endingTime) {
		this.endingTime = endingTime;
	}
	
	//closedFlag getter and setter
	public Boolean getClosedFlag() {
		return closedFlag;
	}
	
	public void setClosedFlag(Boolean closedFlag) {
		this.closedFlag = closedFlag;
	}
	
	//initialPrice getter and setter
	public int getInitialPrice() {
		return initialPrice;
	}
	
	public void setInitialPrice(int initialPrice) {
		this.initialPrice = initialPrice;
	}
	
	//minimumBid getter and setter
	public int getMinimumBid() {
		return minimumBid;
	}
	
	public void setMinimumBid(int minimumBid) {
		this.minimumBid = minimumBid;
	}
}
