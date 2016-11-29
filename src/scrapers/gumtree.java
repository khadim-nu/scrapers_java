/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import static java.nio.charset.StandardCharsets.*;
import java.sql.SQLException;

/**
 *
 * @author raath
 */
public class gumtree implements Runnable {

    Connection connection;
    private String tag;
    private Thread t;
    private String urlsTable;
    private String ItemsTable;
    int pageFrom;
    int pageTo;

    public gumtree(String tag, int pagefrom, int pageto) {
        String password = "incubasys";

        this.urlsTable = "urls";
        this.ItemsTable = "items";

        this.pageFrom = pagefrom;
        this.pageTo = pageto;

        this.tag = tag;

        String url = "jdbc:mysql://localhost:3306/byetback?useUnicode=true&characterEncoding=UTF-8";
        String username = "root";

        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("connected successfully");;
        } catch (Exception e) {
            System.out.println("DB connection error");;
        }

        System.out.println("Creating " + tag);
        this.start();
    }

    public void startScraping() {

//                String u1 = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20html%20where%20url%3D'http%3A%2F%2F";
//                String u2 = "www.wayfair.com";
//                String u3 = "%2F'&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
        boolean keepRunnung = false;

        // fetching urls from DB
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs;

            rs = stmt.executeQuery("SELECT * FROM " + this.urlsTable + " WHERE tag = '" + tag + "' AND id >= " + pageFrom + " AND id < " + pageTo + " AND status=1 order by id");

            String domainURL = "";

            while (rs.next()) {
                domainURL = rs.getString("url");

                /////////////////////////////
                if (!domainURL.isEmpty()) {
                    keepRunnung = true;
                }

                for (int i = 1; keepRunnung; i++) {

                    System.out.println("linkId:" + rs.getInt("id") + " => " + domainURL);

                    Document doc = Jsoup.connect(domainURL)
                            .header("Accept-Encoding", "gzip, deflate")
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                            .referrer("http://www.google.com")
                            .timeout(1200000)
                            .followRedirects(true)
                            .get();

                    Elements URLs = null;

                    URLs = doc.getElementsByClass("listing-link");

                    int size = URLs.size();

                    // preserve old url
                    String domainURL_old = domainURL;

                    if (doc.getElementsByClass("pagination-next").size() > 0) {
                        domainURL = "https://www.gumtree.com" + doc.getElementsByClass("pagination-next").get(0).getElementsByTag("a").attr("href");

                    } else {
                        domainURL = "";
                    }
                    //stoping condition
                    if (domainURL.isEmpty() || i >= 50) {
                        keepRunnung = false;

                        /// Deleting existing products ///
                        String uqueryCheck = "UPDATE " + this.urlsTable + " SET status = 0 WHERE id = ?";
                        PreparedStatement ust = connection.prepareStatement(uqueryCheck);
                        ust.setInt(1, rs.getInt("id"));
                        int urst = ust.executeUpdate();
                        /////////////////////////////  
                    }

                    System.out.println(size);

                    for (Element URL : URLs) {

                        String adLink = URL.attr("href");
                        if (!adLink.isEmpty()) {

                            adLink = adLink.replaceAll("\\?", "%C2%A3");
                            adLink = "https://www.gumtree.com" + adLink;
                            scrapeSingleUrl(adLink, domainURL_old);
                        }

                    }
                }
//                    System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("error in domain url:" + e.getMessage());
        }

    }

    public void scrapeSingleUrl(String adLink, String domainURL_old) throws SQLException {
        Document doc;
        try {
            doc = Jsoup.connect(adLink)
                    .header("Accept-Encoding", "gzip, deflate")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .referrer("http://www.google.com")
                    .timeout(1200000)
                    .followRedirects(true)
                    .get();

            String ad_id = doc.getElementById("reportForm.advertId-1").attr("value");

            String name = doc.getElementsByClass("space-mbn").get(0).text();

            String contact = "";

            ////////////
            String token = doc.html().split("revealSellerTelephoneNumberToken")[1].split("\"")[2];
            String ajaxUrl = "https://www.gumtree.com/ajax/account/seller/reveal/number/" + ad_id;

            try {
                Document tdoc = Jsoup.connect(ajaxUrl)
                        .header("Accept-Encoding", "gzip, deflate, sdch, br")
                        .header("accept", "application/json, text/javascript, */*; q=0.01")
                        .header("accept-language", "en-US,en;q=0.8")
                        .ignoreContentType(true)
                        .header("x-gumtree-token", token)
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36")
                        .referrer("https://www.gumtree.com/p/samsung/samsung-galaxy-s7-32gb-unlocked-gold-or-silver-or-black-very-good-condition-with-warranty-/1199706972")
                        .timeout(1200000)
                        .followRedirects(true)
                        .get();
                contact = tdoc.text().split("\"")[7];
            } catch (Exception e) {

            }
            ///////////////
            String adTitle = doc.getElementsByClass("space-mbs").get(0).getElementsByTag("h1").get(0).text();

            String address = doc.getElementsByClass("space-mbs").get(0).getElementsByClass("ad-location").get(0).text();

            String adText = doc.getElementsByClass("ad-description").get(0).text();

//                            ///// Deleting existing products ///
            String queryCheck = "DELETE FROM " + this.ItemsTable + " WHERE ad_id = ?";
            PreparedStatement st = connection.prepareStatement(queryCheck);
            st.setString(1, ad_id);
            int rst = st.executeUpdate();
/////////////////////////////

            try {
                Statement stmtInsert = connection.createStatement();
                stmtInsert.execute("set names 'utf8mb4'");
                String sql = "INSERT INTO " + this.ItemsTable + " (ad_id,name,contact,ad_title,address,source_link,category,adText,other)"
                        + "VALUES(?,?,?,?,?,?,?,?,?)";

                PreparedStatement pstmt = connection.prepareStatement(sql);
                // Set the values
                pstmt.setString(1, ad_id);
                pstmt.setString(2, name);
                pstmt.setString(3, contact);
                pstmt.setString(4, adTitle);
                pstmt.setString(5, address);
                pstmt.setString(6, adLink);
                pstmt.setString(7, domainURL_old);
                pstmt.setString(8, adText);
                pstmt.setString(9, tag);
                // Insert
                pstmt.executeUpdate();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("DB Error.");
            }

//                                     System.exit(1);
        } catch (Exception e) {
            Statement stmtInsert = connection.createStatement();
            stmtInsert.execute("set names 'utf8mb4'");
            String sql = "INSERT INTO " + this.urlsTable + " (url, tag, status)"
                    + "VALUES(?,?,?)";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            // Set the values
            pstmt.setString(1, adLink);
            pstmt.setString(2, tag);
            pstmt.setInt(3, 2);
            pstmt.executeUpdate();
            System.out.println("------" + e.getMessage() + "------");
        }
    }

    private void scrapeUrls() {
        ///// Deleting existing products ///
        try {
            if (!tag.isEmpty()) {
                String queryCheck = "DELETE FROM " + this.urlsTable + " WHERE tag = ?";
                PreparedStatement st = connection.prepareStatement(queryCheck);
                st.setString(1, tag);
                int rs = st.executeUpdate();
            }
        } catch (Exception e) {
        }
        /////////////////////////////
        try {
            String countries[] = {"england", "northern-ireland", "scotland", "wales"};

            String url = "https://www.gumtree.com/";
            Document doc = Jsoup.connect(url)
                    .header("Accept-Encoding", "gzip, deflate")
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .referrer("http://www.google.com")
                    .timeout(12000)
                    .followRedirects(true)
                    .get();

            Elements categories = doc.getElementsByClass("category-header-panel");
            for (Element cat : categories) {
                String t_tag = cat.attr("data-category-header-panel");
                if (t_tag.equalsIgnoreCase(tag)) {
                    Elements lis = cat.getElementsByTag("li");
                    for (Element li : lis) {
                        String catUrl = "https://www.gumtree.com" + li.getElementsByTag("a").attr("href");
                        for (int c = 0; c < countries.length; c++) {
                            String t_catUrl = catUrl + "/" + countries[c];
                            System.out.println("<====" + countries[c] + "=====>");

                            Document t_doc = Jsoup.connect(t_catUrl)
                                    .header("Accept-Encoding", "gzip, deflate")
                                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                                    .referrer("http://www.google.com")
                                    .timeout(12000)
                                    .followRedirects(true)
                                    .get();
                            Elements cities = t_doc.getElementsByClass("space-mlm").get(0).getElementsByClass("space-mrxs");

                            for (Element city : cities) {
                                String c_url = "https://www.gumtree.com" + city.attr("href");
                                //System.out.println(c_url);
                                this.saveUrls(c_url);
                            }
                            // System.exit(1);

                        }
                    }
                }
                //System.out.println();
            }

        } catch (IOException ex) {

        }
    }

    private void saveUrls(String url) throws IOException, NumberFormatException {
        //////////////////////////////

        Document tt_doc = Jsoup.connect(url)
                .header("Accept-Encoding", "gzip, deflate")
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                .referrer("http://www.google.com")
                .timeout(12000)
                .followRedirects(true)
                .get();

        //
        String total_ads = tt_doc.getElementsByClass("h1-responsive").get(0).text().split(" ")[0];
        total_ads = total_ads.trim();
        total_ads = total_ads.replaceAll(",", "");
        int tads = Integer.parseInt(total_ads);

        if (tads > 1500 && tt_doc.getElementsByClass("space-mlm").size() > 0) {

            System.err.println("----------");
            System.err.println(url);
            System.err.println("count:" + tads);
            System.err.println("---------");

            try {
                Elements items_sub_cat = null;
                if (tt_doc.getElementsByClass("space-mlm").size() > 1) {
                    items_sub_cat = tt_doc.getElementsByClass("space-mlm").get(1).getElementsByClass("space-mrxs");
                } else {
                    items_sub_cat = tt_doc.getElementsByClass("space-mlm").get(0).getElementsByClass("space-mrxs");
                }
                for (Element sc : items_sub_cat) {
                    String sc_name = "https://www.gumtree.com" + sc.attr("href");
                    this.saveUrls(sc_name);
                }
            } catch (Exception e) {
                System.err.println("Exception in recursion");
            }

        } else {
            // save in Databse
            System.err.println("count:" + tads + " => " + url);
            try {
                Statement stmtInsert = connection.createStatement();
                stmtInsert.execute("set names 'utf8'");
                String sql = "INSERT INTO " + this.urlsTable + " (url,tag)"
                        + "VALUES(?,?)";

                PreparedStatement pstmt = connection.prepareStatement(sql);
                // Set the values
                pstmt.setString(1, url);
                pstmt.setString(2, tag);
                // Insert 
                pstmt.executeUpdate();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("DB Error.");
            }
            // System.exit(1);
        }
        //////////////////////////////
    }

    public void start() {
        System.out.println("Starting " + tag);
        if (t == null) {
            t = new Thread(this, tag);
            t.start();
        }
    }

    @Override
    public void run() {
        System.out.println("Running " + pageFrom + " to " + pageTo);

        this.startScraping();

        System.out.println("Thread " + tag + " exiting.");
    }

}
