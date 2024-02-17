import ch.qos.logback.classic.Logger;
import com.opencsv.CSVWriter;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        // for our crawling specs 
        CrawlConfig config = new CrawlConfig();

        // Set the folder where intermediate crawl data is stored (e.g. list of urls that are extracted from previously
        // fetched pages and need to be crawled later).
        config.setCrawlStorageFolder("results");

        // Be polite: Make sure that we don't send more than 1 request per second (1000 milliseconds between requests).
        // Otherwise it may overload the target servers.
        config.setPolitenessDelay(800);

        // You can set the maximum crawl depth here.
        // CSCI-572: maximum depth should be set to 16 to ensure that we limit the crawling
        config.setMaxDepthOfCrawling(16);

        // You can set the maximum number of pages to crawl. The default value is -1 for unlimited number of pages.
        // CSCI-572: should be set to 20,000 to ensure a reasonable execution time for this exercise
        config.setMaxPagesToFetch(20000);

        // Should binary data should also be crawled? example: the contents of pdf, or the metadata of images etc
        config.setIncludeBinaryContentInCrawling(true);

        // This config parameter can be used to set your crawl to be resumable
        // (meaning that you can resume the crawl from a previously
        // interrupted/crashed crawl). Note: if you enable resuming feature and
        // want to start a fresh crawl, you need to delete the contents of
        // rootFolder manually.
        config.setResumableCrawling(false);

        // Set this to true if you want crawling to stop whenever an unexpected error
        // occurs. You'll probably want this set to true when you first start testing
        // your crawler, and then set to false once you're ready to let the crawler run
        // for a long time.
        //config.setHaltOnError(true);

        // Instantiate the controller for this crawl.
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);


        // STARTER 'seed'
        //controller.addSeed("https://bytes.usc.edu/cs572/s23-sear-chhh");
        controller.addSeed("https://www.usatoday.com");


        // Number of threads to use during crawling. Increasing this typically makes crawling faster. But crawling
        // speed depends on many other factors as well. You can experiment with this to figure out what number of
        // threads works best for you.
        int numberOfCrawlers = 16;

        // To demonstrate an example of how you can pass objects to crawlers, we use an AtomicInteger that crawlers
        // increment whenever they see a url which points to an image.
        //AtomicInteger numPagesSeen = new AtomicInteger();

        // The factory which creates instances of crawlers.
        //CrawlController.WebCrawlerFactory<BasicCrawler> factory = () -> new BasicCrawler(numPagesSeen);

        // Start the crawl. This is a blocking operation, meaning that your code
        // will reach the line after this only when crawling is finished.
        // GO!!!!
        String[] hdr = new String[]{"URL", "Status"};
        write2csv("fetch", hdr);

        hdr = new String[]{"URL", "Size (bytes)", "# Outlinks", "content-type"};
        write2csv("visit", hdr);

        hdr = new String[]{"URL", "Indicator"};
        write2csv("urls", hdr);

        hdr = new String[]{"URL", "Type"};
        write2csv("NULL", hdr);

        long elapsed = System.nanoTime();
        controller.start(BasicCrawler.class, numberOfCrawlers);
        elapsed = System.nanoTime() - elapsed;

        List<Object> crawlersLocalData = controller.getCrawlersLocalData();
        long totalLinks = 0;
        long totalTextSize = 0;
        int totalProcessedPages = 0;
        for (Object localData : crawlersLocalData) {
            CrawlStat stat = (CrawlStat) localData;
            totalLinks += stat.getTotalLinks();
            totalTextSize += stat.getTotalTextSize();
            totalProcessedPages += stat.getTotalProcessedPages();
        }

        long sec = elapsed/1000000000;
        long min = sec/60;
        sec = sec - min*60;

        System.out.print("Aggregated Statistics:\n");
        System.out.printf("\tProcessed Pages: {%d}\r\n", totalProcessedPages);
        System.out.printf("\tTotal Links found: {%d}\r\n", totalLinks);
        System.out.printf("\tTotal Text Size: {%d}\n", totalTextSize);
        System.out.printf("\tTotal Time %d min %d s\n", min,sec);
        System.out.printf("\tTotal s %d\n", elapsed);

        PrintWriter pw = new PrintWriter(new FileWriter("logs\\" + "aggregate.txt"));
        pw.printf("Aggregated Statistics:\n");
        pw.printf("\tProcessed Pages: {%d}\r\n", totalProcessedPages);
        pw.printf("\tTotal Links found: {%d}\r\n", totalLinks);
        pw.printf("\tTotal Text Size: {%d}\n", totalTextSize);
        pw.printf("\tTotal Time %d min %d s\n", min,sec);
        pw.printf("\tTotal s %d\n", elapsed);
        pw.flush();
        pw.close();
    }

    public static void write2csv(String csvType, String[] write) throws IOException {
        String outfile = switch (csvType) {
            case "fetch" -> "logs\\fetch_usatoday.csv";
            case "visit" -> "logs\\visit_usatoday.csv";
            case "urls" -> "logs\\urls_usatoday.csv";
            default -> "logs\\UNDETERMINED.csv";
        };

        CSVWriter csv = new CSVWriter(new FileWriter(outfile, false));
        csv.writeNext(write);
        csv.close();
    }

}// Main