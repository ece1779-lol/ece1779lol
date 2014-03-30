package ece1779.appengine;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import javax.servlet.http.*;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class Clock2 extends HttpServlet {
    public void doGet(HttpServletRequest req,
                      HttpServletResponse resp)
        throws IOException {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSSSS");
        fmt.setTimeZone(new SimpleTimeZone(0, ""));

        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        
 
        
        String navBar;
        if (user != null) {
            navBar = "<p>Welcome, " + user.getNickname() + "! You can <a href=\"" +
                     userService.createLogoutURL("/") +
                     "\">sign out</a>.</p>";
        } else {
            navBar = "<p>Welcome! <a href=\"" + userService.createLoginURL("/") +
                     "\">Sign in or register</a> to customize.</p>";
        }

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println(navBar);
        out.println("<p>The time is: " + fmt.format(new Date()) + "</p>");
    }
}
