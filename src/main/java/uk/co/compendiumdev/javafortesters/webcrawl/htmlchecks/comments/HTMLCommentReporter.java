package uk.co.compendiumdev.javafortesters.webcrawl.htmlchecks.comments;


import uk.co.compendiumdev.javafortesters.webcrawl.urlchecker.StdOutLogger;

/**
 * Created by Alan on 24/03/2015.
 */
public class HTMLCommentReporter {

    HTMLCommentFinder comments;

    public HTMLCommentReporter(HTMLCommentFinder comments) {

        this.comments = comments;

    }
    

    public void reportTo(StdOutLogger stdOutLogger) {
        String report = "";

        if (comments.foundCount() > 1){
            for (int getComment = 0; getComment < comments.foundCount(); getComment++) {
                report += comments.found(getComment) + "\n";
                report += "===========================\n";
            }
        }

        stdOutLogger.logStdOut(report);
    }
}
