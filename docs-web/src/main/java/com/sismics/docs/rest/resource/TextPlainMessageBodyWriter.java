package com.sismics.docs.rest.resource;

import com.sun.jersey.core.util.ReaderWriter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * MessageBodyWriter personalized to write JSON despite the text/plain MIME type.
 * Used in particuler in return of a posted form, since IE doesn't knw how to read the application/json MIME type.
 * 
 * @author bgamard
 */
@Provider
@Produces(MediaType.TEXT_PLAIN)
public class TextPlainMessageBodyWriter implements
        MessageBodyWriter<JSONObject> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public long getSize(JSONObject array, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(JSONObject jsonObject, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException,
            WebApplicationException {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(entityStream, ReaderWriter.getCharset(mediaType));
            jsonObject.write(writer);
            writer.flush();
        } catch (JSONException e) {
            throw new WebApplicationException(e);
        }
    }
}
