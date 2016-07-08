package customer.service;

import customer.rest.PicoSign;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;

/**
 * Class runs PicoSign post image API in separate thread
 *
 * Created by roman rasskazov on 16.06.2015.
 */
public class PicoSignService {

    private Logger log = Logger.getLogger(getClass());

    private PicoSignService(){
    }

    private static PicoSignService instance;

    public static PicoSignService getInstance() {
        if (instance == null){
            instance = new PicoSignService();
        }
        return instance;
    }

    /**
     * Runs PicoSign post image method in separate thread
     */
    public void postImage(final String imagePath) throws IOException {
        RestScheduler.runTask(new Runnable() {
            public void run() {
                try {
                    System.out.println("running PicoSign post image task: " + imagePath);
                    log.info("running PicoSign post image task: " + imagePath);

                    PicoSign.getInstance().postImage(new File(imagePath));

                    System.out.println("Image posted: " + imagePath);
                    log.info("Image posted: " + imagePath);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });

    }

}
