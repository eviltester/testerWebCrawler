package uk.co.compendiumdev.javafortesters.webcrawl;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by Alan on 17/01/2017.
 */
public class HttpBrowser {
    private String userAgent;
    private Map<String, String> cookieJar;
    Connection.Response lastresponse;
    boolean skipped = false;
    private String source;

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void addCookies(Map<String, String> cookieJar) {
        this.cookieJar = cookieJar;
    }

    public void get(String url) {


        skipped = false;

        try {
            lastresponse = Jsoup.connect(url)
                    .followRedirects(true) //to follow redirects
                    .execute();
        } catch (UnsupportedMimeTypeException e){

            System.out.println("SKIPPING GET BECAUSE IT IS NOT SUPPORTED MIME TYPE " + lastresponse.contentType());
            skipped = true;

        } catch (IOException e) {
            e.printStackTrace();
            skipped = true;
            throw new RuntimeException(e);
        }
    }

    public String getCurrentUrl() {
        return lastresponse.url().toExternalForm();
    }

    public List<HtmlElement> findElements(String searchQuery) {
        List returnElements = new ArrayList<HtmlElement>();

        if(skipped){
            return returnElements;
        }

        try {
            Document doc = lastresponse.parse();
            Elements elements = doc.select(searchQuery);
            ListIterator<Element> searchResults = elements.listIterator();
            while(searchResults.hasNext()){
                Element element = searchResults.next();
                returnElements.add(new HtmlElement(element));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return returnElements;
    }

    public String getSource() {
        if(skipped){
            return "";
        }
        
        return lastresponse.body();
    }
}
