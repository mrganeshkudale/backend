package petclinic.api.client.owner.datamodel;

import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Visit {


    @Expose(serialize = false)
    private final int id;

    @Expose
    private final String date;

    @Expose
    private final String description;
}
