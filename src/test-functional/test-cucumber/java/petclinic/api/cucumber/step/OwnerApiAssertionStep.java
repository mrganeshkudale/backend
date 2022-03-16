package petclinic.api.cucumber.step;

import org.fedex.test.common.api.rest.exception.InvalidResponseException;
import io.cucumber.java.en.Then;
import petclinic.api.client.owner.datamodel.Owner;
import petclinic.api.cucumber.TestContext;
import petclinic.api.data.OwnerField;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OwnerApiAssertionStep extends OwnerApiCommonStep{

    private final List<Owner> owners;

    public OwnerApiAssertionStep(TestContext testContext) {
        super(testContext);
        ownerRequest = this.testContext.getOwnerRequest();
        ownerResponse = this.testContext.getOwnerResponse();
        owners = this.testContext.getOwners();
    }

    @Then("a new Owner is created in the system")
    public void aNewOwnerIsCreatedInTheSystem() {
        assertThat(ownerResponse).isNotNull();
        assertThat(ownerResponse.getId()).as(OwnerField.ID.toString()).isGreaterThan(0);
        assertThat(ownerResponse.getFirstName()).as(OwnerField.FIRSTNAME.toString()).isEqualTo(ownerRequest.getFirstName());
    }

    @Then("a list of Owners is returned")
    public void aListOfOwnersIsReturned() {
        assertThat(owners).as("List of Owners").isNotEmpty();
    }

    @Then("every Owner has a valid {string} value")
    public void everyOwnerHasAFieldValue(String fieldName) {
        try {
            OwnerField ownerField = OwnerField.valueOf(fieldName.toUpperCase());
            assertThat(owners).as("List of owners").allSatisfy(owner -> {
                switch (ownerField) {
                    case ID:
                        assertThat(owner.getId()).as(ownerField.getFieldName()).isGreaterThan(0);
                        break;
                    case FIRSTNAME:
                        assertThat(owner.getFirstName()).as(ownerField.getFieldName()).isNotBlank();
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("This step does not accept %s field", fieldName));
                }
            });
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for OwnerField enum: " + fieldName);
        }
    }

    @Then("the Owner's {string} value should be {string}")
    public void theOwnerSFieldShouldBe(String fieldName, String fieldValue) {
        try {
            OwnerField ownerField = OwnerField.valueOf(fieldName.toUpperCase());
            switch (ownerField) {
                case FIRSTNAME:
                    assertThat(ownerResponse.getFirstName()).as(ownerField.getFieldName()).isEqualTo(fieldValue);
                    break;
                case ADDRESS:
                    assertThat(ownerResponse.getAddress()).as(ownerField.getFieldName()).isEqualTo(fieldValue);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("This step does not accept %s field", fieldName));
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for OwnerField enum: " + fieldName);
        }
    }

    @Then("the Owner should be deleted")
    public void theOwnerShouldBeDeleted() throws InvalidResponseException {
        assertThat(client.getOwners()).as("List of owners").noneSatisfy(owner ->
            assertThat(owner.getId()).as(OwnerField.ID.getFieldName()).isEqualTo(ownerRequest.getId()));
    }
}
