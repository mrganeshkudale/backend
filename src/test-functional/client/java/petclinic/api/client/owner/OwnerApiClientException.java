package petclinic.api.client.owner;

import org.fedex.test.common.api.rest.exception.InvalidResponseException;
import com.google.gson.Gson;
import petclinic.api.client.owner.datamodel.Owner;

public class OwnerApiClientException extends Exception {
    public OwnerApiClientException(String message, Owner request, InvalidResponseException ex) {
        super(message + " for request : " + new Gson().toJson(request), ex);
    }
}
