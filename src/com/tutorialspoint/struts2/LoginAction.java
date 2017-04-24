package com.tutorialspoint.struts2;

import com.opensymphony.xwork2.ActionSupport;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by xiedan on 2017/4/21.
 */
public class LoginAction extends ActionSupport {
    private String  user;
    private String password;
    private String name;

    public String execute() {
        String ret = ERROR;
        Connection connection = null;
        String url = "jdbc:mysql://localhost/struts_tutorial";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, "root", "root");
            String sql = "SELECT name FROM login WHERE";
            sql+=" user = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                name = resultSet.getString(1);
                ret = SUCCESS;
            }
        } catch (Exception e) {
            ret = ERROR;
        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                }
            }
        }
        return ret;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
