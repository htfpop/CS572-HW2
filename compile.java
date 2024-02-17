import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.Scanner;

public class compile {

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
}
