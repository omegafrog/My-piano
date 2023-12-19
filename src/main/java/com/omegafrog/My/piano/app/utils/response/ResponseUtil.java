package com.omegafrog.My.piano.app.utils.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ResponseUtil {

    public static void writeResponse(
            JsonAPIResponse apiResponse, HttpServletResponse response, ObjectMapper objectMapper) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(apiResponse));
        writer.flush();
    }
    public static Map<String, Object> getStringObjectMap(String keyName, Object object){
        Map<String, Object> data = new HashMap<>();
        data.put(keyName, object);
        return data;
    }
}
