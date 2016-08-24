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
public class buybuybaby {
    
    public static String getTitlePart(String[] data, int index){
    try{
      return data[index];
    } catch(ArrayIndexOutOfBoundsException e){
      return "";
    }
}

    public static void main(String[] args) throws IOException {
        System.out.println("buybuybaby.com started");
        String url = "jdbc:mysql://localhost:3306/scrapers?useUnicode=true&characterEncoding=UTF-8";
        String username = "root";
        String password = "raath@aws";
        String sub_cat_link = "";
        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            String siteurl[] = {
                "http://www.buybuybaby.com/store/category/furniture/32609/", 
                "http://www.buybuybaby.com/store/category/gifts-more/personalized-gifts/room-decor/32755/",
                "http://www.buybuybaby.com/store/category/health-safety/baby-monitors/video-monitors/32479/",
                "http://www.buybuybaby.com/store/category/health-safety/baby-monitors/audio-monitors/32480/",
                "http://www.buybuybaby.com/store/category/health-safety/baby-monitors/movement-monitors/32481/",
                "http://www.buybuybaby.com/store/category/health-safety/baby-monitors/wifi-monitors/32482/",
                
                "http://www.buybuybaby.com/store/category/health-safety/baby-monitors/wifi-monitors/32482/",
                "http://www.buybuybaby.com/store/category/toys-learning/books-media/books/32112/",
                "http://www.buybuybaby.com/store/category/toys-learning/books-media/music/32244/",
                "http://www.buybuybaby.com/store/category/toys-learning/books-media/activity-books/32791/",
                "http://www.buybuybaby.com/store/category/toys-learning/books-media/movies-dvd/32243/",
                "http://www.buybuybaby.com/store/category/clothing-accessories/girls-clothing-newborn-4t/girls-tops-sweaters/32336/",
                "http://www.buybuybaby.com/store/category/clothing-accessories/girls-clothing-newborn-4t/girls-sets/32353/",
                "http://www.buybuybaby.com/store/category/clothing-accessories/girls-clothing-newborn-4t/skirts-dresses/32476/",
                "http://www.buybuybaby.com/store/category/clothing-accessories/girls-clothing-newborn-4t/girls-athletic/32355/",
                "http://www.buybuybaby.com/store/category/gifts-more/smart-innovations/gear-travel/32706/",
                "http://www.buybuybaby.com/store/category/gifts-more/smart-innovations/nursing-feeding/32707/",
                "http://www.buybuybaby.com/store/category/gifts-more/smart-innovations/bath-potty/32708/",
                "http://www.buybuybaby.com/store/category/gifts-more/smart-innovations/health-safety/32709/",
                "http://www.buybuybaby.com/store/category/gifts-more/team-shop/collegiate/32671/",
                "http://www.buybuybaby.com/store/category/gifts-more/baby-shower-gifts-favors/baby-shower-favors/32661/",
                "http://www.buybuybaby.com/store/category/gifts-more/gifts-by-recipient/gifts-for-infants/32677/",
                
               "http://www.buybuybaby.com/store/category/strollers/32571/",
                            "http://www.buybuybaby.com/store/category/car-seats/32592/",
                            "http://www.buybuybaby.com/store/category/gear-travel/infant-activity/30505/",
                            "http://www.buybuybaby.com/store/category/nursing-feeding/30009/",
                            "http://www.buybuybaby.com/store/category/clothing-accessories/layette-preemie-24m/32345/",
            };
            for (String _url : siteurl) {

                Document doc = Jsoup.connect(_url)
                        .header("Accept-Encoding", "gzip, deflate")
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                        .referrer("http://www.google.com")
                        .timeout(0)
                        .followRedirects(true)
                        .get();

                Elements categories = doc.getElementsByClass("popularCategoryRow").get(0).getElementsByTag("a");

                for (Element category : categories) {
                    String caturl = "http://www.buybuybaby.com" + category.attr("href") + "1-96?pagSortOpt=DEFAULT-0&view=grid&_requestid=1486321";
                    try {
                        doc = Jsoup.connect(caturl)
                                .header("Accept-Encoding", "gzip, deflate")
                                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                                .referrer("http://www.google.com")
                                .timeout(0)
                                .followRedirects(true)
                                .get();
                        
                        Elements products = doc.getElementsByClass("productContent");
                        System.out.println(products.size());
                        for (Element product : products) {
                            String img = product.getElementsByTag("img").attr("data-lazyloadsrc");
                            String title = product.getElementsByClass("prodName").get(0).getElementsByTag("a").text();
                            String link = product.getElementsByClass("prodName").get(0).getElementsByTag("a").attr("href");
                            String price = product.getElementsByClass("isPrice").get(0).text();
                            String id = product.getElementsByTag("input").attr("data-productid");
                            
                            title =title.replaceAll("[^a-zA-Z ]", "");
                            String parts[]=title.split(" ");
                            String upcTitle =getTitlePart(parts,0);
                            upcTitle +=" "+getTitlePart(parts,1);
                            upcTitle +=" "+getTitlePart(parts,2);
                            upcTitle +=" "+getTitlePart(parts,3);
                            upcTitle +=" "+getTitlePart(parts,4);
                          
                            String upcitemdbURL = "http://www.upcitemdb.com/query?s=";
                            upcitemdbURL += upcTitle + "&type=2";
                            ///////////////
                            doc = Jsoup.connect(upcitemdbURL)
                                    .header("Accept-Encoding", "gzip, deflate")
                                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                                    .referrer("http://www.google.com")
                                    .timeout(0)
                                    .ignoreContentType(true).ignoreHttpErrors(true)
                                    .get();
                            String upc_code = doc.getElementsByClass("rImage").get(0).getElementsByTag("a").text();

                            img = img.replace("?$229$", "");
                            link = "http://www.buybuybaby.com" + link;
                            id = "buybuybaby_" + id;
                            String cats[] = caturl.split("/");
                            String category_title = cats[5] + "=>" + cats[6];

                            price = price.replace("PKR ", "");
                            price = price.replace(",", "");


                            try {

                                ///// Deleting existing products ///
                                String queryCheck = "DELETE FROM items WHERE p_id = ?";
                                PreparedStatement st = connection.prepareStatement(queryCheck);
                                st.setString(1, id);
                                int rs = st.executeUpdate();
                                //////////////////////

                                Statement stmt = connection.createStatement();
                                stmt.execute("set names 'utf8'");
                                String sql = "INSERT INTO items (p_id,title,status,link,price,image_url,description,specification,upc,category_title)"
                                        + "VALUES(?,?,?,?,?,?,?,?,?,?)";

                                PreparedStatement pstmt = connection.prepareStatement(sql);
                                // Set the values
                                pstmt.setString(1, id);
                                pstmt.setString(2, title);
                                pstmt.setInt(3, 1);
                                pstmt.setString(4, link);
                                pstmt.setString(5, price);
                                pstmt.setString(6, img);
                                pstmt.setString(7, "");
                                pstmt.setString(8, "");
                                pstmt.setString(9, upc_code);
                                pstmt.setString(10, category_title);
                                // Insert 
                                pstmt.executeUpdate();
                                System.out.println("Inserted");

                                try {
                                    Thread.sleep(10000);
                                } catch (InterruptedException ie) {
                                   System.out.println(ie.getMessage());
                                }
                            } catch (Exception e) {
                                System.out.println("DB error");
                                System.out.println(e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Category URL Error");
                        System.out.println(caturl);
                        System.out.println(e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Site URL Error");
            System.out.println(e.getMessage());
        }
        System.out.println("Done.");
    }
}
