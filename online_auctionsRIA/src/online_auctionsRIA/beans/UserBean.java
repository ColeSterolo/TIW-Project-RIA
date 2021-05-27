package online_auctionsRIA.beans;


public class UserBean {

	private Integer userId;
	private String username;
	private String password;
	private String address;
		
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	//userId getter and setter
	public Integer getUserId() {
		return userId;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	//username getter and setter
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	//password getter and setter
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


}

