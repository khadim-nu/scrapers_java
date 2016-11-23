package scrapers;

import java.io.IOException;

/**
 *
 * @author raath
 *
 * http://stackoverflow.com/questions/819182/how-do-i-get-the-html-code-of-a-web-page-in-php
 *
 * https://developer.yahoo.com/yql/console/#h=select+*+from+html+where+url%3D%22http%3A%2F%2Fwww.ncdd.com%2F%22
 */
public class main {

    public static void main(String[] args) throws IOException {
        String tag = "for-sale";

        int pageFrom = 0;
        int pageTo = 0;

        try {
            pageTo = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        // 16 , 16+50
        //16+50 , 16
        int increment = 50;

        pageFrom = pageTo;
        pageTo = pageFrom + increment;
        new gumtree(tag, pageFrom, pageTo);
        
        pageFrom = pageTo;
        pageTo = pageFrom + increment;
        new gumtree(tag, pageFrom, pageTo);
        
        pageFrom = pageTo;
        pageTo = pageFrom + increment;
        new gumtree(tag, pageFrom, pageTo);
        
        pageFrom = pageTo;
        pageTo = pageFrom + increment;
        new gumtree(tag, pageFrom, pageTo);
        
        pageFrom = pageTo;
        pageTo = pageFrom + increment;
        new gumtree(tag, pageFrom, pageTo);
        
        pageFrom = pageTo;
        pageTo = pageFrom + increment;
        new gumtree(tag, pageFrom, pageTo);
        
        pageFrom = pageTo;
        pageTo = pageFrom + increment;
        new gumtree(tag, pageFrom, pageTo);
        
        pageFrom = pageTo;
        pageTo = pageFrom + increment;
        new gumtree(tag, pageFrom, pageTo);
        
        pageFrom = pageTo;
        pageTo = pageFrom + increment;
        new gumtree(tag, pageFrom, pageTo);
        
        pageFrom = pageTo;
        pageTo = pageFrom + increment;
        new gumtree(tag, pageFrom, pageTo);
        
        
        
        

        System.out.println(pageFrom);
        System.out.println(pageTo);
        // System.exit(1);

        System.out.println("Done.");
    }

}
