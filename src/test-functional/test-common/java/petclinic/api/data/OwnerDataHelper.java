package petclinic.api.data;

import petclinic.api.client.owner.datamodel.Owner;
import petclinic.api.client.owner.datamodel.Pet;
import petclinic.api.client.owner.datamodel.Type;

import java.util.Collections;

public class OwnerDataHelper implements DataHelper<Owner> {

    private final Owner.OwnerBuilder request;

    public OwnerDataHelper() {
        this.request = Owner.builder();
    }

    public OwnerDataHelper withMandatoryFields() {
        request.firstName("Automation").lastName("User").address("New Address").city("Anywhere").telephone("12345678");
        return this;
    }

    public OwnerDataHelper withOnePet() {
        request.pets(Collections.singletonList(
            Pet.builder()
                .name("Rocket")
                .type(Type.builder().id(1).name("Dog").build())
                .birthDate("2020-01-22T17:18:38.401Z")
                .build()));
        return this;
    }

    @Override
    public Owner build() {
        return request.build();
    }
}
