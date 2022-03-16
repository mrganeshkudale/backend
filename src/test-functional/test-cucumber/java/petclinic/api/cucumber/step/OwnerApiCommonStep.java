package petclinic.api.cucumber.step;

import petclinic.api.client.owner.OwnerApiClient;
import petclinic.api.client.owner.datamodel.Owner;
import petclinic.api.cucumber.TestContext;

public class OwnerApiCommonStep {

    protected TestContext testContext;
    protected Owner ownerRequest;
    protected Owner ownerResponse;
    protected OwnerApiClient client;

    public OwnerApiCommonStep(TestContext testContext) {
        this.testContext = testContext;
        this.client = testContext.getClient();
    }
}
