package ece1779.appengine.datastore;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import javax.servlet.http.*;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;


@SuppressWarnings("serial")
public class QueriesServlet extends HttpServlet {
    public void doGet(HttpServletRequest req,
                      HttpServletResponse resp)
        throws IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        // Create some entities.
        Entity book1 = new Entity("Book");
        book1.setProperty("title", "The Grapes of Wrath");
        book1.setProperty("copyrightYear", 1939);
        ds.put(book1);
        Entity book2 = new Entity("Book");
        book2.setProperty("title", "Of Mice and Men");
        book2.setProperty("copyrightYear", 1937);
        ds.put(book2);
        Entity book3 = new Entity("Book");
        book3.setProperty("title", "East of Eden");
        book3.setProperty("copyrightYear", 1952);
        ds.put(book3);

        // Prepare a query.
        Query q = new Query("Book");
        q.setFilter(new FilterPredicate("copyrightYear",
                Query.FilterOperator.LESS_THAN_OR_EQUAL,
                1960));
        q.addSort("copyrightYear",Query.SortDirection.DESCENDING);
        q.addSort("title");

        
        // select * 
        // from Book 
        // where copyrightYear <= 1960 
        // order by copyrightYear desc, title"
        
        out.println("<h1>Properties Query</h1>");

        
        // Perform the query.
        PreparedQuery pq = ds.prepare(q);
        for (Entity result : pq.asIterable(FetchOptions.Builder.withLimit(10).offset(0))) {
            String title = (String) result.getProperty("title");
            out.println("<p>Query result: title = " + title + "</p>");
        }
  

        out.println("<h1>Keys Query</h1>");

        // Perform query that returns only keys
        q.setKeysOnly();
        pq = ds.prepare(q);
        for (Entity result : pq.asIterable(FetchOptions.Builder.withLimit(10).offset(0))) {
            Key key = result.getKey();
            out.println("<p>Query result: key = " + KeyFactory.keyToString(key) + "</p>");
        }

        ds.delete(book1.getKey(), book2.getKey(), book3.getKey());

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSSSS");
        fmt.setTimeZone(new SimpleTimeZone(0, ""));
        out.println("<p>The time is: " + fmt.format(new Date()) + "</p>");
    }
}
