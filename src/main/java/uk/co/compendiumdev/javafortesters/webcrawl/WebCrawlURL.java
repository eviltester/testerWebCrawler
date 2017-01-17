package uk.co.compendiumdev.javafortesters.webcrawl;

import uk.co.compendiumdev.javafortesters.webcrawl.urlchecker.StdOutLogger;
import uk.co.compendiumdev.javafortesters.webcrawl.urlchecker.URLChecker;
import uk.co.compendiumdev.javafortesters.webcrawl.urlchecker.URLStatusCheckResult;

import java.util.ArrayList;
import java.util.List;

public class WebCrawlURL implements StdOutLogger {

    private final String guid;
    private final StringBuilder log;
    private String absoluteUrl;
	private ArrayList <String>fromUrls;
	private ArrayList <String>undisplayedUrls;
	private boolean scannedInThisSession;
	private String redirectedFrom;
    private List<URLStatusCheckResult> urlStatusChecks;
    private String redirectedTo;

    public WebCrawlURL(String absoluteUrl) {
        this.guid = System.currentTimeMillis() + "_" + System.nanoTime();
		this.absoluteUrl = absoluteUrl;
		this.fromUrls = new ArrayList <String>();
		this.undisplayedUrls = new ArrayList <String>();
		this.redirectedFrom ="";
        this.redirectedTo="";
        this.urlStatusChecks = new ArrayList<URLStatusCheckResult>();
        this.log = new StringBuilder();
	}

    public String getGUID(){
        return guid;
    }

	public String absoluteURL() {
		return this.absoluteUrl;
	}

	public void addFrom(String fromCurrentURL) {
		if(!this.fromUrls.contains(fromCurrentURL)){
			this.fromUrls.add(fromCurrentURL);
		}
	}

    public List<String> from(){
        return fromUrls;
    }

	public void setAsScannedInThisSession() {
		this.scannedInThisSession = true;
	}

	public boolean hasBeenScannedInThisSession() {
		return this.scannedInThisSession;
	}
	
	public void gotoAndFollowRedirects(HttpBrowser browser){
		browser.get(this.absoluteURL());
		
		String driverURL = browser.getCurrentUrl();
		
		// get to the correct page
		if((this.absoluteUrl.length()!= driverURL.length()) || (!this.absoluteURL().equals(driverURL))){
			// redirect occurred
			this.redirectedFrom = this.absoluteUrl;
            this.redirectedTo = browser.getCurrentUrl();
            logStdOut("REDIRECT: " + this.redirectedFrom + " to " + this.redirectedTo);
		}		
	}

	public boolean redirected() {
		return !this.redirectedFrom.equals("");
	}

    public String redirectedFrom() {
        return this.redirectedFrom;
    }

    public String redirectedTo() {
        return this.redirectedTo;
    }

	public void hasUnDisplayedLink(String href) {
		if(!this.undisplayedUrls.contains(href)){
			this.undisplayedUrls.add(href);
		}
	}

	public URLStatusCheckResult checkPageStatus() throws Exception {
        URLStatusCheckResult check = URLChecker.checkThisURL(this.absoluteUrl, this);
        urlStatusChecks.add(check);

        return check;
	}

    public URLStatusCheckResult getLastStatusCheck(){

        URLStatusCheckResult returnCheck = null;
        long lastMillis=0;

        for(URLStatusCheckResult statusCheck : urlStatusChecks){
            if(statusCheck.timeChecked() > lastMillis){
                lastMillis = statusCheck.timeChecked();
                returnCheck = statusCheck;
            }
        }

        return returnCheck;
    }

    public void logStdOut(String s) {
        this.log.append(String.format("%s%n",s));
        System.out.println(s);
    }

    public String getLog() {
        return this.log.toString();
    }
}
