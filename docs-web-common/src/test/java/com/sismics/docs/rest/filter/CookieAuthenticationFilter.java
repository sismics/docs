package com.sismics.docs.rest.filter;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

import javax.ws.rs.core.Cookie;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter to add the authentication token into a cookie.
 *
 * @author jtremeaux
 */
public class CookieAuthenticationFilter extends ClientFilter {
    private String authToken;
    
    public CookieAuthenticationFilter(String authToken) {
        this.authToken = authToken;
    }
    
    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        Cookie cookie = new Cookie(TokenBasedSecurityFilter.COOKIE_NAME, authToken);
        List<Object> cookieList = new ArrayList<Object>();
        cookieList.add(cookie);
        if (authToken != null) {
            request.getHeaders().put("Cookie", cookieList);
        }
        ClientResponse response = getNext().handle(request);
        if (response.getCookies() != null) {
            cookieList.addAll(response.getCookies());
        }
        return response;
    }

}
