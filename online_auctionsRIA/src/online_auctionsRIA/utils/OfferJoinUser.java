package online_auctionsRIA.utils;

import online_auctionsRIA.beans.Offer;
import online_auctionsRIA.beans.UserBean;

public class OfferJoinUser {
	
	public OfferJoinUser() {
		this.user = new UserBean();
		this.offer = new Offer();
	}
	
	UserBean user;
	
	Offer offer;

	public UserBean getUser() {
		return user;
	}

	public void setUser(UserBean user) {
		this.user = user;
	}

	public Offer getOffer() {
		return offer;
	}

	public void setOffer(Offer offer) {
		this.offer = offer;
	}
	
	


}
