/**
* Copyright 2016 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.sample;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.mfp.adapter.api.AdaptersAPI;
import com.ibm.mfp.adapter.api.ConfigurationAPI;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.sql.*;


@Path("/API")
public class JavaSQLResource {
	/*
	 * For more info on JAX-RS see https://jax-rs-spec.java.net/nonav/2.0-rev-a/apidocs/index.html
	 */

	@Context
	ConfigurationAPI configurationAPI;

	@Context
	AdaptersAPI adaptersAPI;

	public Connection getSQLConnection() throws SQLException{
		// Create a connection object to the database
		JavaSQLApplication app = adaptersAPI.getJaxRsApplication(JavaSQLApplication.class);
		return app.dataSource.getConnection();
	}
	
		
	@GET
	@Path("/teacherLogin")
	@Produces("application/json")
	public JSONArray getAllTeachers() throws SQLException{
		JSONArray results = new JSONArray();
		Connection con = getSQLConnection();
		PreparedStatement getAllUsers = con.prepareStatement("SELECT * FROM echo_lesson.teacher_account");
		ResultSet data = getAllUsers.executeQuery();
		
		while(data.next()){
			JSONObject item = new JSONObject();
			item.put("teacher_account_id", data.getString("teacher_account_id"));
			item.put("teacher_account_name", data.getString("teacher_account_name"));
			item.put("teacher_account_password", data.getString("teacher_account_password"));
			results.add(item);
		}

		getAllUsers.close();
		con.close();

		return results;
	}
	
	@GET
	@Path("/loginConfirm/{teacherName}/{teacherPassword}")
	public String loginConfirm(
			@PathParam(value="teacherName") String name,
			@PathParam(value="teacherPassword") String password) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement loginConfirm = con.prepareStatement("SELECT teacher_account_password FROM echo_lesson.teacher_account WHERE teacher_account_name = ?");
		
		try{
	    	loginConfirm.setString(1, name);
	    	ResultSet data = loginConfirm.executeQuery();
	    	if(data.first()){
	    		String rightPassString = data.getString("teacher_account_password");
	    		if( password.equals(rightPassString)) {
	    			return "Success";
	    		} else {
	    			return "PasswordWrong";
	    		}
	    	} else {
	    		return "NameWrong";
	    	}
	    }
	    finally{
	    	//Close resources in all cases
	    	loginConfirm.close();
	    	con.close();
	    }
	}

}
