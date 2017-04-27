package uk.co.compendiumdev.javafortesters.webcrawl;


import uk.co.compendiumdev.javafortesters.webcrawl.htmlchecks.comments.HTMLCommentFinder;
import uk.co.compendiumdev.javafortesters.webcrawl.htmlchecks.comments.HTMLCommentReporter;
import uk.co.compendiumdev.javafortesters.webcrawl.urlchecker.StdOutLogger;
import uk.co.compendiumdev.javafortesters.webcrawl.urlchecker.URLChecker;
import uk.co.compendiumdev.javafortesters.webcrawl.urlchecker.URLStatusCheckResult;
import uk.co.compendiumdev.javafortesters.webcrawl.urlchecks.URLCheck;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class URLScanQueue {

	ArrayList <WebCrawlURL> urls;
	private boolean followExternal;
	private boolean restrictToSubURL;
	private URL jURL;
    private URL subDomainURL;
	private String currentURL;
	private boolean checkAlternateLinks;
	private boolean checkStyleSheetsExist;
	private boolean checkScriptFilesExist;
	private boolean checkForMissingImages;
	private boolean followAlternateLinks;
	private boolean reportAnyForms;
	private boolean reportAnyHiddenFields;
	private boolean reportAnyFileUploads;
    private boolean skipDuplicates;
    private boolean followNotVisible;
    private boolean weShouldCacheLinkStatusChecks;

    private Map<String, URLStatusCheckResult> linkStatusCache;
    private Map<String, URLStatusCheckResult> externalLinkStatusCache;
    private boolean skipHashLinks;
    private boolean cacheReturnCodeOfExternal;
    private String tempDirectoryRootPath;
    private TemporaryReporting temporaryReporting;
    private String userAgent;

    private String reportComment;
    private List<URLCheck> internalIgnoreChecks;
    private Map<String, String> cookieJar;
    private List<CheckBodyForText> bodyChecks = new ArrayList<CheckBodyForText>();
    private boolean reportHTMLComments;

    private String htmlforCurrentURLScan() {
        StringBuffer details = new StringBuffer();
        String newLine = String.format("%n");

        details.append("<h1>" + currentURL + "</h1>" + newLine);


        details.append("<p>" + "User Agent: " + this.userAgent + "</p>" + newLine);
    //    if(this.dimension!=null)
    //        details.append("<p>" + "Dimensions: " + this.dimension.getWidth() + " x " + this.dimension.getHeight()  + "</p>" + newLine);

        details.append("<p>" + this.reportComment  + "</p>" + newLine);

        return details.toString();
    }

    public URLScanQueue(){
		urls = new ArrayList<WebCrawlURL>();
		followExternal = false;
		jURL=null;
        subDomainURL=null;
		currentURL = "";
		checkAlternateLinks = true;
		checkStyleSheetsExist = true;
		checkScriptFilesExist = true;
		checkForMissingImages = true;
		followAlternateLinks = true;
		reportAnyForms = true;
		reportAnyHiddenFields = true;
		reportAnyFileUploads = true;
        skipDuplicates = true;
        skipHashLinks = true;
        cacheReturnCodeOfExternal = false;
        reportHTMLComments = false;

        bodyChecks = new ArrayList<CheckBodyForText>();

        reportComment = "";

        // when false we only follow what is visible on screen load
        followNotVisible = false;

        // when true we pretend to cache the links
        weShouldCacheLinkStatusChecks = false;
		linkStatusCache = new HashMap<String, URLStatusCheckResult>();
        externalLinkStatusCache = new HashMap<String, URLStatusCheckResult>();
        this.userAgent = "";

        // internal checks for ignore when
        this.internalIgnoreChecks = new ArrayList<URLCheck>();

        this.cookieJar = new HashMap<String, String>();
	}

	public void setRootURL(String aURL) {
		currentURL = aURL;
		try { this.jURL= new URL(aURL); } catch (Exception e) {}
	}

	public void followExternal(boolean followExternalOrNot) {
		this.followExternal = followExternalOrNot;
	}
	
	public void checkScriptFilesExist(boolean checkScriptFiles) {
		this.checkScriptFilesExist = checkScriptFiles;
	}
	
	public void checkStyleSheetsExist(boolean checkStylesheets) {
		this.checkStyleSheetsExist = checkStylesheets;
	}	
	
	
	public void checkForMissingImages(boolean checkForMissingImages) {
		this.checkForMissingImages = checkForMissingImages;
	}	
	
	public void checkAlternateLinks(boolean checkAlternate) {
		this.checkAlternateLinks = checkAlternate;
	}	

	public void restrictToSubURL(boolean restrict) {
		// Do we allow the user to stay on the same domain but
		// have to be under the root url
		this.restrictToSubURL = restrict;
	}

    public void setSubDomainURL(String subDomainURL) throws MalformedURLException {
        this.subDomainURL = new URL(subDomainURL);
    }

    public void skipDuplicates(boolean skipDupes) {
        this.skipDuplicates = skipDupes;
    }

    public void followNotVisibleLinks(boolean followNotVisible) {
        this.followNotVisible = followNotVisible;
    }

    public void checkStatusOnLinksOnce(boolean checkLinksStatusOnce) {
        this.weShouldCacheLinkStatusChecks = checkLinksStatusOnce;
    }

	public void scanFromRootURL() throws Exception {


        HttpBrowser browser = new HttpBrowser();

        if(this.userAgent.length()>0){
            browser.setUserAgent(this.userAgent);
        }


        browser.addCookies(cookieJar);


        temporaryReporting.createTemporaryIndexWith(htmlforCurrentURLScan());




        WebCrawlURL wcURL = new WebCrawlURL(currentURL);

		WebCrawlURL rootURL = followThisURLLater(wcURL, currentURL,"ROOT");
		
		rootURL.gotoAndFollowRedirects(browser);
		if(rootURL.redirected()){
			setRootURL(rootURL.absoluteURL());
		}
		scanURL(browser,rootURL);
		
		
		WebCrawlURL queuedURL;
		Integer URLcount = 0;
		
		while(URLcount < 1500){
			System.out.println(URLcount + " of " + urls.size());
			queuedURL = getAnUnscannedURL();
			if(queuedURL== null)
				break;
			scanURL(browser,queuedURL);
			URLcount++;
		}

		temporaryReporting.addfinalHTMLT0TemporaryIndex();
	}



    private WebCrawlURL getAnUnscannedURL(){

        for( WebCrawlURL queuedURL : urls){
            if(!queuedURL.hasBeenScannedInThisSession()){
                return queuedURL;
            }
        }

        // TODO: Ugly, do something else
        return null;
	}
	
	private void scanURL(HttpBrowser browser, WebCrawlURL wcURL) throws IOException {

        StdOutLogger stdOutLogger = wcURL;

        stdOutLogger.logStdOut("SCANNING URL: " + wcURL.absoluteURL());



        try{
		    wcURL.gotoAndFollowRedirects(browser);

            if(wcURL.redirected()){
                if(urlAlreadyQueued(wcURL.redirectedTo())){
                   stdOutLogger.logStdOut("Redirected to something already Queued - skipping checks this time to avoid a duplicate check");
                   wcURL.setAsScannedInThisSession(); // so we don't follow it
                   if(!isURLExternal(new URL(wcURL.redirectedTo()))){
                      // outputScreenshotToTemporaryReport(browser, wcURL);
                   }
                   return;
                }
            }

            if(this.checkAlternateLinks){
                checkAndQueueAlternateLinks(browser, stdOutLogger);
            }

            if(this.checkStyleSheetsExist){
                checkStyleSheetLinksExist(browser, stdOutLogger);
            }

            if(this.checkScriptFilesExist){
                checkScriptFilesExist(browser, stdOutLogger);
            }

            if(this.checkForMissingImages){
                checkImagesExist(browser, stdOutLogger);
            }

            // Check business rules

            if(this.reportAnyForms){
                reportOnAnyFormsOnThePageForManualChecking(browser, stdOutLogger);
            }

            if(this.reportAnyFileUploads){
                reportOnAnyFileUploadsOnThePage(browser, stdOutLogger);
            }

            // any fields with on_x events
            // TODO: report on on_x events

            if(this.reportAnyHiddenFields){
                reportOnAnyHiddenFormsOnThePage(browser, stdOutLogger);
            }

            if(this.reportHTMLComments){
                reportOnAnyHTMLCommentsOnThePage(browser, stdOutLogger);
            }

            for(CheckBodyForText checker : bodyChecks){
                checker.check(browser, stdOutLogger);
            }

            queueLinksToFollowLater(browser, wcURL, stdOutLogger);

        }catch(Exception e){
            stdOutLogger.logStdOut("ERROR Scanning URL " + wcURL.absoluteURL());
            stdOutLogger.logStdOut(e.getMessage());
            e.printStackTrace();
            return;
        }


        if(!isURLExternal(new URL(wcURL.absoluteURL()))){
            outputScreenshotToTemporaryReport(browser, wcURL);
        }

		wcURL.setAsScannedInThisSession();
		
	}

    private void reportOnAnyHTMLCommentsOnThePage(HttpBrowser browser, StdOutLogger stdOutLogger) {
        HTMLCommentFinder comments = new HTMLCommentFinder(browser.getSource());
        comments.findAllComments();

        new HTMLCommentReporter(comments).reportTo(stdOutLogger);
    }

    private void queueLinksToFollowLater(HttpBrowser browser, WebCrawlURL wcURL, StdOutLogger stdOutLogger) throws Exception {
        List<HtmlElement> links = browser.findElements("a[href]");

        for (HtmlElement link : links) {

            String href = link.getAttribute("href");
            URL urlToFollow = null;

            // assume we will follow it
            Boolean follow = true;
            String noFollowReason = "";

            /* only available on actual web implementations of the browser
            if(!link.isDisplayed()){
                if(!followNotVisible){
                    follow = false;
                    noFollowReason += " link not visible to user -";
                    wcURL.hasUnDisplayedLink(href);
                }
            }
            */

            try {
                if(isRelativeURL(href)){
                    href=convertRelativeURLToAbsolute(href);
                }

                urlToFollow = new URL(href);


                // if this is external
                if(isURLExternal(urlToFollow)){
                    if(!this.followExternal){
                        // it is external
                        noFollowReason += " it is external - ";
                        follow = false;
                    }

                    if(!this.cacheReturnCodeOfExternal){
                        getStatusOfLinkWithCache(stdOutLogger, href, externalLinkStatusCache, true);
                    }
                }else{

                    // check for internal ignore rules
                    for(URLCheck check : this.internalIgnoreChecks){
                        if(check.appliesTo(urlToFollow)){
                            follow=false;
                            noFollowReason += check.becauseReason();
                        }
                    }

                }

                if(follow && this.restrictToSubURL){
                    if(!href.startsWith(this.subDomainURL.getProtocol() + "://" + this.subDomainURL.getHost() + this.subDomainURL.getPath())){
                        noFollowReason += " it is not in required path - ";
                        follow = false;
                    }
                }


            } catch (Exception e) {
                follow = false;
                noFollowReason += " malformed url -";
            }


            if(follow){
                followThisURLLater(stdOutLogger, href, wcURL.absoluteURL());
            }else{
                stdOutLogger.logStdOut("DO NOT FOLLOW: " + href + " from " + wcURL.absoluteURL() + " because " + noFollowReason);
            }
        }
    }

    private void reportOnAnyHiddenFormsOnThePage(HttpBrowser browser, StdOutLogger stdOutLogger) {
        List<HtmlElement> formsOnPage = browser.findElements("input[type='hidden']");

        for (HtmlElement webElement : formsOnPage) {
            stdOutLogger.logStdOut("!!! MANUALLY CHECK HIDDEN FIELDS");
            stdOutLogger.logStdOut("!!!     name = " + webElement.getAttribute("name"));
            stdOutLogger.logStdOut("!!!     value = " + webElement.getAttribute("value"));
        }
    }

    private void reportOnAnyFileUploadsOnThePage(HttpBrowser driver, StdOutLogger stdOutLogger) {
        List<HtmlElement> formsOnPage = driver.findElements("input[type='file']");

        for (HtmlElement webElement : formsOnPage) {
            stdOutLogger.logStdOut("!!! MANUALLY TEST FILE UPLOAD");
            stdOutLogger.logStdOut("!!!     name = " + webElement.getAttribute("name"));
            stdOutLogger.logStdOut("!!!     id = " + webElement.getAttribute("id"));
            stdOutLogger.logStdOut("!!!     enctype = " + webElement.getAttribute("enctype"));
            if(!webElement.getAttribute("enctype").equalsIgnoreCase("multipart/form-data")){
                stdOutLogger.logStdOut("***** WARNING file upload not multipart - may not get full file");
            }
            stdOutLogger.logStdOut("!!!     action = " + webElement.getAttribute("action"));
            stdOutLogger.logStdOut("!!!     method = " + webElement.getAttribute("method"));
        }
    }

    private void reportOnAnyFormsOnThePageForManualChecking(HttpBrowser driver, StdOutLogger stdOutLogger) {
        List<HtmlElement> formsOnPage =  driver.findElements("form");

        for (HtmlElement webElement : formsOnPage) {
            stdOutLogger.logStdOut("!!! MANUALLY TEST FORM");
            stdOutLogger.logStdOut("!!!     name = " + webElement.getAttribute("name"));
            stdOutLogger.logStdOut("!!!     id = " + webElement.getAttribute("id"));
            stdOutLogger.logStdOut("!!!     action = " + webElement.getAttribute("action"));
            stdOutLogger.logStdOut("!!!     method = " + webElement.getAttribute("method"));
        }
    }

    private void checkImagesExist(HttpBrowser driver, StdOutLogger stdOutLogger) throws Exception {
        List<HtmlElement> alternateLinks = driver.findElements("img[src]");

        for (HtmlElement webElement : alternateLinks) {

            URLStatusCheckResult linkStatus = getStatusOfLink(stdOutLogger, webElement.getAttribute("src"));

            if(linkStatus.responseCode()!=200){
                stdOutLogger.logStdOut("*** ERROR RESPONSE CODE (" + linkStatus + ") for IMAGE " + webElement.getAttribute("src"));
            }

        }
    }

    private void checkScriptFilesExist(HttpBrowser driver, StdOutLogger stdOutLogger) throws Exception {
        List<HtmlElement> alternateLinks = driver.findElements("script[src]");

        for (HtmlElement webElement : alternateLinks) {

            URLStatusCheckResult linkStatus = getStatusOfLink(stdOutLogger, webElement.getAttribute("src"));

            if(linkStatus.responseCode()!=200){
                stdOutLogger.logStdOut("*** ERROR RESPONSE CODE (" + linkStatus + ") for SCRIPT " + webElement.getAttribute("src"));
            }

        }
    }

    private void checkStyleSheetLinksExist(HttpBrowser driver, StdOutLogger stdOutLogger) throws Exception {
        List<HtmlElement> alternateLinks = driver.findElements("link[rel='stylesheet']");

        for (HtmlElement webElement : alternateLinks) {

            URLStatusCheckResult linkStatus = getStatusOfLink(stdOutLogger, webElement.getAttribute("href"));

            if(linkStatus.responseCode()!=200){
                stdOutLogger.logStdOut("*** ERROR RESPONSE CODE (" + linkStatus + ") for CSS " + webElement.getAttribute("href"));
            }

        }
    }

    private void checkAndQueueAlternateLinks(HttpBrowser driver, StdOutLogger stdOutLogger) throws Exception {
        List<HtmlElement> alternateLinks = driver.findElements("link[rel='alternate']");

        for (HtmlElement webElement : alternateLinks) {

            stdOutLogger.logStdOut("FOUND ALTERNATE LINK: " + webElement.getAttribute("href"));

            URLStatusCheckResult linkStatus = getStatusOfLink(stdOutLogger, webElement.getAttribute("href"));

            if(linkStatus.responseCode()!=200){
                stdOutLogger.logStdOut("*** ERROR RESPONSE CODE (" + linkStatus + ") for ALTERNATE LINK " + webElement.getAttribute("href"));
            }

            if(this.followAlternateLinks){
                followThisURLLater(stdOutLogger, webElement.getAttribute("href"), currentURL + " : ALTERNATE LINK");
            }
        }
    }

    private boolean isURLExternal(URL urlToFollow) {
        return !urlToFollow.getHost().equals(this.jURL.getHost());
    }

    private void outputScreenshotToTemporaryReport(HttpBrowser driver, WebCrawlURL wcURL) throws IOException {
       /* File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        temporaryReporting.copyScreenshot(screenshot, wcURL.getGUID());
        temporaryReporting.createTempReportOn(wcURL);
        */
    }


    private URLStatusCheckResult getStatusOfLink(StdOutLogger stdOutLogger, String aURL) throws Exception {
        return getStatusOfLinkWithCache(stdOutLogger, aURL, linkStatusCache, weShouldCacheLinkStatusChecks);
	}

    private URLStatusCheckResult getStatusOfLinkWithCache(StdOutLogger stdOutLogger, String aURL, Map<String, URLStatusCheckResult> resultCache, boolean shouldICache) throws Exception {
        String absoluteURL = aURL;

        if(isRelativeURL(aURL)){
            absoluteURL=convertRelativeURLToAbsolute(aURL);
        }

        if(shouldICache){
            if(resultCache.containsKey(absoluteURL)){
                URLStatusCheckResult cachedStatus = resultCache.get(absoluteURL);
                stdOutLogger.logStdOut("...cached status of (" + cachedStatus.responseCode() + ") " + absoluteURL);
                return cachedStatus;
            }
        }

        URLStatusCheckResult urlCheck;

        urlCheck = URLChecker.checkThisURL(absoluteURL, stdOutLogger);

        if(shouldICache){
            resultCache.put(absoluteURL, urlCheck);
        }

        return urlCheck;
    }


    private boolean urlAlreadyQueued(String urlToFollow){

        for(WebCrawlURL queuedURL : urls){
            if((queuedURL.absoluteURL().length()==urlToFollow.length()) && queuedURL.absoluteURL().equals(urlToFollow)){
                return true;
            }
        }

        return false;
    }


	private WebCrawlURL followThisURLLater(StdOutLogger stdOutLogger, String thisURL, String fromCurrentURL) throws Exception {
		String urlToFollow = thisURL;
		
		if(isRelativeURL(thisURL)){
			urlToFollow = convertRelativeURLToAbsolute(thisURL);
		}
		
		WebCrawlURL queuedURL=null;
		Boolean foundIt=false;

        // skip # links in here by finding the primary, and add the primary and the first hash link
        String primaryResource="";
        if(skipHashLinks){

            if(thisURL.contains("#")){
                String urlPositions[] = thisURL.split("#");
                primaryResource = urlPositions[0];

                // add the primary Resource
                if(!urlAlreadyQueued(primaryResource)){
                    WebCrawlURL primaryURL = new WebCrawlURL(primaryResource);
                    primaryURL.addFrom(fromCurrentURL);
                    urls.add(primaryURL);
                    foundIt = true;
                    stdOutLogger.logStdOut("Skip Fragment URL " + thisURL);
                    // TODO: should really check for fragments on the page when we scan it
                }else{
                    // primary already added to queue
                    foundIt = true;
                }
            }
        }

        if(!foundIt){
            foundIt = urlAlreadyQueued(urlToFollow);
        }

        // This should be handling duplicates

		if(!foundIt){
			queuedURL = new WebCrawlURL(urlToFollow);
            queuedURL.addFrom(fromCurrentURL);
            int responseCode = queuedURL.checkPageStatus().responseCode();
			if(responseCode != 404){
				urls.add(queuedURL);
                stdOutLogger.logStdOut("NEED TO FOLLOW: " + thisURL + " from " + currentURL);
			}else{
                stdOutLogger.logStdOut("WILL NOT FOLLOW because its a 404: " + thisURL + " from " + currentURL);
            }
        }else{
            stdOutLogger.logStdOut("FOUND A DUPLICATE: " + thisURL);
		}

		return queuedURL;
	}
	
	public boolean isRelativeURL(String href){
        if(href.startsWith("//")){
            return false;
        }
		if(href.startsWith("/")){
			return true;
		}
        if(href.startsWith("./")){
		    // TODO - need current URL to make this absolute so this will probably fail in converstion to absolute
            return true;
        }
        if(href.startsWith("../")){
            // TODO - need current URL to make this absolute so this will probably fail in converstion to absolute
            return true;
        }

		return false;
	}
	
	public String convertRelativeURLToAbsolute(String aRelativeUrl){
	    String host= this.jURL.getHost();
	    if(this.jURL.getPort()!=-1){
	        host = host + ":" + this.jURL.getPort();
        }
		return this.jURL.getProtocol() + "://" + host + aRelativeUrl;
	}


    public void skipHashLinks(boolean skipHashLinks) {
        // # is a fragment, so skip these
        // http://en.wikipedia.org/wiki/Fragment_identifier
        this.skipHashLinks = skipHashLinks;
    }

    public void cacheReturnCodeOfExternal(boolean cacheExternalReturnCode) {
        this.cacheReturnCodeOfExternal = cacheExternalReturnCode;
    }

    public void setReportDirectoryAs(String tempDirectoryPath) {

        temporaryReporting = new TemporaryReporting(tempDirectoryPath);

    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /*
    public void setDimensions(Dimension dimension) {
        this.dimension = dimension;
    }
    */

    public void setReportComment(String s) {
        this.reportComment = s;
    }

    public void internalIgnoreWhen(URLCheck urlCheck) {
        this.internalIgnoreChecks.add(urlCheck);
    }

    public void setCookie(String cookieName, String cookieValue, String cookieDomain, String cookiePath, Date cookieExpiry, boolean cookieSecure ) {
        /*
        Cookie aCookie = new Cookie(cookieName, cookieValue, cookieDomain, cookiePath, cookieExpiry , cookieSecure);
        cookieJar.add(aCookie);
        */
    }

    public void reportAsWhenContains(String message, String whenPageContainsText) {
        bodyChecks.add(new CheckBodyForText(message, whenPageContainsText));
    }

    public void setReportHTMLComments(boolean reportHTMLComments) {
        this.reportHTMLComments = reportHTMLComments;
    }
}
