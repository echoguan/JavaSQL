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
import com.ibm.mfp.adapter.api.OAuthSecurity;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.sql.*;


@Path("/API")
//@OAuthSecurity(enabled=false)
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
		PreparedStatement getAllUsers = con.prepareStatement("SELECT * FROM lesson.student_account");
		ResultSet data = getAllUsers.executeQuery();
		
		while(data.next()){
			JSONObject item = new JSONObject();
			item.put("student_id", data.getString("student_id"));
			item.put("student_name", data.getString("student_name"));
			item.put("student_password", data.getString("student_password"));
			results.add(item);
		}

		getAllUsers.close();
		con.close();

		return results;
	}
	
	/**
	 * 学生登录验证
	 * @param name 学生用户名
	 * @param password 学生密码
	 * @return 登录状态，字符串类型
	 * @throws SQLException
	 */
	@GET
	@Path("/loginConfirm/{studentName}/{studentPassword}")
	public String loginConfirm(
			@PathParam(value="studentName") String name,
			@PathParam(value="studentPassword") String password) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement loginConfirm = con.prepareStatement("SELECT student_password FROM lesson.student_account WHERE student_name = ?");
		
		try{
	    	loginConfirm.setString(1, name);
	    	ResultSet data = loginConfirm.executeQuery();
	    	if(data.first()){
	    		String rightPassString = data.getString("student_password");
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
	
	/**
	 * 得到登录学生的ID，以供之后各项操作使用
	 * @param name 学生用户名
	 * @return 学生ID,或-1代表查询失败
	 * @throws SQLException
	 */
	@GET
	@Path("/getStudentID/{studentName}")
	public int getStudentID(
			@PathParam(value="studentName") String name) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement loginConfirm = con.prepareStatement("SELECT student_id FROM lesson.student_account WHERE student_name = ?");
		
		try{
	    	loginConfirm.setString(1, name);
	    	ResultSet data = loginConfirm.executeQuery();
	    	if(data.first()){
	    		int studentID = data.getInt("student_id");
	    		return studentID;
	    	} else {
	    		return -1;
	    	}
	    }
	    finally{
	    	//Close resources in all cases
	    	loginConfirm.close();
	    	con.close();
	    }
	}
	
	@GET
	@Path("/registerStudent/{studentName}/{studentPassword}")
	public Response registerStudent(
			@PathParam(value="studentName") String name,
			@PathParam(value="studentPassword") String password) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement insertStudent = con.prepareStatement("insert into lesson.student_account (student_name,student_password) values (?,?)");
		
		try{
			insertStudent.setString(1, name);
			insertStudent.setString(2, password);
			insertStudent.executeUpdate();
	    	//Return a 200 OK
	    	return Response.ok().build();
	    }
		catch (SQLIntegrityConstraintViolationException violation) {
	        //Trying to create a user that already exists
	        return Response.status(Status.CONFLICT).entity(violation.getMessage()).build();
	    }
	    finally{
	        //Close resources in all cases
	    	insertStudent.close();
	        con.close();
	    }
	}
	
	/**
	 * 查询所有课程
	 * @return JSON格式的所有课程
	 * @throws SQLException
	 */
	@GET
	@Path("/getLesson")
	@Produces("application/json")
	public JSONArray getAllLesson() throws SQLException{
		JSONArray results = new JSONArray();
		Connection con = getSQLConnection();
		PreparedStatement getAllUsers = con.prepareStatement("SELECT * FROM lesson.lessontable");
		ResultSet data = getAllUsers.executeQuery();
		
		while(data.next()){
			JSONObject item = new JSONObject();
			item.put("id", data.getString("lessontable_id"));
			item.put("lessontable_name", data.getString("lessontable_name"));
			item.put("lessontable_description", data.getString("lessontable_description"));
			item.put("lessontable_key", data.getString("lessontable_key"));
			results.add(item);
		}

		getAllUsers.close();
		con.close();

		return results;
	}
	
	/**
	 * 学生订阅某课程
	 * @return 订阅结果，字符串类型，有订阅成功、已订阅过、订阅失败。
	 * @throws SQLException
	 */
//	@GET
//	@Path("/collectLesson/{lessonName}/{studentName}")
//	public String collectLesson(
//			@PathParam(value="lessonName") String lessonName,
//			@PathParam(value="studentName") String studentName) throws SQLException{
//		Connection con = getSQLConnection();
//		PreparedStatement insertStudent = con.prepareStatement("insert into lesson.student_account (student_name,student_password) values (?,?)");
//		
//		try{
//			insertStudent.setString(1, lessonName);
//			insertStudent.setString(2, studentName);
//			insertStudent.executeUpdate();
//	    	//Return a 200 OK
//	    	return Response.ok().build();
//	    }
//		catch (SQLIntegrityConstraintViolationException violation) {
//	        //Trying to create a user that already exists
//	        return Response.status(Status.CONFLICT).entity(violation.getMessage()).build();
//	    }
//	    finally{
//	        //Close resources in all cases
//	    	insertStudent.close();
//	        con.close();
//	    }
//	}

}
