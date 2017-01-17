package uk.co.compendiumdev.javafortesters.webcrawl.urlchecker;

public class URLStatusCheckResult {

    private final String absoluteURL;
    private int responseCode;
    private long checkedMillis;
    private boolean hasBeenChecked;
    private Exception exceptionThrown;

    public URLStatusCheckResult(String absoluteURL) {
        this.absoluteURL = absoluteURL;
        hasBeenChecked = false;
        checkedMillis = System.currentTimeMillis();
        this.responseCode = 0;
        this.exceptionThrown = null;
    }

    public int responseCode() {
        return this.responseCode;
    }

    public boolean hasBeenChecked(){
        return hasBeenChecked;
    }

    public boolean wasCheckedSuccessfully(){
        return (exceptionThrown==null);
    }

    public Exception getExceptionThrown(){
        return exceptionThrown;
    }

    public long timeChecked() {
        return checkedMillis;
    }

    public void successfulCheck(int responseCode) {
        this.responseCode = responseCode;
        this.hasBeenChecked = true;
    }

    public void exceptionCheck(Exception e) {
        this.hasBeenChecked = true;
        this.exceptionThrown = e;
    }
}
