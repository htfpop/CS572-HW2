import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.util.*;

public class compile {

    final static String folder = "logs\\archive6_new\\";
    final static String fetchCsv = "fetch_usatoday.csv";
    final static String urlsCsv = "urls_usatoday.csv";
    final static String visitCsv = "visit_usatoday.csv";

    final static int ONE_KB = 1024;
    final static int TEN_KB = 1024*10;
    final static int ONE_HUNDRED_KB = 1024*100;
    final static int ONE_MB = 1024*1000;

    public int fetchAttempted = 0;
    public int fetchSucceeded = 0;
    public int fetchFailedAbort = 0;
    public int numThreads;
    public int totalURLExtracted = 0;
    public int totalOutlinks = 0;
    public int status_200 = 0;
    public int status_301 = 0;
    public int status_302 = 0;
    public int status_303 = 0;
    public int status_307 = 0;
    public int status_308 = 0;
    public int status_401 = 0;
    public int status_403 = 0;
    public int status_404 = 0;
    public int status_500 = 0;
    public int status_502 = 0;
    public int status_1001 = 0;
    public int status_UNDEF = 0;
    public Set<String> fetchURLS;
    public Set<String> uniqueURLExtracted;
    public Set<String> uniqueURLWithinNews;
    public Set<String> uniqueURLOutsideNews;
    Map<Integer,Integer> statusCodes; // <StatusCode:Occurrence>
    HashMap<String, Integer> fileSizes;
    HashMap<String, Integer> contentTypes;

    public compile() throws FileNotFoundException {
        this.numThreads = 16; // For this value, see Main.java:67 (int numberOfCrawlers = 16;)
        this.fetchURLS = new HashSet<String>();
        this.fetchAttempted = 0;
        this.fetchSucceeded = 0;
        this.fetchFailedAbort = 0;
        this.totalURLExtracted = 0;
        this.uniqueURLExtracted = new HashSet<String>();
        this.uniqueURLWithinNews = new HashSet<String>();
        this.uniqueURLOutsideNews = new HashSet<String>();
        this.statusCodes = new HashMap<>();
        this.fileSizes = new HashMap<>();
        this.contentTypes = new HashMap<>();
    }

    public static void main(String[] args) throws IOException, CsvException {
        compile c = new compile();

        readFetch(c);

        readUrls(c);

        readVisit(c);

        print(c);
    }

