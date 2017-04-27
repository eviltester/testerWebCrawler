package uk.co.compendiumdev.javafortesters.webcrawl.urlchecker;


import java.net.HttpURLConnection;
import java.net.URL;

public class URLChecker {

    public static URLStatusCheckResult checkThisURL(String absoluteURL, StdOutLogger tempLogger) throws Exception {

        URLStatusCheckResult returnCheck = new URLStatusCheckResult(absoluteURL);

        // handle cdn type urls
        if(absoluteURL.startsWith("//")){
            absoluteURL = "http:" + absoluteURL;
        }

        try {
            URL myURL = new URL(absoluteURL);
            HttpURLConnection myConnection = (HttpURLConnection)myURL.openConnection();
            int responseCode = myConnection.getResponseCode();

            if(responseCode!=200){
                tempLogger.logStdOut("*** ERROR PAGE RESPONSE CODE (" + responseCode + ") for " + absoluteURL);
            }else{
                tempLogger.logStdOut("Status Code ("+ responseCode + ") for " + absoluteURL);
            }

            returnCheck.successfulCheck(responseCode);

        } catch (Exception e) {
            tempLogger.logStdOut("Page Status Error: " + e);
            returnCheck.exceptionCheck(e);
            e.printStackTrace();
            //do not throw the exception
            // throw e;
        }

        return returnCheck;
    }

}
