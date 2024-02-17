import com.opencsv.CSVWriter;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import java.io.*;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.Set;
import org.apache.http.Header;

public class BasicCrawler extends WebCrawler {
    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|CSS|css|EXE|GIF|MP3)$");
    CrawlStat myCrawlStat;

    public BasicCrawler() {
        myCrawlStat = new CrawlStat();
    }

    /**
     * You should implement this function to specify whether the given url
     * should be crawled or not (based on your crawling logic).
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();

        // Ignore the url if it has an extension that matches our defined set
        if (IMAGE_EXTENSIONS.matcher(href).matches()) {
            logger.warn(String.format("No visit since pattern matched not allowed - %s",href));

            // CSCI-572: All the URLs (including repeats) that were discovered s = URL;N_OK
            String[] out = new String[]{url.getURL(), "N_OK"};
            try {
                write2csv("urls", out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return false;
        }

        // Check if valid domain, else skip
        if(href.startsWith("https://www.usatoday.com") || href.startsWith("http://www.usatoday.com") )
        {
            // CSCI-572: All the URLs (including repeats) that were discovered s = URL;OK
            String[] out = new String[]{url.getURL(), "OK"};
            try {
                write2csv("urls", out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        else {
            // CSCI-572: All the URLs (including repeats) that were discovered s = URL;N_OK
            String[] out = new String[]{url.getURL(), "N_OK"};
            try {
                write2csv("urls", out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return false;
        }
    }

    /**
     * This function is called when a page is fetched and ready to be processed
     * by our program.
     */
    @Override
    public void visit(Page page) {
        logger.info("Visited: {}", page.getWebURL().getURL());
        myCrawlStat.incFetchedPages();

        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        String domain = page.getWebURL().getDomain();
        String path = page.getWebURL().getPath();
        String subDomain = page.getWebURL().getSubDomain();
        String parentUrl = page.getWebURL().getParentUrl();
        String anchor = page.getWebURL().getAnchor();
        int statusCode = page.getStatusCode();
        String[] out;

//        //CSCI-572: The URLs it attempts to fetch s: "URL;status"
//        String[] out = new String[]{url, String.valueOf(statusCode)};
//        try {
//            write2csv("fetch", out);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        //Downloading
        // Success (2XX)
        if (statusCode >= 200 && statusCode < 300)
        {
            myCrawlStat.incTotalSuccess();
            if (page.getParseData() instanceof HtmlParseData) {
                HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                String text = htmlParseData.getText();
                String html = htmlParseData.getHtml();
                Set<WebURL> links = htmlParseData.getOutgoingUrls();
                myCrawlStat.incTotalLinks(links.size());

//                try {
//                    myCrawlStat.incTotalTextSize(htmlParseData.getText().getBytes("UTF-8").length);
//                } catch (UnsupportedEncodingException ignored) {
//                    // Do nothing
//                }

                String contentType = page.getContentType();

                //CSCI-572: should we display "text/html;charset=UTF
                if(contentType.contains("text/html"))
                {
                    contentType = contentType.split(";")[0];
                }

                // CSCI-572: the files it successfully downloads s: "URL;size;links;type"
                out = new String[]{url, String.valueOf(page.getContentData().length), String.valueOf(links.size()), contentType};
                try {
                    write2csv("visit", out);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                myCrawlStat.incProcessedPages();
            }
            else {
                System.out.println("Processing Binary Data");
                String contentType = page.getContentType();

                // Primary Binary Filter
                if (contentType.contains("text/html") || contentType.contains("image") ||
                        contentType.contains("application/pdf") || contentType.contains("doc") )
                {
                    int outlinks = page.getParseData().getOutgoingUrls().size();
                    out = new String[]{url, String.valueOf(page.getContentData().length), String.valueOf(outlinks), contentType};

                    try {
                        write2csv("visit", out);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    myCrawlStat.incProcessedPages();
                }
                else
                {
                    int outlinks = page.getParseData().getOutgoingUrls().size();
                    out = new String[]{url, String.valueOf(page.getContentData().length), String.valueOf(outlinks), contentType};

                    try {
                        write2csv("NULL", out);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }

//            Header[] responseHeaders = page.getFetchResponseHeaders();
//            if (responseHeaders != null) {
//                System.out.println("Response headers:");
//                for (Header header : responseHeaders) {
//                    System.out.println(header.getName() + "," + header.getValue());
//                }
//            }

        }

        System.out.println("\n==========================\n");
    }

    public void dumpMyData() {
        int id = getMyId();
        // You can configure the log to output to file
        logger.info("Crawler {} > Fetched Pages: {}", id, myCrawlStat.getFetchedPages());
        logger.info("Crawler {} > Processed Pages: {}", id, myCrawlStat.getTotalProcessedPages());
        logger.info("Crawler {} > Total Links Found: {}", id, myCrawlStat.getTotalLinks());
        logger.info("Crawler {} > Total Text Size: {}", id, myCrawlStat.getTotalTextSize());
    }

    /**
     * This function is called by controller to get the local data of this crawler when job is
     * finished
     */
    @Override
    public Object getMyLocalData() {
        return myCrawlStat;
    }

    /**
     * This function is called by controller before finishing the job.
     * You can put whatever stuff you need here.
     */
    @Override
    public void onBeforeExit()
    {
        dumpMyData();
    }

    public void write2File() throws IOException {
        String csv1_text = "Thread"+getMyId()+"_csv1.txt";
        String csv2_text = "Thread"+getMyId()+"_csv2.txt";
        String csv3_text = "Thread"+getMyId()+"_csv3.txt";

        try {
            // CSV 1 Output
            fWriter(myCrawlStat.stack_csv1, csv1_text);
        } catch (IOException e)
        {
            System.out.printf("An error occurred in thread %d - Writing to csv1.txt",getMyId());
            System.exit(-1000);
        }

        try {
            // CSV 2 Output
            fWriter(myCrawlStat.stack_csv2, csv2_text);
        }catch (IOException e)
        {
            System.out.printf("An error occurred in thread %d - Writing to csv2.txt",getMyId());
            System.exit(-1000);
        }

        try {
            // CSV 3 Output
            fWriter(myCrawlStat.stack_csv3, csv3_text);
        }catch (IOException e)
        {
            System.out.printf("An error occurred in thread %d - Writing to csv3.txt",getMyId());
            System.exit(-1000);
        }

    }

    public void fWriter(Stack<String> stack, String outfile) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter("logs\\" + outfile));
        int stackSize = myCrawlStat.stackSize(stack);
        for(int i = 0; i < stackSize ; i++)
        {
            pw.println(myCrawlStat.stackPop(stack));
        }
        pw.flush();
        pw.close();
    }

    public void write2csv(String csvType, String[] write) throws IOException {
        String outfile = switch (csvType) {
            case "fetch" -> "logs/fetch_usatoday.csv";
            case "visit" -> "logs/visit_usatoday.csv";
            case "urls" -> "logs/urls_usatoday.csv";
            default -> "logs/UNDETERMINED.csv";
        };

        CSVWriter csv = new CSVWriter(new FileWriter(outfile, true));
        csv.writeNext(write);
        csv.close();
    }

    @Override
    protected void onUnexpectedError(String urlStr, int statusCode, String contentType, String description)
    {
        logger.warn("Skipping URL: {}, StatusCode: {}, {}, {}", urlStr, statusCode, contentType, description);

        String[] out = new String[]{urlStr, String.valueOf(statusCode)};
        try {
            write2csv("fetch", out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Redirect (3XX)
        if (statusCode >= 300 && statusCode < 400)
        {
        myCrawlStat.incTotalFailedOrAborted();

        }
        // Client Failure (4XX)
        else if (statusCode >= 400 && statusCode < 500)
        {
            myCrawlStat.incTotalFailedOrAborted();

        }
        // Server Failure (5XX)
        else if (statusCode >= 500 && statusCode < 600)
        {
            myCrawlStat.incTotalFailedOrAborted();

        }
        else
        {
            myCrawlStat.incTotalFailedOrAborted();
        }
    }

    @Override
    protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription)
    {
        //CSCI-572: The URLs it attempts to fetch s: "URL;status"
        String[] out = new String[]{webUrl.getURL(), String.valueOf(statusCode)};
        try {
            write2csv("fetch", out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}// BasicCrawler

