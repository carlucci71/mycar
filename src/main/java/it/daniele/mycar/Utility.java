package it.daniele.mycar;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class Utility {
    public String toJson(Object o) {
        try {
            byte[] data = getMapper().writeValueAsBytes(o);
            return new String(data, StandardCharsets.ISO_8859_1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private ObjectMapper getMapper() {
        if (mapper == null) {
            mapperGetInstance();
        }
        return mapper;
    }

    private synchronized void mapperGetInstance() {
        if (mapper == null) {
            mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
    }
    ObjectMapper mapper = null;
}
