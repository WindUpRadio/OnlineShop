package com.example.OnlineShop;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

@WebServlet("/hello-servlet")
public class HelloServlet extends HttpServlet {
private static boolean flag = false;
private static String current_user = null;

    @Override
    public void init() throws ServletException {
        super.init();
        flag=false;
        current_user=null;
        System.out.println("init"+false);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("dopost"+flag);
        request.setCharacterEncoding("UTF-8");
        String cart = request.getParameter("cart");
if (cart.equals("false"))
{
    String choose = request.getParameter("choose");
    System.out.println(choose);
    if ("login".equals(choose)) {
        String userName = request.getParameter("UserName");
        String pwd = request.getParameter("Pwd");
        System.out.println(userName);
        System.out.println(pwd);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql:///mydb", "root", "fracture@123");
            String sql = "select * from users where username='" + userName + "'";
            Statement st;
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            String password;
            if (rs.next()) {
                do {
                    password = rs.getString(3);
                }
                while (rs.next());
                if (pwd.equals(password)) {
                    current_user=userName;
                    flag=true;
                    System.out.println("user:+"+ current_user+ "flag:"+flag);
                    request.getRequestDispatcher("/index.html").forward(request, response);
                } else {
                    request.getRequestDispatcher("/error_wrong.html").forward(request, response);
                }
            } else {
                request.getRequestDispatcher("/error_null.html").forward(request, response);
            }
            st.close();
            con.close();
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }
    if ("register".equals(choose)) {
        String UserName = request.getParameter("Username");
        String pwd = request.getParameter("Pwd");
        String repeat_pwd = request.getParameter("Rpwd");
        String email = request.getParameter("Email");
        String number = request.getParameter("Mnumber");
        System.out.println(UserName);
        System.out.println(pwd);
        System.out.println(repeat_pwd);
        System.out.println(email);
        System.out.println(number);
        if (!pwd.equals(repeat_pwd)) {
            request.getRequestDispatcher("/error_register.html").forward(request, response);
        } else {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql:///mydb", "root", "fracture@123");
                String sql1 = "insert into users(username,email,password,phonenumber) values(\"" + UserName + "\",\"" + email + "\",\"" + pwd + "\",\"" + number + "\")";
                String sql2 = "select * from users where username='" + UserName + "'";
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql2);
                if (rs.next()) {
                    do {
                    }
                    while (rs.next());
                    request.getRequestDispatcher("/error_exist.html").forward(request, response);
                } else {
                    st.executeUpdate(sql1);
                    request.getRequestDispatcher("/index.html").forward(request, response);
                }
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
else
{
    System.out.println("doget"+flag);
    System.out.println("user:+"+ current_user+ "flag:"+flag);
    String check = request.getParameter("check");
    if (!check.equals("true")) {
        String name = request.getParameter("name");
        int qty = Integer.parseInt(request.getParameter("qty"));
        int price = Integer.parseInt(request.getParameter("price"));
        System.out.println(name);
        System.out.println(qty);
        System.out.println(price);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql:///mydb", "root", "fracture@123");
            Statement st = con.createStatement();
            if (flag) {
                String sql1 = "insert into shoppingcart(user,name,qty,price) values(\"" + current_user+"\",\""+name + "\"," + qty + "," + price + ")";
                st.executeUpdate(sql1);
                request.getRequestDispatcher("/add.html").forward(request, response);
            }
            else{
                request.getRequestDispatcher("/error_nologin.html").forward(request, response);
            }
            st.close();
            con.close();
        } catch (ClassNotFoundException | SQLException | ServletException e) {
            e.printStackTrace();
        }
    } else {
        System.out.println("Check");//发邮件
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql:///mydb", "root", "fracture@123");
            Statement st = con.createStatement();
            ResultSet rs;
            String email = null;
            if (flag) {
                System.out.println("user:+"+ current_user+ "flag:"+flag);
                String sql2 = "select * from users where username = \"" + current_user + "\"";
                rs = st.executeQuery(sql2);
                while (rs.next()) {
                    email = rs.getString(2);
                }
                Properties properties = new Properties();

                properties.setProperty("mail.host","smtp.qq.com");

                properties.setProperty("mail.transport.protocol","smtp");

                properties.setProperty("mail.smtp.auth","true");

                properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

                //创建一个session对象
                Session session = Session.getDefaultInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("1850779031@qq.com","zxyrfyhvxiqseccg");
                    }
                });

                //开启debug模式
                session.setDebug(true);

                //获取连接对象
                Transport transport = session.getTransport();

                //连接服务器
                transport.connect("smtp.qq.com","1850779031@qq.com","zxyrfyhvxiqseccg");

                //创建邮件对象
                MimeMessage mimeMessage = new MimeMessage(session);

                //邮件发送人
                mimeMessage.setFrom(new InternetAddress("1850779031@qq.com"));

                //邮件接收人
                mimeMessage.setRecipient(Message.RecipientType.TO,new InternetAddress(email));

                //邮件标题
                mimeMessage.setSubject("感谢您的购买");
                String sql5 = "select * from shoppingcart where user = \""+current_user+"\"";
                int total=0;
                Statement st1 = con.createStatement();
                ResultSet rs1 = st1.executeQuery(sql5);
                while (rs1.next())
                {
                    String sql = "insert into shoppinglogs(user,name,qty,price)values(\""+current_user+"\",\""+rs1.getString(2)+"\","+rs1.getString(3)+","+rs1.getString(4)+")";
                    st.executeUpdate(sql);
                    total+=Integer.parseInt(rs1.getString(3))*Integer.parseInt(rs1.getString(4));
                }
                //邮件内容
                mimeMessage.setContent(new StringBuilder().append("感谢您的购买，共消费￥").append(total).toString(),"text/html;charset=UTF-8");

                //发送邮件
                transport.sendMessage(mimeMessage,mimeMessage.getAllRecipients());

                //关闭连接
                transport.close();
                String sql4 = "delete from shoppingcart where user = \""+current_user+"\"";
                st.executeUpdate(sql4);
                request.getRequestDispatcher("/check.html").forward(request, response);
            } else {
                request.getRequestDispatcher("/error_nologin.html").forward(request, response);
            }
        } catch (ClassNotFoundException | SQLException | ServletException | MessagingException e) {
            e.printStackTrace();
        }

    }
}
    }

}