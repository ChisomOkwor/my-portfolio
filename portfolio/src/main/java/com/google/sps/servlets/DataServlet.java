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

import java.io.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
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
    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Query query = new Query("UserData");
        ArrayList < UserData > data = new ArrayList < UserData > ();
        PreparedQuery results = datastore.prepare(query);

        Date currentDate = new Date();

        for (Entity entity: results.asIterable()) {
            long id = entity.getKey().getId();
            String name = (String) entity.getProperty("name");
            String email = (String) entity.getProperty("email");
            String comment = (String) entity.getProperty("comment");
            Date date = (Date) entity.getProperty("date");
            UserData newData = new UserData(id, name, email, comment, date);
            data.add(newData);
        }

        response.setContentType("application/json;");

        Gson gson = new Gson();
        final int numCommentsUserSelects = getIntParameter(request, "numValue", 5);

        final int numCommentsDisplayed = Math.min(numCommentsUserSelects, data.size());
        response.getWriter().println(gson.toJson(data.subList(0, numCommentsDisplayed)));

    }

    public static class UserData {
        long id;
        String name;
        String email;
        String comment;
        Date date;

        UserData(long id, String name, String email, String comment, Date date) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.comment = comment;
            this.date = date;
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Date date = new Date();
        String name = getParameter(request, "name", "");
        String email = getParameter(request, "email", "");
        String comment = getParameter(request, "comment", "");

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name not entered.");
        }

        if (comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment field empty.");
        }

        Entity taskEntity = new Entity("UserData");
        taskEntity.setProperty("name", name);
        taskEntity.setProperty("email", email);
        taskEntity.setProperty("comment", comment);
        taskEntity.setProperty("date", date);
        datastore.put(taskEntity);

        // Redirect back to the HTML page.
        response.sendRedirect("/index.html");
    }

    private int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value == null) {
            return (defaultValue);
        }

        return Integer.parseInt(value);
    }

    private String getParameter(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}