    public static void readVisit(compile c) throws IOException, CsvException
    {
        CSVReader reader = new CSVReader(new FileReader(folder+visitCsv));
        List<String[]> csvLines = reader.readAll();
        Iterator<String[]> iter = csvLines.iterator();

        // Skip Header [URL:Indicator]
        String[] hdr = iter.next();

        // Iterate through list
        while(iter.hasNext())
        {
            String[] csvEntry = iter.next();
            String url = csvEntry[0];
            int size = Integer.parseInt(csvEntry[1]);
            int outlinks = Integer.parseInt(csvEntry[2]);
            String contentType = csvEntry[3];

            if( size < ONE_KB )
            {
                c.fileSizes.put("A", c.fileSizes.getOrDefault("A", 0) + 1);
            }
            else if(size < TEN_KB)
            {
                c.fileSizes.put("B", c.fileSizes.getOrDefault("B", 0) + 1);
            }
            else if(size < ONE_HUNDRED_KB)
            {
                c.fileSizes.put("C", c.fileSizes.getOrDefault("C", 0) + 1);
            }
            else if(size < ONE_MB)
            {
                c.fileSizes.put("D", c.fileSizes.getOrDefault("D", 0) + 1);
            }
            else {
                c.fileSizes.put("E", c.fileSizes.getOrDefault("E", 0) + 1);
            }

            // Push content type onto HM
            c.contentTypes.put(contentType, c.contentTypes.getOrDefault(contentType, 0) + 1);

            // CSCI 572: #total urls extracted is the sum of all values in column 3 of visit.csv ?
            c.totalOutlinks += outlinks;

        }

        reader.close();

    }
    public static void readUrls(compile c) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new FileReader(folder+urlsCsv));
        List<String[]> csvLines = reader.readAll();
        Iterator<String[]> iter = csvLines.iterator();

        // Skip Header [URL:Indicator]
        String[] hdr = iter.next();

        // Iterate through list
        while(iter.hasNext())
        {
            String[] csvEntry = iter.next();
            String url = csvEntry[0];

            c.uniqueURLExtracted.add(url);

            if(url.startsWith("https://www.usatoday.com") || url.startsWith("http://www.usatoday.com"))
            {
                c.uniqueURLWithinNews.add(url);
            }
            else
            {
                c.uniqueURLOutsideNews.add(url);
            }

        }

        c.totalURLExtracted = csvLines.size() - 1; //Subtract 1 from header

        reader.close();

    }

    public static void readFetch(compile c) throws IOException, CsvException {
        CSVReader reader = new CSVReader(new FileReader(folder+fetchCsv));
        List<String[]> csvLines = reader.readAll();

        // Skip Header [URL:Status]
        for(int i = 1; i < csvLines.size(); i++)
        {
            String[] csvEntry = csvLines.get(i);
            String url = csvEntry[0];
            int status = Integer.parseInt(csvEntry[1]);
            if(c.fetchURLS.contains(url))
            {
                System.out.printf("[WARN] - Duplicate in fetch_usatoday.csv || %s\n",url);
            }

            //System.out.printf("url: %s || Status: %d\n", url, Integer.valueOf(status));
            c.fetchURLS.add(url);

            // For determining success and failure anything over 2XX is failed
            if (status < 300 && status >= 200)
                c.fetchSucceeded += 1;
            else
                c.fetchFailedAbort += 1;

            c.statusCodes.put(status, c.statusCodes.getOrDefault(status, 0) + 1);

        }

        c.fetchAttempted = c.fetchURLS.size();

        reader.close();

    }
    public static void print(compile c)
    {
        System.out.println("Name: Christopher K. Leung");
        System.out.println("USC ID: 2965-7518-69");
        System.out.println("News site crawled: www.usatoday.com");
        System.out.printf("Number of threads: %d", c.numThreads);
        System.out.println();
        System.out.println();
        System.out.println("Fetch statistics:");
        System.out.println("================");
        System.out.printf("# Fetches attempted: %d\n",c.fetchAttempted);
        System.out.printf("# Fetches succeeded: %d\n", c.fetchSucceeded);
        System.out.printf("# Fetches failed or aborted: %d\n", c.fetchFailedAbort);
        System.out.printf("# Fetches status UNDEF: %d\n", c.status_UNDEF);
        System.out.println();
        System.out.println("Outgoing URLs:");
        System.out.println("==============");
        System.out.printf("Total URLs extracted: %d\n",c.totalOutlinks); // CSCI-572 # of total URLS is sum(column3) of visit.csv
        //System.out.printf("Total URLs extracted: %d\n",c.totalURLExtracted);
        //System.out.printf("Total outlinks: %d\n",c.totalOutlinks);
        System.out.printf("# Unique URLs extracted: %d\n", c.uniqueURLExtracted.size());
        System.out.printf("# Unique URLs within News Site: %d\n", c.uniqueURLWithinNews.size());
        System.out.printf("# Unique URLs outside News Site: %d\n", c.uniqueURLOutsideNews.size());
        System.out.println();
        System.out.println("Status Codes:");
        System.out.println("=============");

        Object[] keys = c.statusCodes.keySet().toArray();
        Arrays.sort(keys);
        for(Object key: keys)
        {
            int k = (int) key;
            int v = c.statusCodes.get(k);

            String prefix = determinePrefix(k);
            System.out.printf("%s %d\n",prefix, v);
        }
        System.out.println();
        System.out.println("File Sizes:");
        System.out.println("===========");
        System.out.printf("< 1KB: %d\n", c.fileSizes.get("A"));
        System.out.printf("1KB ~ <10KB: %d\n", c.fileSizes.get("B"));
        System.out.printf("10KB ~ <100KB: %d\n", c.fileSizes.get("C"));
        System.out.printf("100KB ~ <1MB: %d\n", c.fileSizes.get("D"));
        System.out.printf(">= 1MB: %d\n", c.fileSizes.get("E"));
        System.out.println();
        System.out.println("Content Types:");
        System.out.println("==============");
        Object[] types = c.contentTypes.keySet().toArray();
        Arrays.sort(types);
        for(Object k: types)
        {
            System.out.printf("%s %d\n",k, c.contentTypes.get(k));
        }
    }

    public static String determinePrefix(int k)
    {
        String prefix = "";

        switch(k)
        {
            case 200:
                prefix = "200 OK:";
                break;
            case 201:
                prefix = "201 Created:";
                break;
            case 202:
                prefix = "202 Accepted:";
                break;
            case 203:
                prefix = "203 Non-Authoritative Information:";
                break;
            case 204:
                prefix = "204 No Content:";
                break;
            case 205:
                prefix = "205 Reset Content:";
                break;
            case 206:
                prefix = "206 Partial Content:";
                break;
            case 207:
                prefix = "207 Multi-Status:";
                break;
            case 208:
                prefix = "208 Already Reported:";
                break;
            case 209:
                prefix = "209 IM Used:";
                break;

            case 300:
                prefix = "300 Multiple Choices:";
                break;
            case 301:
                prefix = "301 Moved Permanently:";
                break;
            case 302:
                prefix = "302 Moved Temporarily:";
                break;
            case 303:
                prefix = "303 See Other:";
                break;
            case 304:
                prefix = "304 Not Modified:";
                break;
            case 305:
                prefix = "305 Use Proxy:";
                break;
            case 306:
                prefix = "306 Switch Proxy:";
                break;
            case 307:
                prefix = "307 Temporary Redirect:";
                break;
            case 308:
                prefix = "308 Permanent Redirect:";
                break;

            case 400:
                prefix = "400 Bad Request:";
                break;
            case 401:
                prefix = "401 Unauthorized:";
                break;
            case 403:
                prefix = "403 Forbidden:";
                break;
            case 404:
                prefix = "404 Not Found:";
                break;

            case 500:
                prefix = "500 Internal Server Error:";
                break;

            case 502:
                prefix = "502 Bad Gateway:";
                break;

            case 1001:
                prefix = "1001 Going Away:";
                break;

            case 1008:
                prefix = "1008 Slow Connection:";
                break;

            default:
                prefix = String.format("%d - UNDEFINED",k);
        }

        return prefix;
    }









    /*
    public static void main(String[] args) throws FileNotFoundException {
        Set<String> set = new HashSet<>();

        for(int i =0; i < 16; i++)
        {
            int curThread = i+1;
            String file = String.format("logs\\Thread%d_log.txt",curThread);

            System.out.printf("Current File: %s\n",file);

            try
            {
                Scanner scan = new Scanner(new File(file));
                while(scan.hasNextLine())
                {
                    String line = scan.nextLine();
                    //System.out.println(scan.nextLine());
                    String[] split = line.split(";");

                    if(set.contains(split[0]))
                    {
                        System.out.printf("Already in set!! - %s\n", split[0]);
                    }
                    else {
                        set.add(split[0]);
                    }

                }
            } catch(FileNotFoundException e)
            {
                System.out.printf("Cannot open file! %s\n", file);
                System.exit(-200);
            }
        }
        System.out.printf("DONE... Set size - %d",set.size());

    }
    */
}
