package com.sismics.docs.rest.resource;

import org.glassfish.jersey.message.internal.ReaderWriter;

import javax.json.JsonObject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * When a JSON-based exception is thrown but a JSON response is not expected,
 * set the media type of the response as plain text. 
 */
@Provider
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class DocsMessageBodyWriter implements MessageBodyWriter<JsonObject> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public void writeTo(JsonObject o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        ReaderWriter.writeToAsString(o.toString(), entityStream, MediaType.TEXT_PLAIN_TYPE);
    }
}
