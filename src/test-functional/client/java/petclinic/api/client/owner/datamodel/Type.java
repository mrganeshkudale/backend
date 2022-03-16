package petclinic.api.client.owner.datamodel;

import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Type {

    @Expose(serialize = false)
    private final int id;

    @Expose
    private final String name;
}
