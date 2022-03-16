package petclinic.api.client.owner.datamodel;

import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class Pet {

    @Expose(serialize = false)
    private final int id;

    @Expose
    private final String name;

    @Expose
    private final String birthDate;

    @Expose
    private final Type type;

    @Expose
    private final List<Visit> visits;
}
