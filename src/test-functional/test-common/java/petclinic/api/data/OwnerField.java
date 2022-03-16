package petclinic.api.data;

import lombok.Getter;

@Getter
public enum OwnerField {
    ID("id"),
    FIRSTNAME("firstName"),
    ADDRESS("address"),
    CITY("city"),
    PHONE("telephone");

    private final String fieldName;

    OwnerField(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return this.fieldName;
    }
}
