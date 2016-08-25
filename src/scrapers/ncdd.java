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
public class ncdd {

    public static void main(String[] args) throws IOException {
        System.out.println("ncdd started");
        String url = "jdbc:mysql://localhost:3306/lawyer_marketing?useUnicode=true&characterEncoding=UTF-8";
        String username = "root";
        String password = "raath@aws";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            //Retrieve by column name
            String domainURL = "https://www.ncdd.com/find-an-attorney/usa/alabama";

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
            URLs = doc.getElementsByClass("sidebarlist").get(0).getElementsByTag("a");
            int size = URLs.size();
            System.out.println(size);

            for (Element URL : URLs) {
                int start = 0;
                String stateLink = URL.attr("href");
                stateLink = "https://www.ncdd.com" + stateLink;

                try {
                    doc = Jsoup.connect(stateLink)
                            .header("Accept-Encoding", "gzip, deflate")
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                            .referrer("http://www.google.com")
                            .timeout(12000)
                            .followRedirects(true)
                            .get();
                    Elements item_listings = doc.getElementsByClass("searchresultswrap");
                    System.out.println("("+item_listings.size()+") "+stateLink);

                    for (Element item_listing : item_listings) {
                        String pimg = "";
                        String plink = "";
                        String ptitle = "";
                        String pid = "";
                        String email = "";
                        String phone = "";
                        String site = "";
                        String address = "";

                        pimg = item_listing.getElementsByClass("searchlayerimg").get(0).attr("src");
                        plink = item_listing.getElementsByClass("span1").get(0).getElementsByTag("a").attr("href");
                        ptitle = item_listing.getElementsByClass("laywername").get(0).text();
                        email = item_listing.getElementsByClass("span4").get(0).getElementsByTag("a").attr("href");
                        phone = item_listing.getElementsByClass("phone").get(0).getElementsByTag("a").attr("href");
                        try {
                            site = item_listing.getElementsByClass("span3").get(0).getElementsByTag("a").attr("href");
                        } catch (Exception e) {

                        }

                        address = item_listing.getElementsByClass("span8").get(0).text();

                        pid = "ncdd_"+plink;
                        plink ="https://www.ncdd.com"+plink;
                        ///// Deleting existing products ///
                        String queryCheck = "DELETE FROM items WHERE p_id = ?";
                        PreparedStatement st = connection.prepareStatement(queryCheck);
                        st.setString(1, pid);
                        int rs = st.executeUpdate();

                        try {
                            Statement stmtInsert = connection.createStatement();
                            stmtInsert.execute("set names 'utf8'");
                            String sql = "INSERT INTO items (p_id,title,status,link,email,image_url,phone,site,address)"
                                    + "VALUES(?,?,?,?,?,?,?,?,?)";

                            PreparedStatement pstmt = connection.prepareStatement(sql);
                            // Set the values
                            pstmt.setString(1, pid);
                            pstmt.setString(2, ptitle);
                            pstmt.setInt(3, 1);
                            pstmt.setString(4, plink);
                            pstmt.setString(5, email);
                            pstmt.setString(6, pimg);
                            pstmt.setString(7, phone);
                            pstmt.setString(8, site);
                            pstmt.setString(9, address);
                            // Insert 
                            pstmt.executeUpdate();

                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            System.out.println("DB Error.");
                        }
                       
                    }
                 //   System.exit(1);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Page Fetch Error.");
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Done.");
    }

}
