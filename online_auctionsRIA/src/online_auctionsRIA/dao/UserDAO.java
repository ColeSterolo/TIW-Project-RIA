package online_auctionsRIA.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.security.auth.login.CredentialException;

import online_auctionsRIA.beans.UserBean;

public class UserDAO {
	private Connection con;

	public UserDAO(Connection connection) {
		this.con = connection;
	}

	public int registerUser(String username, String password, String address) throws SQLException {
		int code = 0;
		String query = "INSERT into user_table (username, password, address)   VALUES(?, ?, ?)";

		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);

			pstatement.setString(1, username);
			pstatement.setString(2, password);
			pstatement.setString(3, address);

			code = pstatement.executeUpdate();

		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			try {
				pstatement.close();
			} catch (Exception e1) {
				throw e1;
			}
		}
		return code;
	}


	public UserBean checkCredentials(String username, String pword) throws SQLException, CredentialException {
		String query = "SELECT * FROM user_table WHERE username = ?";
		UserBean user = null;
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					if (pword.equalsIgnoreCase(result.getString("password")) ) {
						user = new UserBean();
						user.setUserId(result.getInt("userId"));
						user.setUsername(username);
						// user.setPassword(pword);
						return user;
					}

				}

			}
		}
		throw new CredentialException("Login failure: incorrect password");
	}

}
