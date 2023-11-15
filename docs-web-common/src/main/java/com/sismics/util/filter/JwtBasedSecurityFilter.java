package com.sismics.util.filter;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.impl.JWTParser;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.util.Base64;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.feign.KeycloakClient;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;
import java.util.UUID;

import static java.util.Optional.ofNullable;

/**
 * This filter is used to authenticate the user having an active session by validating a jwt token.
 * The filter extracts the jwt token stored from Authorization header.
 * It validates the token by calling an Identity Broker like KeyCloak.
 * If validated, the user is retrieved, and the filter injects a UserPrincipal into the request attribute.
 *
 * @author smitra
 */
public class JwtBasedSecurityFilter extends SecurityFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtBasedSecurityFilter.class);
    /**
     * Name of the header used to store the authentication token.
     */
    public static final String HEADER_NAME = "Authorization";

    @Override
    protected User authenticate(final HttpServletRequest request) {
        log.info("Jwt authentication started");
        User user = null;
        String token = extractAuthToken(request).replace("Bearer ", "");
        DecodedJWT jwt = JWT.decode(token);
        if (verifyJwt(jwt, token)) {
            String email = jwt.getClaim("preferred_username").toString();
            UserDao userDao = new UserDao();
            user = userDao.getActiveByUsername(email);
            if (user == null) {
                user = new User();
                user.setRoleId(Constants.DEFAULT_USER_ROLE);
                user.setUsername(email);
                user.setEmail(email);
                user.setStorageQuota(10L);
                user.setPassword(UUID.randomUUID().toString());
                try {
                    userDao.create(user, email);
                    log.info("user created");
                } catch (Exception e) {
                    log.info("Error:" + e.getMessage());
                    return null;
                }
            }
        }
        return user;
    }

    private boolean verifyJwt(final DecodedJWT jwt, final String token) {

        try {
            buildJWTVerifier(jwt).verify(token);
            // if token is valid no exception will be thrown
            log.info("Valid TOKEN");
            return Boolean.TRUE;
        } catch (CertificateException e) {
            //if CertificateException comes from buildJWTVerifier()
            log.info("InValid TOKEN: " + e.getMessage());
            return Boolean.FALSE;
        } catch (JWTVerificationException e) {
            // if JWT Token in invalid
            log.info("InValid TOKEN: " + e.getMessage() );
            return Boolean.FALSE;
        } catch (Exception e) {
            // If any other exception comes
            log.info("InValid TOKEN, Exception Occurred: " + e.getMessage());
            return Boolean.FALSE;
        }
    }

    private String extractAuthToken(final HttpServletRequest request) {
        return ofNullable(request.getHeader("Authorization")).orElse("");
    }

    private RSAPublicKey getPublicKey(DecodedJWT jwt) {
        KeycloakClient client = Feign.builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .logLevel(feign.Logger.Level.BASIC)
                .logger(new Slf4jLogger(KeycloakClient.class))
                .target(KeycloakClient.class, jwt.getIssuer());
        String publicKey = client.getCert().getKeys().stream().filter(k -> Objects.equals(k.getKid(), jwt.getKeyId()))
                .findFirst()
                .map(k -> k.getX5c().get(0))
                .orElse("");
        try {
            var decode = Base64.getDecoder().decode(publicKey);
            var certificate = CertificateFactory.getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(decode));
            return (RSAPublicKey)certificate.getPublicKey();
        } catch (CertificateException ex) {
            return null;
        }
    }

    private JWTVerifier buildJWTVerifier(DecodedJWT jwt) throws CertificateException {
        var algo = Algorithm.RSA256(getPublicKey(jwt), null);
        return JWT.require(algo).build();
    }
}
