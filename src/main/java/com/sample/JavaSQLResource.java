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
	
	/**
	 * 查询学生是否已订阅过某课程
	 * @return 查询结果
	 * @throws SQLException
	 */
	@GET
	@Path("/isStudentName/{studentName}")
	public int isStudentName(@PathParam(value="studentName") String studentName) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement isStudentName = con.prepareStatement("SELECT student_id FROM lesson.student_account where student_name = ?");
		
		try{
			isStudentName.setString(1, studentName);
			ResultSet data = isStudentName.executeQuery();
	    	if(data.first()){
	    		int student_id = data.getInt("student_id");
	    		return student_id;
	    	} else {
	    		return -1;
	    	}
	    }
		catch (SQLIntegrityConstraintViolationException violation) {
	        //Trying to create a user that already exists
			return -2;
	    }
	    finally{
	        //Close resources in all cases
	    	isStudentName.close();
	        con.close();
	    }
	}
	
	/**
	 * 学生注册
	 * @param name 用户名
	 * @param password 密码
	 * @return 注册结果
	 * @throws SQLException
	 */
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
	@Path("/getAllLesson")
	@Produces("application/json")
	public JSONArray getAllLesson() throws SQLException{
		JSONArray results = new JSONArray();
		Connection con = getSQLConnection();
		PreparedStatement getAllLesson = con.prepareStatement("select lessontable_id, lessontable_name, lessontable_description, lessontable_key, teacher_name from (select teacher_id,teacher_name from lesson.teacher_account) t , (select * from lesson.lessontable) l where t.teacher_id=l.teacher_id");
		ResultSet data = getAllLesson.executeQuery();
		
		while(data.next()){
			JSONObject item = new JSONObject();
			item.put("id", data.getString("lessontable_id"));
			item.put("lessontable_name", data.getString("lessontable_name"));
			item.put("lessontable_description", data.getString("lessontable_description"));
			item.put("teacher_name", data.getString("teacher_name"));
			item.put("lessontable_key", data.getString("lessontable_key"));
			results.add(item);
		}

		getAllLesson.close();
		con.close();

		return results;
	}
	
	/**
	 * 查询某学生已订阅的所有课程
	 * @return JSON格式的所有课程
	 * @throws SQLException
	 */
	@GET
	@Path("/getMyCollectLesson/{studentID}")
	@Produces("application/json")
	public JSONArray getMyCollectLesson(@PathParam(value="studentID") String studentID) throws SQLException{
		JSONArray results = new JSONArray();
		Connection con = getSQLConnection();
		PreparedStatement getMyCollectLesson = con.prepareStatement("select lessontable_id, lessontable_name, lessontable_description, lessontable_key, teacher_name from (select teacher_id,teacher_name from lesson.teacher_account) t , (select * from lesson.lessontable) l where t.teacher_id=l.teacher_id and lessontable_id in (select lessontable_id  from lesson.collect_lesson where student_id = ?)");
		getMyCollectLesson.setString(1, studentID);
		ResultSet data = getMyCollectLesson.executeQuery();
		
		while(data.next()){
			JSONObject item = new JSONObject();
			item.put("id", data.getString("lessontable_id"));
			item.put("lessontable_name", data.getString("lessontable_name"));
			item.put("lessontable_description", data.getString("lessontable_description"));
			item.put("teacher_name", data.getString("teacher_name"));
			item.put("lessontable_key", data.getString("lessontable_key"));
			results.add(item);
		}

		getMyCollectLesson.close();
		con.close();

		return results;
	}
	
	/**
	 * 学生订阅某课程
	 * @return 订阅结果
	 * @throws SQLException
	 */
	@GET
	@Path("/collectLesson/{lessonID}/{studentID}")
	public Response collectLesson(
			@PathParam(value="lessonID") String lessonID,
			@PathParam(value="studentID") String studentID) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement collectLesson = con.prepareStatement("insert into lesson.collect_lesson (student_id, lessontable_id) values (?,?)");
		
		try{
			collectLesson.setString(1, studentID);
			collectLesson.setString(2, lessonID);
			collectLesson.executeUpdate();
	    	//Return a 200 OK
	    	return Response.ok().build();
	    }
		catch (SQLIntegrityConstraintViolationException violation) {
	        //Trying to create a user that already exists
	        return Response.status(Status.CONFLICT).entity(violation.getMessage()).build();
	    }
	    finally{
	        //Close resources in all cases
	    	collectLesson.close();
	        con.close();
	    }
	}
	
	/**
	 * 查询学生是否已订阅过某课程
	 * @return 查询结果
	 * @throws SQLException
	 */
	@GET
	@Path("/isCollect/{lessonID}/{studentID}")
	public int isCollect(
			@PathParam(value="lessonID") String lessonID,
			@PathParam(value="studentID") String studentID) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement isCollect = con.prepareStatement("SELECT collect_lesson_id FROM lesson.collect_lesson where student_id=? and lessontable_id=?");
		
		try{
			isCollect.setString(1, studentID);
			isCollect.setString(2, lessonID);
			ResultSet data = isCollect.executeQuery();
	    	if(data.first()){
	    		int collect_lesson_id = data.getInt("collect_lesson_id");
	    		return collect_lesson_id;
	    	} else {
	    		return -1;
	    	}
	    }
		catch (SQLIntegrityConstraintViolationException violation) {
	        //Trying to create a user that already exists
			return -2;
	    }
	    finally{
	        //Close resources in all cases
	    	isCollect.close();
	        con.close();
	    }
	}
	
	/**
	 * 取消订阅
	 * @param lessonID 取消订阅课程编号
	 * @param studentID 取消订阅学生编号
	 * @return 取消订阅是否成功
	 * @throws SQLException
	 */
	@DELETE
	@Path("/deleteCollect/{lessonID}/{studentID}")
	public Response deleteCollect(
			@PathParam(value="lessonID") String lessonID,
			@PathParam(value="studentID") String studentID) throws SQLException{
	    Connection con = getSQLConnection();
	    PreparedStatement getCollect = con.prepareStatement("SELECT * FROM lesson.collect_lesson where student_id=? and lessontable_id=?");

	    try{
	    	getCollect.setString(1, studentID);
	    	getCollect.setString(2, lessonID);
	        ResultSet data = getCollect.executeQuery();

	        if(data.first()){
	            PreparedStatement deleteCollect = con.prepareStatement("DELETE FROM lesson.collect_lesson WHERE student_id = ? and lessontable_id = ?");
	            deleteCollect.setString(1, studentID);
	            deleteCollect.setString(2, lessonID);
	            deleteCollect.executeUpdate();
	            deleteCollect.close();
	            return Response.ok().build();

	        } else{
	            return Response.status(Status.NOT_FOUND).entity("User not found...").build();
	        }
	    }
	    finally{
	        //Close resources in all cases
	    	getCollect.close();
	        con.close();
	    }

	}
	
	/**
	 * 查询某课程的所有公告
	 * @return JSON格式的所有公告
	 * @throws SQLException
	 */
	@GET
	@Path("/getLessonNotice/{lessonID}")
	@Produces("application/json")
	public JSONArray getLessonNotice(@PathParam(value="lessonID") String lessonID) throws SQLException{
		JSONArray results = new JSONArray();
		Connection con = getSQLConnection();
		PreparedStatement getLessonNotice = con.prepareStatement("SELECT * FROM lesson.lesson_notice where lessontable_id = ?");
		getLessonNotice.setString(1, lessonID);
		ResultSet data = getLessonNotice.executeQuery();
		
		while(data.next()){
			JSONObject item = new JSONObject();
			item.put("lesson_notice_id", data.getString("lesson_notice_id"));
			item.put("lessontable_id", data.getString("lessontable_id"));
			item.put("lesson_notice_description", data.getString("lesson_notice_description"));
			item.put("lesson_notice_time", data.getString("lesson_notice_time"));
			results.add(item);
		}

		getLessonNotice.close();
		con.close();

		return results;
	}

	
	/**
	 * 查询某课程的所有提问
	 * @return JSON格式的所有提问
	 * @throws SQLException
	 */
	@GET
	@Path("/getLessonQuestion/{lessonID}")
	@Produces("application/json")
	public JSONArray getLessonQuestion(@PathParam(value="lessonID") String lessonID) throws SQLException{
		JSONArray results = new JSONArray();
		Connection con = getSQLConnection();
		PreparedStatement getLessonQuestion = con.prepareStatement("select lesson_question_id, lesson_question_title, lesson_question_description, lesson_question_time, student_name from (select student_id,student_name from lesson.student_account) s , (select * from lesson.lesson_question) q where s.student_id=q.student_id and lessontable_id in (select lessontable_id  from lesson.lesson_question where lessontable_id = ?)");
		getLessonQuestion.setString(1, lessonID);
		ResultSet data = getLessonQuestion.executeQuery();
		
		while(data.next()){
			JSONObject item = new JSONObject();
			item.put("lesson_question_id", data.getString("lesson_question_id"));
			item.put("student_name", data.getString("student_name"));
			item.put("lesson_question_title", data.getString("lesson_question_title"));
			item.put("lesson_question_description", data.getString("lesson_question_description"));
			item.put("lesson_question_time", data.getString("lesson_question_time"));
			results.add(item);
		}

		getLessonQuestion.close();
		con.close();

		return results;
	}
	
	/**
	 * 查询某一个提问的具体内容
	 * @return JSON格式的所有提问
	 * @throws SQLException
	 */
	@GET
	@Path("/getOneQuestion/{questionID}")
	@Produces("application/json")
	public JSONObject getOneQuestion(@PathParam(value="questionID") String questionID) throws SQLException{
		JSONObject item = new JSONObject();
		Connection con = getSQLConnection();
		PreparedStatement getOneQuestion = con.prepareStatement("select lesson_question_title, lesson_question_description, lesson_question_time, student_name from (select student_id,student_name from lesson.student_account) s , (select * from lesson.lesson_question) q where s.student_id=q.student_id and lesson_question_id in (select lesson_question_id  from lesson.lesson_question where lesson_question_id = ?)");
		getOneQuestion.setString(1, questionID);
		ResultSet data = getOneQuestion.executeQuery();
		
		while(data.next()){
			item.put("lesson_question_title", data.getString("lesson_question_title"));
			item.put("lesson_question_description", data.getString("lesson_question_description"));
			item.put("lesson_question_time", data.getString("lesson_question_time"));
			item.put("student_name", data.getString("student_name"));
		}

		getOneQuestion.close();
		con.close();

		return item;
	}
	
	@POST
	@Path("/addQuestion/{lessonID}/{studentID}/{questionTitle}/{questionDescription}/{questionTime}")
	public Response addQuestion(
			@PathParam(value="lessonID") String lessonID,
			@PathParam(value="studentID") String studentID,
			@PathParam(value="questionTitle") String questionTitle,
			@PathParam(value="questionDescription") String questionDescription,
			@PathParam(value="questionTime") String questionTime) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement addQuestion = con.prepareStatement("insert into lesson.lesson_question (lessontable_id,student_id,lesson_question_title,lesson_question_description,lesson_question_time) values (?,?,?,?,?)");
		
		try{
			addQuestion.setString(1, lessonID);
			addQuestion.setString(2, studentID);
			addQuestion.setString(3, questionTitle);
			addQuestion.setString(4, questionDescription);
			addQuestion.setString(5, questionTime);
			addQuestion.executeUpdate();
	    	//Return a 200 OK
	    	return Response.ok().build();
	    }
		catch (SQLIntegrityConstraintViolationException violation) {
	        //Trying to create a user that already exists
	        return Response.status(Status.CONFLICT).entity(violation.getMessage()).build();
	    }
	    finally{
	        //Close resources in all cases
	    	addQuestion.close();
	        con.close();
	    }
	}
}
