// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.text.SimpleDateFormat;  
import java.util.Date;  
import java.util.ArrayList; 
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
	
  ArrayList<UserDetails> allData = new ArrayList<UserDetails>(); 
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
    
    String json = new Gson().toJson(allData);
    response.getWriter().println(json);
  }

  @Override
   public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
       java.util.Date currentDate=new java.util.Date();  
       String name = getParameter(request, "name", "");
       String email = getParameter(request, "email", "");
       String comment = getParameter(request, "comment", "");
   
    	UserDetails newData = new UserDetails(name, email, comment, currentDate);
        allData.add(newData);

        // Redirect back to the HTML page.
    	response.sendRedirect("/index.html");
    }

    public class UserDetails{
        String name;
        String email;
        String comment;
        Date currentDate;

        UserDetails(String name, String email, String comment, Date currentDate){
            this.name = name;
            this.email = email;
            this.comment = comment;
            this.currentDate = currentDate;
        }
    }


	private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
    }
}
