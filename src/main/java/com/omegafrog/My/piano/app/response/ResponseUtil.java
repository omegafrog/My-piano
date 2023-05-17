package com.omegafrog.My.piano.app.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
public class ResponseUtil {

    public static void writeResponse(
            JsonAPIResponse apiResponse, HttpServletResponse response, ObjectMapper objectMapper) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(apiResponse));
        writer.flush();
    }
}
