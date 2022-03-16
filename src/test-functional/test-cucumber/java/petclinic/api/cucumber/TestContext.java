package petclinic.api.cucumber;

import lombok.Getter;
import lombok.Setter;
import petclinic.api.client.owner.OwnerApiClient;
import petclinic.api.client.owner.datamodel.Owner;
import petclinic.api.data.OwnerDataHelper;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TestContext {

    static List<Integer> ownerIdList = new ArrayList<>();
    private final OwnerApiClient client;
    String apiUrl;
    OwnerDataHelper ownerDataHelper;
    @Setter
    private Owner ownerRequest;
    @Setter
    private Owner ownerResponse;
    @Setter
    private List<Owner> owners;

    public TestContext() {
        apiUrl = System.getProperty("apiUrl");
        client = new OwnerApiClient(apiUrl);
        ownerDataHelper = new OwnerDataHelper();
    }

    public void addOwnerIdForDeletion(Integer ownerId) {
        ownerIdList.add(ownerId);
    }
}
