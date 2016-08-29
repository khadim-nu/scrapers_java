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
 * https://developer.yahoo.com/yql/console/#h=select+*+from+html+where+url%3D%22http%3A%2F%2Fwww.fbScraper.com%2F%22
 */
public class fbScraper {

    public static void main(String[] args) throws IOException {
        System.out.println("fbScraper started");
        String url = "jdbc:mysql://localhost:3306/felipekptive?useUnicode=true&characterEncoding=UTF-8";
        String username = "root";
        String password = "raath@aws";
        String sub_cat_link = "";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            //  System.out.println("Database connected");
            //////////////////////////
            String u1 = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20html%20where%20url%3D'http%3A%2F%2F";
            String u2 = "www.wayfair.com";
            String u3 = "%2F'&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

            String siteurl = u1 + u2 + u3;
//iraisfv@hotmail.com
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM domains";
            ResultSet resultSet = stmt.executeQuery(sql);
            //STEP 5: Extract data from result set
            while (resultSet.next()) {
                String domainURL = "https://www.facebook.com/public/?query=" + resultSet.getString("email");
                sub_cat_link=domainURL;
                //Retrieve by column name
                Document doc = Jsoup.connect(domainURL)
                        .header("Accept-Encoding", "gzip, deflate")
                        .header("Accept-Language", "en")
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                        .timeout(0)
                        .get();

                Element page = null;
                try {
                    page = doc.getElementById("u_0_6");

                    String IDurls[] = page.html().split("_fbBrowseXuiResult__profileImage");
                    String IDlink = IDurls[1].split("\"")[2];
                    sub_cat_link=IDlink;
                    
                    try {
                        doc = Jsoup.connect(IDlink)
                                .header("Accept-Encoding", "gzip, deflate")
                                .header("Accept-Language", "en")
                                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                                .timeout(0)
                                .get();

                        String name = doc.getElementsByClass("_6-e").get(0).text();
                        String fbid = doc.getElementsByClass("coverPhotoImg").get(0).attr("data-fbid");
                        String dp = doc.getElementsByClass("profilePic").get(0).attr("src");

                        Elements fbinfo = doc.getElementsByClass("_4qm1");
                        String information = "";
                        for (Element fbprofile : fbinfo) {
                            String inf = "";
                            inf = fbprofile.getElementsByClass("_h72").get(0).text();
                            inf += "=>" + fbprofile.getElementsByClass("fbProfileEditExperiences").get(0).text();
                            inf += ";";
                            information += inf;

                        }
                        System.out.println(information);
                        String FavouritesString = "";
                        Elements favourites = doc.getElementById("favorites").getElementsByTag("tbody");
                        for (Element favourite : favourites) {
                            String FavStr = "";
                            FavStr = favourite.getElementsByTag("th").get(0).text();
                            FavStr += "=>";
                            for (Element fav : favourite.getElementsByTag("a")) {
                                FavStr += fav.attr("href");
                                FavStr += ",";
                                FavStr += fav.text();
                                FavStr += ",";
                            }
                            FavStr += ";";
                            FavouritesString += FavStr;
                        }
                        System.out.println(FavouritesString);

                        try {
                            String queryCheck = "DELETE FROM items WHERE p_id = ?";
                            PreparedStatement st = connection.prepareStatement(queryCheck);
                            st.setString(1, fbid);
                            int rs = st.executeUpdate();
                            Statement stmtInsert = connection.createStatement();
                            stmtInsert.execute("set names 'utf8'");
                            sql = "INSERT INTO items (p_id,title,link,image_url,information,favourites,other,email)"
                                    + "VALUES(?,?,?,?,?,?,?,?)";

                            PreparedStatement pstmt = connection.prepareStatement(sql);
                            // Set the values
                            pstmt.setString(1, fbid);
                            pstmt.setString(2, name);
                            pstmt.setString(3, IDlink);
                            pstmt.setString(4, dp);
                            pstmt.setString(5, information);
                            pstmt.setString(6, FavouritesString);
                            pstmt.setString(7, "");
                            pstmt.setString(8, resultSet.getString("email"));
                            // Insert 
                            pstmt.executeUpdate();

                        } catch (Exception e) {
                             System.out.println("---------------------------");
                            System.out.println(e.getMessage());
                            System.out.println("DB Error.");
                             System.out.println("---------------------------");
                        }
                    } catch (Exception e) {
                         System.out.println("---------------------------");
                        System.out.println("FB profile URL Error");
                        System.out.println(e.getMessage());
                        System.out.println(sub_cat_link);
                         System.out.println("---------------------------");
                    }
                } catch (Exception e) {
                     System.out.println("---------------------------");
                    System.out.println("Facebook not exist.");
                    System.out.println(sub_cat_link);
                    System.out.println("---------------------------");
                }
                Thread.sleep(5000);
            }

        } catch (Exception e) {
             System.out.println("---------------------------");
            System.out.println("FB Search Error");
            System.out.println(e.getMessage());
            System.out.println(sub_cat_link);
             System.out.println("---------------------------");
        }
        System.out.println("Done.");
    }

}
