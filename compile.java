import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class compile {
    public int fetchAttempted;
    public int fetchSucceeded;
    public int fetchFailedAbort;
    public int totalURLExtracted;
    public Set<String> uniqueURLExtracted;
    public Set<String> uniqueURLWithinNews;
    public Set<String> uniqueURLOutsideNews;
    HashMap<String,Integer> statusCodes;
    HashMap<String, Integer> fileSizes;
    HashMap<String, Integer> contentTypes;

    public compile()
    {
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
        CSVReader reader = new CSVReader(new FileReader("logs/fetch_usatoday.csv"));

        List<String[]> line = reader.readAll();
        HashMap<String, Integer> hm = new HashMap<>();

        for(int i = 1; i < line.size(); i ++)
        {
            String[] l = line.get(i);
            //System.out.printf("URL %s - Status %d\r\n", l[0], Integer.valueOf(l[1]));
            if(hm.containsKey(l[0]))
            {
                System.out.printf("Found existing key: %s\r\n",l[0]);
            }
            hm.put(l[0], Integer.valueOf(l[1]));
        }
        System.out.printf("hm size: %d", hm.size());

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
