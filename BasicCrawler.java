import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.*;
import java.util.regex.Pattern;
import java.util.Set;
import org.apache.http.Header;

import java.util.concurrent.atomic.AtomicInteger;

// -need- to implement visit() and shouldVisit()
public class BasicCrawler extends WebCrawler {

    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|CSS|EXE|GIF|MP3)$");

    //private static final Pattern ALLOWED = Pattern.compile(".*(JPG|jpg|PNG|png|JPEG|jpeg|PDF|pdf|DOC|doc|HTML|html|HTM|htm)");
    CrawlStat myCrawlStat;

    //private final AtomicInteger numPagesSeen;


    public BasicCrawler() {
        //this.numPagesSeen = 0;
        myCrawlStat = new CrawlStat();
    }

    /**
     * Creates a new crawler instance.
     *
     * @param numPagesSeen This is just an example to demonstrate how you can pass objects to crawlers. In this
     * example, we pass an AtomicInteger to all crawlers and they increment it whenever they see a url which points
     * to an image.
     */
    public BasicCrawler(AtomicInteger numPagesSeen) {
         //this.numPagesSeen= numPagesSeen;
    }


    /**
     * You should implement this function to specify whether the given url
     * should be crawled or not (based on your crawling logic).
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        // Ignore the url if it has an extension that matches our defined set of image extensions.
        if (IMAGE_EXTENSIONS.matcher(href).matches()) {
            //numSeenImages.incrementAndGet();
            return false;
        }

        Header[] responseHeaders = referringPage.getFetchResponseHeaders();
        if(href.startsWith("https://www.usatoday.com") || href.startsWith("http://www.usatoday.com") )
        {
            if (responseHeaders != null) {
                String content = String.valueOf(responseHeaders[1]);
                //return ALLOWED.matcher(content).matches();
                return content.contains("text/html") ||
                        content.contains("image/jpeg") || content.contains("image/png") ||
                        content.contains("application/pdf") ||
                        content.contains("doc");
            }
        }

        return false;

        // only do pages in our course site
        //return href.startsWith("https://www.usatoday.com") || href.startsWith("http://www.usatoday.com") || ALLOWED.matcher(href).matches();
    }

    /**
     * This function is called when a page is fetched and ready to be processed
     * by our program.
     */
    @Override
    public void visit(Page page) {
        logger.info("Visited: {}", page.getWebURL().getURL());
        myCrawlStat.incProcessedPages();

        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        String domain = page.getWebURL().getDomain();
        String path = page.getWebURL().getPath();
        String subDomain = page.getWebURL().getSubDomain();
        String parentUrl = page.getWebURL().getParentUrl();
        String anchor = page.getWebURL().getAnchor();
        int statusCode = page.getStatusCode();

        System.out.println("Docid: " + docid);
        System.out.println("URL: " + url);
        System.out.println("Domain: " +  domain);
        System.out.println("Sub-domain: " + subDomain);
        System.out.println("Path: "+ path);
        System.out.println("Parent page: " + parentUrl);
        System.out.println("Anchor text: " +  anchor);
        System.out.println("Status " +  statusCode);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            myCrawlStat.incTotalLinks(links.size());
            try {
                myCrawlStat.incTotalTextSize(htmlParseData.getText().getBytes("UTF-8").length);
            } catch (UnsupportedEncodingException ignored) {
                // Do nothing
            }

            logger.debug("Text length: {}", text.length());
            logger.debug("Html length: {}", html.length());
            logger.debug("Number of outgoing links: {}", links.size());

            // Append to log
            String add = url +';'+ statusCode;
            myCrawlStat.stackPush(add);

            // We dump this crawler statistics after processing every 50 pages
            if ((myCrawlStat.getTotalProcessedPages() % 50) == 0) {
                dumpMyData();
            }
        }

        Header[] responseHeaders = page.getFetchResponseHeaders();
        if (responseHeaders != null) {
            System.out.println("Response headers:");
            for (Header header : responseHeaders) {
                //logger.debug("\t{}: {}", header.getName(), header.getValue());
                System.out.println(header.getName() + "," + header.getValue());
            }
        }

        System.out.println("\n==========================\n");
    }

    public void dumpMyData() {
        int id = getMyId();
        // You can configure the log to output to file
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
    public void onBeforeExit() {
        dumpMyData();
        try {
            write2File();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write2File() throws IOException {
        String file = "Thread"+getMyId()+"_log.txt";

        try {
            PrintWriter pw = new PrintWriter(new FileWriter("logs\\" + file, true));
            int stackSize = myCrawlStat.stackSize();
            for(int i = 0; i < stackSize ; i++)
            {
                pw.println(myCrawlStat.stackPop());
            }
            pw.flush();
            pw.close();

        } catch (IOException e)
        {
            System.out.printf("An error occurred in thread %d",getMyId());
            System.exit(-1000);
        }
    }


}// BasicCrawler

