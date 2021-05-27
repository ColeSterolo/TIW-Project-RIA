package online_auctionsRIA.utils;

import online_auctionsRIA.beans.Item;
import online_auctionsRIA.beans.Offer;

public class OfferItem {
	
	private Offer offer;
	private Item item;
	
	public Offer getOffer() {
		return offer;
	}
	public void setOffer(Offer offer) {
		this.offer = offer;
	}
	public Item getItem() {
		return item;
	}
	public void setItem(Item item) {
		this.item = item;
	}
	

}
