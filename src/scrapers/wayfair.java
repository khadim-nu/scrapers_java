package scrapers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author raath
 */
public class wayfair {

    public static void main(String[] args) throws IOException {
        // System.out.println("Marks started");
        String url = "jdbc:mysql://localhost:3306/yazzoopa_scraper?useUnicode=true&characterEncoding=UTF-8";
        String username = "root";
        String password = "raath@aws";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            //  System.out.println("Database connected");
            //////////////////////////
            String siteurl = "http://www.wayfair.com/";
            Document doc = Jsoup.connect(siteurl)
                    .header("Accept-Encoding", "gzip, deflate")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .maxBodySize(0)
                    .timeout(600000)
                    .get();
            Elements products = null;
            
            System.out.println(doc.html());

            System.exit(1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Done.");
    }

}
