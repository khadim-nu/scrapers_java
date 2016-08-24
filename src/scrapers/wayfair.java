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
 *
 * http://stackoverflow.com/questions/819182/how-do-i-get-the-html-code-of-a-web-page-in-php
 *
 * https://developer.yahoo.com/yql/console/#h=select+*+from+html+where+url%3D%22http%3A%2F%2Fwww.wayfair.com%2F%22
 */
public class wayfair {

    public static void main(String[] args) throws IOException {
        System.out.println("wayfair.com started");
        String url = "jdbc:mysql://localhost:3306/scrapers?useUnicode=true&characterEncoding=UTF-8";
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
            Document doc = Jsoup.connect(siteurl)
                    .header("Accept-Encoding", "gzip, deflate")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .referrer("http://www.google.com")
                    .timeout(0)
                    .followRedirects(true)
                    .get();

            Elements products = null;

            //class="js-cms-link js-ss-click cms_add_link "
            products = doc.getElementsByClass("nav_link_block_links");
            int size = products.size();
            int productsTobeScraped = 5000000;
            for (int i = 0; i < size && productsTobeScraped > 0; i++) {
                Elements cat_anchors = products.get(i).getElementsByTag("a");
                for (Element anchor : cat_anchors) {
                    if (productsTobeScraped > 0) {
                        try {
                            String cat_link = anchor.attr("href");
                            System.out.println("========>>>>" + cat_link);
                            cat_link = cat_link.replace("http://", "");
                            siteurl = u1 + cat_link + u3;
                            doc = Jsoup.connect(siteurl)
                                    .header("Accept-Encoding", "gzip, deflate")
                                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                                    .referrer("http://www.google.com")
                                    .timeout(0)
                                    .followRedirects(true)
                                    .get();

                            Elements cats = doc.getElementsByClass("TextUnderImg");
                            System.out.println(cats.size());
                            for (Element cat : cats) {
                                if (productsTobeScraped > 0) {
                                    sub_cat_link = cat.getElementsByTag("a").attr("href");
                                    sub_cat_link += "&itemsperpage=200";
                                    // System.out.println(sub_cat_link);
                                    try {

                                        doc = Jsoup.connect(sub_cat_link)
                                                .header("Accept-Encoding", "gzip, deflate")
                                                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                                                .referrer("http://www.google.com")
                                                .timeout(0)
                                                .followRedirects(true)
                                                .get();

                                        Elements items = doc.getElementsByClass("productbox");
                                        String category_title = doc.getElementsByTag("title").text().split("|")[0];
                                        for (Element item : items) {
                                            if (productsTobeScraped > 0) {
                                                String pUrl = item.attr("href");
                                                String ptitle = item.getElementsByClass("sb_names").text();
                                                String pprice = item.getElementsByClass("is_price_value").text();
                                                String pupc = item.attr("data-sku");
                                                String pimage = item.getElementsByTag("img").attr("src");
                                                String features = item.getElementsByClass("sb_prod_feature_list").get(0).html();

                                                pprice = pprice.replace("from", "");
                                                pprice = pprice.replace("Â", "");
                                                pprice = pprice.trim();

                                                pupc = "wayfair_" + pupc;

                                                String temp_t[] = ptitle.split("by");

                                                ptitle = temp_t[0];

                                                String upcitemdbURL = "http://www.upcitemdb.com/query?s=";
                                                upcitemdbURL += ptitle + "&type=2";
                                                ///////////////
                                                try {
                                                    doc = Jsoup.connect(upcitemdbURL)
                                                            .header("Accept-Encoding", "gzip, deflate")
                                                            .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                                                            .referrer("http://www.google.com")
                                                            .timeout(0)
                                                            .ignoreContentType(true).ignoreHttpErrors(true)
                                                            .get();
                                                    String upc_code = doc.getElementsByClass("rImage").get(0).getElementsByTag("a").text();
//                                        System.out.println(upc_code);
//                                        System.exit(1);
                                                    //////////////////

                                                    try {

                                                        ///// Deleting existing products ///
                                                        String queryCheck = "DELETE FROM items WHERE p_id = ?";
                                                        PreparedStatement st = connection.prepareStatement(queryCheck);
                                                        st.setString(1, pupc);
                                                        int rs = st.executeUpdate();
                                                        //////////////////////

                                                        Statement stmt = connection.createStatement();
                                                        stmt.execute("set names 'utf8'");
                                                        String sql = "INSERT INTO items (p_id,title,status,link,price,image_url,description,specification,upc,category_title)"
                                                                + "VALUES(?,?,?,?,?,?,?,?,?,?)";

                                                        PreparedStatement pstmt = connection.prepareStatement(sql);
                                                        // Set the values
                                                        pstmt.setString(1, pupc);
                                                        pstmt.setString(2, ptitle);
                                                        pstmt.setInt(3, 1);
                                                        pstmt.setString(4, pUrl);
                                                        pstmt.setString(5, pprice);
                                                        pstmt.setString(6, pimage);
                                                        pstmt.setString(7, "");
                                                        pstmt.setString(8, features);
                                                        pstmt.setString(9, upc_code);
                                                        pstmt.setString(10, category_title);
                                                        // Insert 
                                                        pstmt.executeUpdate();
                                                        productsTobeScraped = productsTobeScraped - 1;
                                                        try {
                                                            Thread.sleep(10000);
                                                        } catch (InterruptedException ie) {
                                                            System.out.println(ie.getMessage());
                                                        }
                                                    } catch (Exception e) {
                                                        System.out.println("DB error");
                                                        System.out.println(e.getMessage());
                                                        System.out.println(sub_cat_link);
                                                    }
                                                } catch (Exception e) {
                                                    System.out.println("UPC error");
                                                    System.out.println(e.getMessage());
                                                    System.out.println(upcitemdbURL);
                                                }
                                            } else {
                                                break;
                                            }
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Sub Cat Error");
                                        System.out.println(sub_cat_link);
                                    }
                                } else {
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Category Error");
                            System.out.println(e.getMessage());
                            System.out.println(sub_cat_link);
                        }
                    } else {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Site URL Error");
            System.out.println(e.getMessage());
            System.out.println(sub_cat_link);
        }
        System.out.println("Done.");
    }

}
