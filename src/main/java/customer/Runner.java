package customer;

import customer.service.RestScheduler;
import customer.service.SquareService;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Application entry point.
 * Manages console input and general logic
 *
 * Created by roman rasskazov on 11.06.2015.
 */
public class Runner {

    private static Logger log = Logger.getLogger(Runner.class);

    public static final int MODE1 = 1;
    public static final int MODE2 = 2;

    private static final Map<String, String> descriptionMap;
    static {
        Map<String, String> aMap = new HashMap<String,String>();
        aMap.put("spadeId", "10");
        descriptionMap = Collections.unmodifiableMap(aMap);
    }

    private static final String MODE2_ARG = "--mode=2";

    public static void main(String[] args) {
        try {

//            PicoSignService.getInstance().postImage("d:\\TCM74_01.png");//TCM74_01
            int modeValue = MODE1;
            if (args.length > 0){
                if (args[0].equals(MODE2_ARG)){
                    modeValue = MODE2;
                }
            }
            final int mode = modeValue;

            System.out.println("Running in mode:" + mode);
            log.info("Running in mode:" + mode);


            System.out.println("Receive items...");
            log.info("Receive items...");

            SquareService squareService = SquareService.getInstance();
            squareService.printItems(System.out, mode, descriptionMap);

            System.out.println("Check for updates...");
            log.info("Check for updates...");

            RestScheduler.schedule(new Runnable() {
                public void run() {
                    //run process to check updated with or without description check depending on mode
                    if (mode == MODE1) {
                        SquareService.getInstance().checkChanges(System.out);
                    } else if (mode == MODE2){
                        SquareService.getInstance().checkChangesWithDescription(System.out, descriptionMap);
                    }
                }
            });

            //read input. Use it as id to request item untill input is empty. Exit on empty input.
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true){
                String id = br.readLine();
                if (id == null || id.length() == 0) {
                    System.out.println("exit");
                    log.info("exit");

                    RestScheduler.releaseThread();
                    System.exit(0);
                }
                squareService.getItemAndPrint(System.out, id);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage(), e);
        }
    }

}
