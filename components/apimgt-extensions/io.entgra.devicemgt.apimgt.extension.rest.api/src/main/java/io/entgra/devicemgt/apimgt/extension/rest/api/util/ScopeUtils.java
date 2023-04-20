package io.entgra.devicemgt.apimgt.extension.rest.api.util;

/**
 * This class represents the data that are required to register
 * the oauth application.
 */
public class ScopeUtils {

    private String key;
    private String name;
    private String roles;
    private String description;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toJSON() {
        String jsonString = "{\n" +
                "   \"name\":\" " + key + "\",\n" +
                "   \"displayName\":\" " + name + "\",\n" +
                "   \"description\":\" " + description + " \",\n" +
                "   \"bindings\":[\n" +
                "      \" " + roles + " \"\n" +
                "   ]\n" +
                "}";
        return jsonString;
    }
}