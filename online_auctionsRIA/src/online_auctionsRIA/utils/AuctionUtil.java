package online_auctionsRIA.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import online_auctionsRIA.beans.AuctionBean;

public class AuctionUtil extends AuctionBean {
	
	private int remainingDays;
	
	private int remainingHours;
	
	private int remainingMinutes;
	
	public AuctionUtil() {
		
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
	
	public void setRemainingTime(Instant start) {
		if (start.isAfter(this.getEndingTime())) {
			setRemainingDays(0);
			setRemainingHours(0);
			setRemainingMinutes(0);
		} else {

			int minutes = (int) start.until(this.getEndingTime(), ChronoUnit.MINUTES);

			int hours = minutes / 60;
			int days = hours / 24;

			if (days > 0) {
				hours = hours % 24;
				minutes = minutes % 60;
			} else if (hours > 0) {
				minutes = minutes % 60;
			}

			setRemainingDays(days);
			setRemainingHours(hours);
			setRemainingMinutes(minutes);
		}
	}

}
