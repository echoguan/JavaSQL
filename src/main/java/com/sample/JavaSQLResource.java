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
	 * 教师登录验证
	 * @param name 用户名
	 * @param password 密码
	 * @return 登录状态，字符串类型
	 * @throws SQLException
	 */
	@GET
	@Path("/teacherLoginConfirm/{teacherName}/{teacherPassword}")
	public String teacherLoginConfirm(
			@PathParam(value="teacherName") String name,
			@PathParam(value="teacherPassword") String password) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement loginConfirm = con.prepareStatement("SELECT teacher_password FROM lesson.teacher_account WHERE teacher_name = ?");
		
		try{
	    	loginConfirm.setString(1, name);
	    	ResultSet data = loginConfirm.executeQuery();
	    	if(data.first()){
	    		String rightPassString = data.getString("teacher_password");
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
	 * 得到登录教师的ID，以供之后各项操作使用
	 * @param name 用户名
	 * @return ID,或-1代表查询失败
	 * @throws SQLException
	 */
	@GET
	@Path("/getTeacherID/{teacherName}")
	public int getTeacherID(
			@PathParam(value="teacherName") String name) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement getTeacherID = con.prepareStatement("SELECT teacher_id FROM lesson.teacher_account WHERE teacher_name = ?");
		
		try{
			getTeacherID.setString(1, name);
	    	ResultSet data = getTeacherID.executeQuery();
	    	if(data.first()){
	    		int studentID = data.getInt("teacher_id");
	    		return studentID;
	    	} else {
	    		return -1;
	    	}
	    }
	    finally{
	    	//Close resources in all cases
	    	getTeacherID.close();
	    	con.close();
	    }
	}

	/**
	 * 查询学生用户名是否存在
	 * @param studentName 学生用户名
	 * @return 查询结果，大于0说明已存在，小于0不存在
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
	 * 教师注册
	 * @param name 用户名
	 * @param password 密码
	 * @return 注册结果
	 * @throws SQLException
	 */
	@GET
	@Path("/registerTeacher/{teacherName}/{teacherPassword}")
	public Response registerTeacher(
			@PathParam(value="teacherName") String name,
			@PathParam(value="teacherPassword") String password) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement insertTeacher = con.prepareStatement("insert into lesson.teacher_account (teacher_name,teacher_password) values (?,?)");
		
		try{
			insertTeacher.setString(1, name);
			insertTeacher.setString(2, password);
			insertTeacher.executeUpdate();
	    	//Return a 200 OK
	    	return Response.ok().build();
	    }
		catch (SQLIntegrityConstraintViolationException violation) {
	        //Trying to create a user that already exists
	        return Response.status(Status.CONFLICT).entity(violation.getMessage()).build();
	    }
	    finally{
	        //Close resources in all cases
	    	insertTeacher.close();
	        con.close();
	    }
	}
	
	/**
	 * 查询教师用户名是否存在
	 * @param teacherName 教师用户名
	 * @return 查询结果，大于0说明已存在，小于0不存在
	 * @throws SQLException
	 */
	@GET
	@Path("/isTeacherName/{teacherName}")
	public int isTeacherName(@PathParam(value="teacherName") String teacherName) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement isTeacherName = con.prepareStatement("SELECT teacher_id FROM lesson.teacher_account where teacher_name = ?");
		
		try{
			isTeacherName.setString(1, teacherName);
			ResultSet data = isTeacherName.executeQuery();
	    	if(data.first()){
	    		int teacher_id = data.getInt("teacher_id");
	    		return teacher_id;
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
	    	isTeacherName.close();
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
	 * @param studentID 学生ID
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
	 * 查询某教师的所有课程
	 * @param teacherID 教师ID
	 * @return JSON格式的所有课程
	 * @throws SQLException
	 */
	@GET
	@Path("/getTeacherLesson/{teacherID}")
	@Produces("application/json")
	public JSONArray getTeacherLesson(@PathParam(value="teacherID") String teacherID) throws SQLException{
		JSONArray results = new JSONArray();
		Connection con = getSQLConnection();
		PreparedStatement getTeacherLesson = con.prepareStatement("SELECT * FROM lesson.lessontable where teacher_id=?");
		getTeacherLesson.setString(1, teacherID);
		ResultSet data = getTeacherLesson.executeQuery();
		
		while(data.next()){
			JSONObject item = new JSONObject();
			item.put("id", data.getString("lessontable_id"));
			item.put("lessontable_name", data.getString("lessontable_name"));
			item.put("lessontable_description", data.getString("lessontable_description"));
			item.put("lessontable_key", data.getString("lessontable_key"));
			results.add(item);
		}

		getTeacherLesson.close();
		con.close();

		return results;
	}
	
	/**
	 * 学生订阅某课程
	 * @param lessonID
	 * @param studentID
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
	 * @param lessonID
	 * @param studentID
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
	 * @param lessonID
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
	 * @param lessonID
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
	 * 查询某学生的所有提问
	 * @param studentID
	 * @return JSON格式的所有提问
	 * @throws SQLException
	 */
	@GET
	@Path("/getStudentQuestion/{studentID}")
	@Produces("application/json")
	public JSONArray getStudentQuestion(@PathParam(value="studentID") String studentID) throws SQLException{
		JSONArray results = new JSONArray();
		Connection con = getSQLConnection();
		PreparedStatement getLessonQuestion = con.prepareStatement("select lesson_question_id, lesson_question_title, lesson_question_description, lesson_question_time, lessontable_name from (select lessontable_id,lessontable_name from lesson.lessontable) l , (select * from lesson.lesson_question) q where l.lessontable_id=q.lessontable_id and student_id in (select student_id  from lesson.lesson_question where student_id = ?)");
		getLessonQuestion.setString(1, studentID);
		ResultSet data = getLessonQuestion.executeQuery();
		
		while(data.next()){
			JSONObject item = new JSONObject();
			item.put("lesson_question_id", data.getString("lesson_question_id"));
			item.put("lessontable_name", data.getString("lessontable_name"));
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
	 * @param questionID
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
	
	/**
	 * 给问题添加评论
	 * @param questionID 问题ID
	 * @param teacherID 教师ID
	 * @param commentDescription 评论内容
	 * @return 操作结果
	 * @throws SQLException
	 */
	@GET
	@Path("/addComment/{questionID}/{teacherID}/{commentDescription}")
	public Response addQuestion(
			@PathParam(value="questionID") String questionID,
			@PathParam(value="teacherID") String teacherID,
			@PathParam(value="commentDescription") String commentDescription) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement addQuestion = con.prepareStatement("insert into lesson.question_comment (lesson_question_id,teacher_id,question_comment_description) values (?,?,?)");
		
		try{
			addQuestion.setString(1, questionID);
			addQuestion.setString(2, teacherID);
			addQuestion.setString(3, commentDescription);
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
	
	/**
	 * 添加公告
	 * @param lessonID
	 * @param teacherID
	 * @param noticeDescription
	 * @return 
	 * @throws SQLException
	 */
	@GET
	@Path("/addNotice/{lessonID}/{teacherID}/{noticeDescription}")
	public Response addNotice(
			@PathParam(value="lessonID") String lessonID,
			@PathParam(value="teacherID") String teacherID,
			@PathParam(value="noticeDescription") String noticeDescription) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement addNotice = con.prepareStatement("insert into lesson.lesson_notice (lessontable_id,teacher_id,lesson_notice_description) values (?,?,?)");
		
		try{
			addNotice.setString(1, lessonID);
			addNotice.setString(2, teacherID);
			addNotice.setString(3, noticeDescription);
			addNotice.executeUpdate();
	    	//Return a 200 OK
	    	return Response.ok().build();
	    }
		catch (SQLIntegrityConstraintViolationException violation) {
	        //Trying to create a user that already exists
	        return Response.status(Status.CONFLICT).entity(violation.getMessage()).build();
	    }
	    finally{
	        //Close resources in all cases
	    	addNotice.close();
	        con.close();
	    }
	}
	
	/**
	 * 添加提问
	 * @param lessonID
	 * @param studentID
	 * @param questionTitle
	 * @param questionDescription
	 * @return 提问结果
	 * @throws SQLException
	 */
	@GET
	@Path("/addQuestion/{lessonID}/{studentID}/{questionTitle}/{questionDescription}")
	public Response addQuestion(
			@PathParam(value="lessonID") String lessonID,
			@PathParam(value="studentID") String studentID,
			@PathParam(value="questionTitle") String questionTitle,
			@PathParam(value="questionDescription") String questionDescription) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement addQuestion = con.prepareStatement("insert into lesson.lesson_question (lessontable_id,student_id,lesson_question_title,lesson_question_description) values (?,?,?,?)");
		
		try{
			addQuestion.setString(1, lessonID);
			addQuestion.setString(2, studentID);
			addQuestion.setString(3, questionTitle);
			addQuestion.setString(4, questionDescription);
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
	
	/**
	 * 查询某提问的所有评论
	 * @param questionID
	 * @return JSON格式的所有评论
	 * @throws SQLException
	 */
	@GET
	@Path("/getQuestionComment/{questionID}")
	@Produces("application/json")
	public JSONArray getQuestionComment(@PathParam(value="questionID") String questionID) throws SQLException{
		JSONArray results = new JSONArray();
		Connection con = getSQLConnection();
		PreparedStatement getQuestionComment = con.prepareStatement("select question_comment_id, question_comment_description, question_comment_time, teacher_name from (select teacher_id,teacher_name from lesson.teacher_account) t , (select * from lesson.question_comment) c where t.teacher_id=c.teacher_id and question_comment_id in (select question_comment_id  from lesson.question_comment where lesson_question_id = ?)");
		getQuestionComment.setString(1, questionID);
		ResultSet data = getQuestionComment.executeQuery();
		
		while(data.next()){
			JSONObject item = new JSONObject();
			item.put("question_comment_id", data.getString("question_comment_id"));
			item.put("question_comment_description", data.getString("question_comment_description"));
			item.put("question_comment_time", data.getString("question_comment_time"));
			item.put("teacher_name", data.getString("teacher_name"));
			results.add(item);
		}

		getQuestionComment.close();
		con.close();

		return results;
	}
	
	/**
	 * 删除提问
	 * @param questionID 提问编号
	 * @return 取消订阅是否成功
	 * @throws SQLException
	 */
	@DELETE
	@Path("/deleteQuestion/{questionID}")
	public Response deleteQuestion(@PathParam(value="questionID") String questionID) throws SQLException{
	    Connection con = getSQLConnection();
	    PreparedStatement selectQuestion = con.prepareStatement("SELECT * FROM lesson.lesson_question where lesson_question_id=?");

	    try{
	    	selectQuestion.setString(1, questionID);
	        ResultSet data = selectQuestion.executeQuery();

	        if(data.first()){
	            PreparedStatement deleteQuestion = con.prepareStatement("DELETE FROM lesson.lesson_question WHERE lesson_question_id=?");
	            deleteQuestion.setString(1, questionID);
	            deleteQuestion.executeUpdate();
	            deleteQuestion.close();
	            return Response.ok().build();

	        } else{
	            return Response.status(Status.NOT_FOUND).entity("Question not found...").build();
	        }
	    }
	    finally{
	        //Close resources in all cases
	    	selectQuestion.close();
	        con.close();
	    }
	}
	
	/**
	 * 删除课程
	 * @param lessonID
	 * @return
	 * @throws SQLException
	 */
	@DELETE
	@Path("/deleteLesson/{lessonID}")
	public Response deleteLesson(@PathParam(value="lessonID") String lessonID) throws SQLException{
	    Connection con = getSQLConnection();
	    PreparedStatement selectLesson = con.prepareStatement("SELECT * FROM lesson.lessontable where lessontable_id=?");

	    try{
	    	selectLesson.setString(1, lessonID);
	        ResultSet data = selectLesson.executeQuery();

	        if(data.first()){
	            PreparedStatement deleteLesson = con.prepareStatement("DELETE FROM lesson.lessontable WHERE lessontable_id=?");
	            deleteLesson.setString(1, lessonID);
	            deleteLesson.executeUpdate();
	            deleteLesson.close();
	            return Response.ok().build();

	        } else{
	            return Response.status(Status.NOT_FOUND).entity("Lesson not found...").build();
	        }
	    }
	    finally{
	        //Close resources in all cases
	    	selectLesson.close();
	        con.close();
	    }
	}
	
	/**
	 * 删除问题相关的评论
	 * @param questionID 问题ID
	 * @return 删除结果
	 * @throws SQLException
	 */
	@DELETE
	@Path("/deleteQuestionComment/{questionID}")
	public Response deleteQuestionComment(@PathParam(value="questionID") String questionID) throws SQLException{
	    Connection con = getSQLConnection();
	    PreparedStatement getQuestionComment = con.prepareStatement("SELECT * FROM lesson.question_comment where lesson_question_id=?");

	    try{
	    	getQuestionComment.setString(1, questionID);
	        ResultSet data = getQuestionComment.executeQuery();

	        if(data.first()){
	            PreparedStatement deleteQuestionCommen = con.prepareStatement("DELETE FROM lesson.question_comment where lesson_question_id=?");
	            deleteQuestionCommen.setString(1, questionID);
	            deleteQuestionCommen.executeUpdate();
	            deleteQuestionCommen.close();
	            return Response.ok().build();

	        } else{
	            return Response.status(Status.NOT_FOUND).entity("Comment not found...").build();
	        }
	    }
	    finally{
	        //Close resources in all cases
	    	getQuestionComment.close();
	        con.close();
	    }

	}
	
	/**
	 * 查找课程名字是否存在
	 * @param lessonName
	 * @return
	 * @throws SQLException
	 */
	@GET
	@Path("/isLessonName/{lessonName}")
	public int isLessonName(@PathParam(value="lessonName") String lessonName) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement isLessonName = con.prepareStatement("SELECT lessontable_id FROM lesson.lessontable where lessontable_name = ?");
		
		try{
			isLessonName.setString(1, lessonName);
			ResultSet data = isLessonName.executeQuery();
	    	if(data.first()){
	    		int student_id = data.getInt("lessontable_id");
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
	    	isLessonName.close();
	        con.close();
	    }
	}
	
	/**
	 * 添加课程
	 * @param lessonName
	 * @param lessonDescription
	 * @param lessonkey
	 * @param teacherID
	 * @return
	 * @throws SQLException
	 */
	@GET
	@Path("/addLesson/{lessonName}/{lessonDescription}/{lessonkey}/{teacherID}")
	public Response addLesson(
			@PathParam(value="lessonName") String lessonName,
			@PathParam(value="lessonDescription") String lessonDescription,
			@PathParam(value="lessonkey") String lessonkey,
			@PathParam(value="teacherID") String teacherID) throws SQLException{
		Connection con = getSQLConnection();
		PreparedStatement addLesson = con.prepareStatement("insert into lesson.lessontable (lessontable_name, lessontable_description, lessontable_key, teacher_id) values (?,?,?,?)");
		
		try{
			addLesson.setString(1, lessonName);
			addLesson.setString(2, lessonDescription);
			addLesson.setString(3, lessonkey);;
			addLesson.setString(4, teacherID);
			addLesson.executeUpdate();
	    	//Return a 200 OK
	    	return Response.ok().build();
	    }
		catch (SQLIntegrityConstraintViolationException violation) {
	        //Trying to create a user that already exists
	        return Response.status(Status.CONFLICT).entity(violation.getMessage()).build();
	    }
	    finally{
	        //Close resources in all cases
	    	addLesson.close();
	        con.close();
	    }
	}
}