package uk.co.compendiumdev.javafortesters.webcrawl;

import uk.co.compendiumdev.javafortesters.webcrawl.urlchecker.StdOutLogger;

/**
 * Created by Alan on 10/04/2017.
 */
public class CheckBodyForText {
    private final String message;
    private final String whenPageContainsText;

    public CheckBodyForText(String message, String whenPageContainsText) {
        this.message = message;
        this.whenPageContainsText = whenPageContainsText;
    }

    public void check(HttpBrowser browser, StdOutLogger stdOutLogger) {
        if(browser.lastresponse.body().contains(whenPageContainsText)){
            stdOutLogger.logStdOut(message);
        }
    }
}
