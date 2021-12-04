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
    //定义标记变量
    private static boolean flag = false;
    private static String current_user = null;

    @Override
    //初始化servlet
    public void init() throws ServletException {
        super.init();
        flag = false;
        current_user = null;
        System.out.println("init" + false);
    }


    @Override
    //doget方法处理get请求
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doPost(request, response);
    }

    @Override
    //dopost方法处理post请求
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("dopost" + flag);
        //设置字符集
        request.setCharacterEncoding("UTF-8");
        //html表单中设置的标志位，用来判断post请求是登录注册还是加购结算
        String cart = request.getParameter("cart");
        //登录注册
        if (cart.equals("false")) {
            String choose = request.getParameter("choose");
            System.out.println(choose);
            //登录
            if ("login".equals(choose)) {
                //获取表单信息
                String userName = request.getParameter("UserName");
                String pwd = request.getParameter("Pwd");
                System.out.println(userName);
                System.out.println(pwd);
                try {
                    //连接数据库并在数据库中查找该用户名对应的密码
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
                        //若密码匹配则登录成功，并设标志位
                        if (pwd.equals(password)) {
                            current_user = userName;
                            flag = true;
                            System.out.println("user:+" + current_user + "flag:" + flag);
                            request.getRequestDispatcher("/index.html").forward(request, response);
                        } else {
                            //密码错误的提示页
                            request.getRequestDispatcher("/error_wrong.html").forward(request, response);
                        }
                    } else {
                        //不存在该用户的提示页
                        request.getRequestDispatcher("/error_null.html").forward(request, response);
                    }
                    st.close();
                    con.close();
                } catch (SQLException | ClassNotFoundException throwables) {
                    throwables.printStackTrace();
                }
            }
            //注册
            if ("register".equals(choose)) {
                //获取表单信息
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
                    //密码输入不一致
                    request.getRequestDispatcher("/error_register.html").forward(request, response);
                } else {
                    try {
                        //连接数据库
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
                            //用户名已存在
                            request.getRequestDispatcher("/error_exist.html").forward(request, response);
                        } else {
                            //注册成功
                            st.executeUpdate(sql1);
                            request.getRequestDispatcher("/index.html").forward(request, response);
                        }
                    } catch (ClassNotFoundException | SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            //处理加购和结算请求
            System.out.println("doget" + flag);
            System.out.println("user:+" + current_user + "flag:" + flag);
            String check = request.getParameter("check");
            //加购
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
                    //判断是否登录
                    if (flag) {
                        String sql1 = "insert into shoppingcart(user,name,qty,price) values(\"" + current_user + "\",\"" + name + "\"," + qty + "," + price + ")";
                        st.executeUpdate(sql1);
                        //将所购买的商品加入购物车表
                        request.getRequestDispatcher("/add.html").forward(request, response);
                    } else {
                        //未登录报错
                        request.getRequestDispatcher("/error_nologin.html").forward(request, response);
                    }
                    st.close();
                    con.close();
                } catch (ClassNotFoundException | SQLException | ServletException e) {
                    e.printStackTrace();
                }
            } else {
                //结算
                System.out.println("Check");//发邮件
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    Connection con = DriverManager.getConnection("jdbc:mysql:///mydb", "root", "fracture@123");
                    Statement st = con.createStatement();
                    ResultSet rs;
                    String email = null;
                    //判断是否登录
                    if (flag) {
                        System.out.println("user:+" + current_user + "flag:" + flag);
                        String sql2 = "select * from users where username = \"" + current_user + "\"";
                        rs = st.executeQuery(sql2);
                        while (rs.next()) {
                            //过去登录用户的邮箱
                            email = rs.getString(2);
                        }
                        //新建邮件连接
                        Properties properties = new Properties();

                        properties.setProperty("mail.host", "smtp.qq.com");

                        properties.setProperty("mail.transport.protocol", "smtp");

                        properties.setProperty("mail.smtp.auth", "true");

                        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

                        //创建一个session对象
                        Session session = Session.getDefaultInstance(properties, new Authenticator() {
                            @Override
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication("1850779031@qq.com", "zxyrfyhvxiqseccg");
                            }
                        });

                        //开启debug模式
                        session.setDebug(true);

                        //获取连接对象
                        Transport transport = session.getTransport();

                        //连接服务器
                        transport.connect("smtp.qq.com", "1850779031@qq.com", "zxyrfyhvxiqseccg");

                        //创建邮件对象
                        MimeMessage mimeMessage = new MimeMessage(session);

                        //邮件发送人
                        mimeMessage.setFrom(new InternetAddress("1850779031@qq.com"));

                        //邮件接收人
                        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(email));

                        //邮件标题
                        mimeMessage.setSubject("感谢您的购买");
                        //将购买记录添加到日志表中
                        String sql5 = "select * from shoppingcart where user = \"" + current_user + "\"";
                        int total = 0;
                        Statement st1 = con.createStatement();
                        ResultSet rs1 = st1.executeQuery(sql5);
                        while (rs1.next()) {
                            String sql = "insert into shoppinglogs(user,name,qty,price)values(\"" + current_user + "\",\"" + rs1.getString(2) + "\"," + rs1.getString(3) + "," + rs1.getString(4) + ")";
                            st.executeUpdate(sql);
                            total += Integer.parseInt(rs1.getString(3)) * Integer.parseInt(rs1.getString(4));
                        }
                        //邮件内容
                        mimeMessage.setContent(new StringBuilder().append("感谢您的购买，共消费￥").append(total).toString(), "text/html;charset=UTF-8");

                        //发送邮件
                        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());

                        //关闭连接
                        transport.close();
                        String sql4 = "delete from shoppingcart where user = \"" + current_user + "\"";
                        st.executeUpdate(sql4);
                        //结算成功提示页
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