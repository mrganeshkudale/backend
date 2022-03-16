package petclinic.api.owner.junit4;

import org.fedex.test.common.api.rest.exception.InvalidResponseException;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import petclinic.api.client.owner.OwnerApiClient;
import petclinic.api.client.owner.OwnerApiClientException;
import petclinic.api.client.owner.datamodel.Owner;
import petclinic.api.data.OwnerDataHelper;
import petclinic.api.data.OwnerField;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class BasicTest {

    static String apiUrl;
    static List<Owner> newOwnerList = new ArrayList<>();
    OwnerApiClient client;
    OwnerDataHelper ownerData;

    public BasicTest() {
        ownerData = new OwnerDataHelper();
    }

    @BeforeClass
    public static void getApiUrl() { apiUrl = System.getProperty("apiUrl"); }

    @AfterClass
    public static void cleanTestData(){
        log.info("Cleanup - Deleting owners created during test execution");
        OwnerApiClient client = new OwnerApiClient(apiUrl);
        newOwnerList.forEach( owner -> client.deleteOwner(owner.getId()));
    }

    @Before
    public void init() {
        client = new OwnerApiClient(apiUrl);
    }

    @Test
    @Category(SanityTest.class)
    public void getOwnersReturnsOwnersHavingValidData() throws InvalidResponseException {
        List<Owner> owners = client.getOwners();

        assertThat(owners).as("List of Owners").isNotEmpty();
        assertThat(owners).as("List of owners").allSatisfy(owner -> {
            assertThat(owner.getId()).as(OwnerField.ID.getFieldName()).isGreaterThan(0);
            assertThat(owner.getFirstName()).as(OwnerField.FIRSTNAME.getFieldName()).isNotBlank();
        });
    }

    @Test
    @Category(SanityTest.class)
    public void getOwnerWithValidIdReturnsOwnerData() throws InvalidResponseException {
        Owner owner = client.getOwner(1);

        assertThat(owner.getId()).as(OwnerField.ID.getFieldName()).isEqualTo(1);
        assertThat(owner.getFirstName()).as(OwnerField.FIRSTNAME.getFieldName()).isNotBlank();
    }

    @Test
    @Category(SanityTest.class)
    public void deleteOwnerWithValidIdDeletesOwner() throws OwnerApiClientException, InvalidResponseException {
        Owner createdOwner = client.createOwner(
            Owner.builder()
                .firstName("Automation").lastName("Dummy").address("Test Road")
                .city("Anywhere").telephone("01234567").build()
        );

        assertThat(client.deleteOwner(createdOwner.getId()))
            .as("Delete Owner response status code")
            .isEqualTo(204);

        List<Owner> owners = client.getOwners();
        assertThat(owners).as("List of owners").noneSatisfy(owner ->
            assertThat(owner.getId()).as(OwnerField.ID.getFieldName()).isEqualTo(createdOwner.getId()));
    }

    @Test
    @Category(SanityTest.class)
    public void updateOwnerWithValidIdReturnsUpdatedOwner() throws OwnerApiClientException, InvalidResponseException {
        Owner createdOwner = client.createOwner(
            Owner.builder()
                .firstName("Automation").lastName("Dummy").address("Test Road")
                .city("Anywhere").telephone("01234567").build()
        );
        newOwnerList.add(createdOwner);

        Owner ownerToBeUpdated = createdOwner.toBuilder().city("Nowhere").telephone("98765432").build();

        assertThat(client.updateOwner(ownerToBeUpdated))
            .as("Update Owner response status code")
            .isEqualTo(204);

        Owner updatedOwner = client.getOwner(ownerToBeUpdated.getId());

        assertThat(updatedOwner).as("Updated owner").isNotNull()
            .hasFieldOrPropertyWithValue(OwnerField.ID.getFieldName(), ownerToBeUpdated.getId())
            .hasFieldOrPropertyWithValue(OwnerField.CITY.getFieldName(), ownerToBeUpdated.getCity())
            .hasFieldOrPropertyWithValue(OwnerField.PHONE.getFieldName(), ownerToBeUpdated.getTelephone());
    }

    @Test
    @Category(SanityTest.class)
    public void createOwnerWithoutPetReturnsNewlyCreatedOwner() throws OwnerApiClientException {
        Owner owner = client.createOwner(ownerData.withMandatoryFields().build());
        newOwnerList.add(owner);

        assertThat(owner.getId()).as(OwnerField.ID.getFieldName()).isGreaterThan(0);
        assertThat(owner.getFirstName()).as(OwnerField.FIRSTNAME.getFieldName()).isEqualTo(ownerData.build().getFirstName());
    }

    @Test
    @Category(FailingTest.class)
    public void createOwnerWithoutMandatoryFieldsReturnsNewlyCreatedOwner() throws OwnerApiClientException {
        Owner requestedOwner = ownerData.withMandatoryFields().build();
        //Remove address value to create incomplete request
        requestedOwner.setAddress("");
        Owner owner = client.createOwner(requestedOwner);
        newOwnerList.add(owner);

        assertThat(owner.getId()).as(OwnerField.ID.getFieldName()).isGreaterThan(0);
        assertThat(owner.getFirstName()).as(OwnerField.FIRSTNAME.getFieldName()).isEqualTo(ownerData.build().getFirstName());
    }

    @Test
    @Category(FailingTest.class)
    public void createOwnerWithOnePetReturnsNewlyCreatedOwner() throws OwnerApiClientException {
        Owner owner = client.createOwner(ownerData.withMandatoryFields().withOnePet().build());
        newOwnerList.add(owner);

        assertThat(owner.getId()).as(OwnerField.ID.getFieldName()).isGreaterThan(0);
        assertThat(owner.getPets()).as("Owner's pets").isNotEmpty();
    }
}
