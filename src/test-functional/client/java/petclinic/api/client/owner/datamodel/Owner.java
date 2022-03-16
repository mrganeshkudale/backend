package petclinic.api.client.owner.datamodel;

import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Builder(toBuilder = true)
@Setter
public class Owner {

    @Expose(serialize = false)
    private Integer id;

    @Expose
    private String firstName;

    @Expose
    private String lastName;

    @Expose
    private String address;

    @Expose
    private String city;

    @Expose
    private String telephone;

    @Expose
    private List<Pet> pets;
}
