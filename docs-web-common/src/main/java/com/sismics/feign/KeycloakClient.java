package com.sismics.feign;

import com.sismics.feign.model.KeycloakCertKeys;
import feign.RequestLine;

public interface KeycloakClient {

    @RequestLine("GET /protocol/openid-connect/certs")
    KeycloakCertKeys getCert();
}
