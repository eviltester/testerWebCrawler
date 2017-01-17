package uk.co.compendiumdev.javafortesters.webcrawl.urlchecks;


import java.net.URL;

public class URLPathEquals implements URLCheck{
    private final String urlCheck;
    private final String reason;
    private String appliedReason;

    public URLPathEquals(String urlCheck, String reason) {
        this.urlCheck = urlCheck;
        this.reason = reason;
        this.appliedReason = reason;
    }

    public boolean appliesTo(URL urlToFollow) {

        boolean retCode = false;


        if(urlToFollow.getPath().equalsIgnoreCase(urlCheck)){
            this.appliedReason = this.reason + " : " + urlToFollow.getPath();
            retCode = true;
        }

        return retCode;
    }

    public String becauseReason() {
        return this.appliedReason;
    }
}
