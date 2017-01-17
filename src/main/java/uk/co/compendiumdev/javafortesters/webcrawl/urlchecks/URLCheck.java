package uk.co.compendiumdev.javafortesters.webcrawl.urlchecks;


import java.net.URL;

public interface URLCheck {
    boolean appliesTo(URL urlToFollow);

    String becauseReason();
}
