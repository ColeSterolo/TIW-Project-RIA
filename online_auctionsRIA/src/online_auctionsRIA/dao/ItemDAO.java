package online_auctionsRIA.dao;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import online_auctionsRIA.beans.*;
import online_auctionsRIA.exceptions.NotUniqueNameException;

public class ItemDAO {

	private Connection con;

	public ItemDAO(Connection con) {
		super();
		this.con = con;
	}
	
	public int insertItem(String name, byte[] image, String description, int user, int auction) throws SQLException, NotUniqueNameException{
		
		if(getItemId(name, user) != -1)
			throw new NotUniqueNameException();
		

		String query = "INSERT into item (name, image, description, auction)   VALUES(?, ?, ?, ?)";
		int code = 0;
		PreparedStatement pstatement = null;
		
		try {
			pstatement = con.prepareStatement(query);

			pstatement.setString(1, name);
			pstatement.setBytes(2, image);  //To be checked
			pstatement.setString(3, description);
			pstatement.setInt(4, auction);
			
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
	
	public List<Item> getItems (int auctionId) throws SQLException {
		String query = "SELECT * FROM item WHERE auction = ?";
		List<Item> items = new ArrayList<Item>();
		
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, auctionId);

			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Item item = new Item();
					
					item.setItemId(result.getInt("itemId"));
					item.setDescription(result.getString("description"));
					byte[] imgData = result.getBytes("image");
					String encodedImg=Base64.getEncoder().encodeToString(imgData);
					item.setImage(encodedImg);
					item.setName(result.getString("name"));
					
					items.add(item);
				}
			}
		}	
		return items;
	
	} 
	
	//This method returns the item id given the item name and its owner id. Returns null if such item is not found
	public int getItemId(String name, int owner) throws SQLException{
		
		String queryString = "SELECT itemId FROM item JOIN auction WHERE name = ? AND vendor = ?";
		try (PreparedStatement pstatement = con.prepareStatement(queryString);) {
			pstatement.setString(1, name);
			pstatement.setInt(2, owner);
		
			try (ResultSet result = pstatement.executeQuery();){
				
				if(result.next())
					return result.getInt("itemId");
				else return -1;
				
			}
		}
	}
	
	
}
