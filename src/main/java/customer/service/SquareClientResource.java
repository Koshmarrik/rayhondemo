package customer.service;

import org.apache.log4j.Logger;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Overrides restlet ClientResource to put error body in log when error occures
 *
 * Created by roman rasskazov on 12.06.2015.
 */
public class SquareClientResource extends ClientResource {

    private Logger log = Logger.getLogger(getClass());

    public SquareClientResource(String link) {
        super(link);
    }

    @Override
    /**
     * Puts errors body in log, runs initial implementation
     */
    public void doError(Status errorStatus) {
        System.out.println("Error Occured with code " + errorStatus.getCode());
        System.out.println(getResponse().getEntityAsText());
        log.error("Error Occured with code " + errorStatus.getCode());
        log.error(getResponse().getEntityAsText());
        super.doError(errorStatus);
    }

}
