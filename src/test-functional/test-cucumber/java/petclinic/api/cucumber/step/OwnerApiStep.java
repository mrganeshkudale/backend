package petclinic.api.cucumber.step;

import org.fedex.test.common.EnumUtility;
import org.fedex.test.common.EnumUtilityException;
import org.fedex.test.common.api.ApiAction;
import org.fedex.test.common.api.rest.exception.InvalidResponseException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import petclinic.api.client.owner.OwnerApiClientException;
import petclinic.api.cucumber.TestContext;
import petclinic.api.data.OwnerDataHelper;
import petclinic.api.data.OwnerField;

public class OwnerApiStep extends OwnerApiCommonStep {

    private final OwnerDataHelper ownerDataHelper;

    public OwnerApiStep(TestContext testContext) {
        super(testContext);
        ownerDataHelper = this.testContext.getOwnerDataHelper();
    }

    @Given("an Owner is present in the system")
    public void anOwnerIsPresentInTheSystem() throws OwnerApiClientException {
        //Setting as request because this step is written as a pre-requisite step
        ownerRequest = client.createOwner(ownerDataHelper.withMandatoryFields().build());
        testContext.addOwnerIdForDeletion(ownerRequest.getId());
    }

    @Given("an Owner request is created")
    public void anOwnerRequestIsPresent() {
        ownerRequest = ownerDataHelper.withMandatoryFields().build();
    }

    @And("the Owner's {string} value is changed to {string}")
    public void theOwnerSIsUpdatedTo(String fieldName, String fieldValue) throws EnumUtilityException {
        OwnerField ownerField = EnumUtility.lookup(OwnerField.class, fieldName.toUpperCase());
        switch (ownerField) {
            case FIRSTNAME:
                ownerRequest = ownerRequest.toBuilder().firstName(fieldValue).build();
                break;
            case ADDRESS:
                ownerRequest = ownerRequest.toBuilder().address(fieldValue).build();
                break;
            default:
                throw new IllegalArgumentException(String.format("This step does not accept %s field", fieldName));
        }
    }

    @When("a/an {string} request is submitted for the owner")
    public void aRequestIsSubmittedForTheOwner(String action) throws OwnerApiClientException,
                                                                     InvalidResponseException, EnumUtilityException {
        ApiAction apiAction = EnumUtility.lookup(ApiAction.class, action.toUpperCase());
        switch (apiAction) {
            case CREATE:
                testContext.setOwnerRequest(ownerRequest);
                testContext.setOwnerResponse(client.createOwner(ownerRequest));
                testContext.addOwnerIdForDeletion(testContext.getOwnerResponse().getId());
                break;
            case GET:
                testContext.setOwnerResponse(client.getOwner(ownerRequest.getId()));
                break;
            case UPDATE:
                testContext.setOwnerRequest(ownerRequest);
                client.updateOwner(ownerRequest);
                testContext.setOwnerResponse(client.getOwner(ownerRequest.getId()));
                break;
            case DELETE:
                testContext.setOwnerRequest(ownerRequest);
                client.deleteOwner(ownerRequest.getId());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + apiAction);
        }
    }

    @When("User requests to get existing Owners")
    public void userRequestsToGetExistingOwners() throws InvalidResponseException {
        testContext.setOwners(client.getOwners());
    }
}
