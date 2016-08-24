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
 * https://developer.yahoo.com/yql/console/#h=select+*+from+html+where+url%3D%22http%3A%2F%2Fwww.adidas.com%2F%22
 */
public class adidas {

    public static void main(String[] args) throws IOException {
        System.out.println("Adidas started");
        String url = "jdbc:mysql://localhost:3306/jamesClark_scraper?useUnicode=true&characterEncoding=UTF-8";
        String username = "root";
        String password = "raath@aws";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM domains";
            ResultSet resultSet = stmt.executeQuery(sql);
            //STEP 5: Extract data from result set
            while (resultSet.next()) {
                //Retrieve by column name
                String domainURL = resultSet.getString("url");

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
                URLs = doc.getElementsByClass("top-cat-link");
                int size = URLs.size();
                System.out.println(URLs.size());
                for (Element URL : URLs) {
                    int start = 0;
                    String catLink = URL.attr("href");
                    catLink += "?grid=true&srule=newest-to-oldest&sz=120&start=";

                    try {
                        boolean run = true;
                        while (run) {
                            System.out.println(catLink + start);
                            try {
                                doc = Jsoup.connect(catLink + start)
                                        .header("Accept-Encoding", "gzip, deflate")
                                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                                        .referrer("http://www.google.com")
                                        .timeout(12000)
                                        .followRedirects(true)
                                        .get();
                                Elements item_listings = doc.getElementsByClass("hockeycard");
                                System.out.println(item_listings.size());
                                for (Element item_listing : item_listings) {
                                    String pimg = "";
                                    String plink = "";
                                    String ptitle = "";
                                    String pid = "";
                                    String pprice = "";
                                    String preleaseDate = "";

                                    pimg = item_listing.getElementsByTag("img").attr("data-original");
                                    plink = item_listing.getElementsByClass("product-link").get(0).attr("href");
                                    ptitle = item_listing.getElementsByClass("title").get(0).text();
                                    pid = item_listing.getElementsByClass("product-link").get(0).attr("data-track");
                                    pprice = item_listing.getElementsByClass("currency-sign").get(0).text().trim();
                                    pprice += item_listing.getElementsByClass("salesprice").get(0).text().trim();
                                    preleaseDate = "";
                                    pid="adidas_" +pid;
                                    ///// Deleting existing products ///
                                    String queryCheck = "DELETE FROM items WHERE p_id = ?";
                                    PreparedStatement st = connection.prepareStatement(queryCheck);
                                    st.setString(1, pid);
                                    int rs = st.executeUpdate();
                                    //////////////////////
                                    System.out.println(pimg);
                                    System.out.println(plink);
                                    System.out.println(ptitle);
                                    System.out.println(pid);
                                    System.out.println(pprice);
                                    try {
                                        Statement stmtInsert = connection.createStatement();
                                        stmtInsert.execute("set names 'utf8'");
                                        sql = "INSERT INTO items (p_id,category_title,title,status,link,price,image_url,description,specification,other)"
                                                + "VALUES(?,?,?,?,?,?,?,?,?,?)";

                                        PreparedStatement pstmt = connection.prepareStatement(sql);
                                        // Set the values
                                        pstmt.setString(1,  pid);
                                        pstmt.setString(2, "");
                                        pstmt.setString(3, ptitle);
                                        pstmt.setInt(4, 1);
                                        pstmt.setString(5, plink);
                                        pstmt.setString(6, pprice);
                                        pstmt.setString(7, pimg);
                                        pstmt.setString(8, "");
                                        pstmt.setString(9, "");
                                        pstmt.setString(10, "");
                                        // Insert 
                                        pstmt.executeUpdate();

                                    } catch (Exception e) {
                                        System.out.println(e.getMessage());
                                        System.out.println("DB Error.");
                                    }
//                                    System.exit(1);
                                }
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                System.out.println("Next Page Fetch Error.");
                            }
                            start += 120;
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        System.out.println("Page Fetch Error.");
                    }
                }

                //  System.out.println(URLs.html());
                System.exit(1);

            }
            resultSet.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Done.");
    }

}
