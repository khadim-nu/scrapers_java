package scrapers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author raath
 *
 * http://stackoverflow.com/questions/819182/how-do-i-get-the-html-code-of-a-web-page-in-php
 *
 * https://developer.yahoo.com/yql/console/#h=select+*+from+html+where+url%3D%22http%3A%2F%2Fwww.ncdd.com%2F%22
 */
public class gumtree {

    public static void main(String[] args) throws IOException {
        
        System.out.println("gumtree started");
        String url = "jdbc:mysql://localhost:3306/byetback?useUnicode=true&characterEncoding=UTF-8";
        String username = "root";
        String password = "raath";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            //Retrieve by column name
            String domainURL = "https://www.gumtree.com/for-sale/uk";

            //  System.out.println("Database connected");
            //////////////////////////
//                String u1 = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20html%20where%20url%3D'http%3A%2F%2F";
//                String u2 = "www.wayfair.com";
//                String u3 = "%2F'&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
            String siteurl = domainURL;
            Document doc = Jsoup.connect(siteurl)
                    .header("Accept-Encoding", "gzip, deflate")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .referrer("http://www.google.com")
                    .timeout(12000)
                    .followRedirects(true)
                    .get();

            Elements URLs = null;

            //class="js-cms-link js-ss-click cms_add_link "
            URLs = doc.getElementsByClass("listing-link");
            int size = URLs.size();
            System.out.println(size);
            // System.exit(1);
            for (Element URL : URLs) {

                String adLink = URL.attr("href");
                if (!adLink.isEmpty()) {
                    adLink = "https://www.gumtree.com" + adLink;
                    System.out.println(adLink);
//                    System.exit(1);
                    try {
                        doc = Jsoup.connect(adLink)
                                .header("Accept-Encoding", "gzip, deflate")
                                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                                .referrer("http://www.google.com")
                                .timeout(12000)
                                .followRedirects(true)
                                .get();

                        String ad_id = doc.getElementById("reportForm.advertId-1").attr("value");

                        String name = doc.getElementsByClass("space-mbn").get(0).text();

                        String contact = "";

                        String adTitle = doc.getElementsByClass("space-mbs").get(0).getElementsByTag("h1").get(0).text();

                        String address = doc.getElementsByClass("space-mbs").get(0).getElementsByClass("ad-location").get(0).text();

                        String adText = doc.getElementsByClass("ad-description").get(0).text();

//                            ///// Deleting existing products ///
                        String queryCheck = "DELETE FROM items WHERE ad_id = ?";
                        PreparedStatement st = connection.prepareStatement(queryCheck);
                        st.setString(1, ad_id);
                        int rs = st.executeUpdate();
                        /////////////////////////////

                        try {
                            Statement stmtInsert = connection.createStatement();
                            stmtInsert.execute("set names 'utf8'");
                            String sql = "INSERT INTO items (ad_id,name,contact,ad_title,address,source_link,adText)"
                                    + "VALUES(?,?,?,?,?,?,?)";

                            PreparedStatement pstmt = connection.prepareStatement(sql);
                            // Set the values
                            pstmt.setString(1, ad_id);
                            pstmt.setString(2, name);
                            pstmt.setString(3, contact);
                            pstmt.setString(4, adTitle);
                            pstmt.setString(5, address);
                            pstmt.setString(6, adLink);
                            pstmt.setString(7, adText);
                            // Insert 
                            pstmt.executeUpdate();

                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            System.out.println("DB Error.");
                        }

                          // System.exit(1);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        System.out.println("Page Fetch Error.");
                    }
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Done.");
    }

}
