package customer.rest;

import customer.service.SquareClientResource;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import java.io.File;
import java.io.IOException;

/**
 * Class connects to PicoSign API
 *
 * Created by roman rasskazov on 18.06.2015.
 */
public class PicoSign {

    private static final String UPLOAD_URL = "http://api.picosign.com/device/upload/";
    private static final String DEVICE_ID = "0023A7345879";
    private static final String DEVICE_PASS = "picosign";
    private static final String PANEL_ID = "0";
    private static final String DISPLAY = "display";
    private static final String FILE = "file";

    private Client client = new Client(new Context(), Protocol.HTTP);;
    private static PicoSign instance;

    public static PicoSign getInstance() {
        if (instance == null){
            instance = new PicoSign();
        }
        return instance;
    }

    /**
     * Uploads image to PicoSign device
     */
    synchronized public void postImage(File image) throws IOException {
        ClientResource resource = new SquareClientResource(UPLOAD_URL);
        //use the same client for all requests
        resource.setNext(client);
        //authorization
        resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, DEVICE_ID, DEVICE_PASS);
        //image
        Representation file = new FileRepresentation(image, MediaType.IMAGE_ALL);

        //form body
        FormDataSet form = new FormDataSet();
        form.setMultipart(true);
        form.getEntries().add(new FormData(DISPLAY, PANEL_ID));
        form.getEntries().add(new FormData(FILE, file));
        resource.setRequestEntityBuffering(true);
        resource.post(form);
    }
}
