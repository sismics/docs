package com.sismics.model;

import java.util.List;

public class KeycloakCertKey {
    public String kid;
    public List<String> x5c;

    public KeycloakCertKey() {
    }

    public List<String> getX5c() {
        return x5c;
    }

    public void setX5c(List<String> x5c) {
        this.x5c = x5c;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }
}
