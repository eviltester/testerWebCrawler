package uk.co.compendiumdev.javafortesters.webcrawl;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TemporaryReporting {
    private final String tempRootPath;
    private final String tempDirectoryPath;
    private final String tempScreensPath;

    public TemporaryReporting(String tempDirectoryPath) {
        this.tempRootPath = tempDirectoryPath + "/linkScanner";

        SimpleDateFormat sdf = new SimpleDateFormat("y_M_d_HH_mm_ss_SSS");
        this.tempDirectoryPath = this.tempRootPath + "/" + sdf.format(new Date());

        File tempDirectory = new File(this.tempDirectoryPath);
        if(tempDirectory.mkdirs()==false)
            throw new RuntimeException("Could not create directory " + tempDirectory.getAbsolutePath());

        this.tempScreensPath = this.tempDirectoryPath + "/screens";
        File screensDirectory = new File(this.tempScreensPath);
        if(screensDirectory.mkdirs()==false)
            throw new RuntimeException("Could not create directory " + screensDirectory.getAbsolutePath());
    }


    // http://stackoverflow.com/questions/106770/standard-concise-way-to-copy-a-file-in-java
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }

    public File getScreenshotFile(String guid) {
        return new File(this.tempScreensPath + "/" + guid + ".png");
    }

    public void copyScreenshot(File screenshot, String guid) throws IOException {
        File newScreenshot = getScreenshotFile(guid);
        copyFile(screenshot, newScreenshot);
    }

    public void createTempReportOn(WebCrawlURL wcURL) {
        StringBuffer urlHTMLReport = new StringBuffer();
        URL aURL=null;

        try {
            aURL = new URL(wcURL.absoluteURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        String newLine = String.format("%n");

        urlHTMLReport.append("<html><head><title>" + aURL.getPath() + "/" + aURL.getFile() + "</title></head>" + newLine);
        urlHTMLReport.append("<body>" + newLine);

        urlHTMLReport.append("<h1>" + aURL.getFile() + "</h1>" + newLine);

        urlHTMLReport.append("<h2>Details</h2>" + newLine);

        urlHTMLReport.append("<p><a href='" + wcURL.absoluteURL() + "'>" + wcURL.absoluteURL() + "</a></p>" + newLine);
        urlHTMLReport.append("<p>" + aURL.getHost() + "</p>" + newLine);
        urlHTMLReport.append("<p>" + aURL.getPath() + "</p>" + newLine);
        urlHTMLReport.append("<p>" + aURL.getFile() + "</p>" + newLine);

        urlHTMLReport.append("<h2>From:</h2>" + newLine);
        urlHTMLReport.append("<ul>" + newLine);

        for(String fromURL : wcURL.from()){
            urlHTMLReport.append("<li><a href='" + fromURL + "'>" + fromURL + "</a></li>" + newLine);
        }
        urlHTMLReport.append("</ul>" + newLine);

        urlHTMLReport.append("<h2>Scanning Log</h2>" + newLine);
        urlHTMLReport.append("<pre>" + newLine);
        urlHTMLReport.append(wcURL.getLog());
        urlHTMLReport.append("</pre>" + newLine);

        urlHTMLReport.append("<img src='screens/" + wcURL.getGUID() + ".png'/>" + newLine);

        urlHTMLReport.append("</body>");
        urlHTMLReport.append("</html>");

        File file = new File(this.tempDirectoryPath + "/" + wcURL.getGUID() + ".htm");

        try {
            // if file doesnt exists, then create it
            if (!file.exists()) {
                    file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(urlHTMLReport.toString());
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        addPageToTemporaryIndex(wcURL);
    }

    private void addPageToTemporaryIndex(WebCrawlURL wcURL) {


        File file = new File(this.tempDirectoryPath + "/tempIndex.htm");
        URL aURL = null;

        try {

            try {
                aURL = new URL(wcURL.absoluteURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));

            String pageFile =  wcURL.getGUID() + ".htm";


            out.println("<li><a href='" + pageFile + "'>" + aURL.getFile() + "</a></li>");
            out.close();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void addfinalHTMLT0TemporaryIndex() {
        File file = new File(this.tempDirectoryPath + "/tempIndex.htm");

        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));

            out.println("</body></html>");
            out.close();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void createTemporaryIndexWith(String s) throws IOException {
        File file = new File(this.tempDirectoryPath + "/tempIndex.htm");

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));

                out.println("<html><head><title>Temporary Index</title></head><body>");
                out.println(s);
                out.println("<ul>");

        out.close();

    }
}
