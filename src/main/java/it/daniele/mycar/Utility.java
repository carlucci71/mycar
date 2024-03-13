package it.daniele.mycar;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

@Component
public class Utility {
    @Value( "${HOST_ADDRESS:}")
    static String HOST_ADDRESS;

    public static String getHostAddress(){
        try {
            System.out.println("**************************");
            System.out.println("**************************");
            System.out.println("**************************");
            if (InetAddress.getLocalHost().isSiteLocalAddress()){
                System.out.println("SITE");
            }
            //InetAddress.getByAddress(InetAddress.getLocalHost().getAddress()));
            System.out.println("InetAddress.getLocalHost().getCanonicalHostName() = " + InetAddress.getLocalHost().getCanonicalHostName());
            System.out.println("InetAddress.getLocalHost().getHostAddress() = " + InetAddress.getLocalHost().getHostAddress());
            System.out.println("InetAddress.getLoopbackAddress().getHostName() = " + InetAddress.getLoopbackAddress().getHostName());
            System.out.println("InetAddress.getLoopbackAddress().getHostAddress() = " + InetAddress.getLoopbackAddress().getHostAddress());
            System.out.println("InetAddress.getLoopbackAddress().getCanonicalHostName() = " + InetAddress.getLoopbackAddress().getCanonicalHostName());
            System.out.println("**************************");
            System.out.println("**************************");
            System.out.println("**************************");
            if (ObjectUtils.isEmpty(HOST_ADDRESS)){
                return InetAddress.getLocalHost().getHostAddress();
            }
            else {
                return "localhost";
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
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
