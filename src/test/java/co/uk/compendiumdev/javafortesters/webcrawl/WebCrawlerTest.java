package co.uk.compendiumdev.javafortesters.webcrawl;

import org.junit.Test;
import uk.co.compendiumdev.javafortesters.webcrawl.URLScanQueue;
import uk.co.compendiumdev.javafortesters.webcrawl.urlchecks.URLPathEquals;

public class WebCrawlerTest {

    @Test
    public void scanAWebSite() throws Exception {
		URLScanQueue urlsq = new URLScanQueue();


        // http://www.useragentstring.com/pages/Safari/

        // iphone
        urlsq.setUserAgent( "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7");

        // ipad 4
        //urlsq.setUserAgent("Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25");

        // iphone
        //urlsq.setDimensions(new Dimension(320, 480));
        // iphone 4 640×960
       //urlsq.setDimensions(new Dimension(640, 960));
        //  iPhone 5 is 640×1136
        //urlsq.setDimensions(new Dimension(640, 1136));

        // ipad 4 is 2048x1536
        //urlsq.setDimensions(new Dimension(2048, 1536));



        urlsq.setRootURL("http://eviltester.com");

        urlsq.setReportComment("Run as iphone");

		urlsq.followExternal(false);
        urlsq.cacheReturnCodeOfExternal(true);
        //TODO: expand subdomain to be 'in scope when' - starts with, matches regex
        urlsq.setSubDomainURL("http://eviltester.com");
		urlsq.restrictToSubURL(true);
        //  TODO: create 'out of scope when' - starts with, matches regex

        // https://site.com/m/stop?url=https%3A%2F%2Fwww.Full+HTML+Redirect
        urlsq.internalIgnoreWhen(new URLPathEquals("/m/stop", "Redirect to external site and leave mobile"));


        // cookies not used after using JSoup yet
        urlsq.setCookie("session","value", "domain", "/", null, true);
//        JSESSIONID+.
//        000bvkj
  // Path    /
  // secure yes
  // http yes

        urlsq.checkAlternateLinks(true);
		urlsq.checkStyleSheetsExist(true);
		urlsq.checkScriptFilesExist(true);
		urlsq.checkForMissingImages(true);
        urlsq.skipDuplicates(true);
        urlsq.followNotVisibleLinks(true);
        urlsq.checkStatusOnLinksOnce(true);
        urlsq.skipHashLinks(true);  // TODO: should really check for fragments on the page when we scan it
        urlsq.setReportDirectoryAs("C:\\Users\\Alan\\Documents\\temp");
		urlsq.scanFromRootURL();

        // TODO: create list of pages - ignoring the params
        // TODO: create list of ignored internal pages
        // TODO: write to report not just stdout

        // add an event based architecture to allow
            // pre scan - access to URLScanQue - which has driver etc.
            // pre url visit
            // pre url scan
            // post scan - scan finished
            // pre driver get - allow access to the driver and other things e.g. navigate to url and login etc.
            // create an event model
        // build the reporting functionality on top of this model
        // plan to build a gui on top of this model e.g. allow cancel etc.
        // this would allow us to release it as a 'library' with an example 'app' that uses it e.g. for reporting, or setup from config files etc.

        // TODO: configurable 'stop' point of number of URLs to stop at - currently hard coded as 1500, e.g. limit, or unlimited
        // TODO: BUG currently doesn't handle pdf, zip etc, and treats all these as redirections - which seem random - investigate
        // TODO: redirection report should link to the 'other' wcURL that will be checked
        // TODO: trap all exceptions - have a 'stop if x exceptions in a row'
        // TODO: capture 'actions' or 'events' against pages e.g. scanned 10:32 21/2/2013
        // TODO: add the stuff in a database for easy reporting and parallel access e.g. hsqldb.org
        // TODO: ensure that if url already in DB that it uses the same id every time
        // TODO: log errors against the objects to allow 'retry errors run'
        // TODO: create sessions for runs to compare site over time
        // TODO: complex reporting
        // TODO: report on external links and their statuses
        // TODO: report on internal links and their statuses

	}
	
	
}
