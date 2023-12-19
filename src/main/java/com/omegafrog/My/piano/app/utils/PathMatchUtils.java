package com.omegafrog.My.piano.app.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.AntPathMatcher;

public class PathMatchUtils {

    private static final int HTTP_METHOD=0;
    private static final int PATH_PATTERN=1;
    private static String[][] jwtFilterBlackListPaths = {{"POST","/**/login"}, {"POST","/**/register"},
            {"GET","/community/{id:\\d+}"}};

    private static final AntPathMatcher matcher = new AntPathMatcher();

    public static boolean isMatched(HttpServletRequest request){
        for(String[] path : jwtFilterBlackListPaths){
            if(matcher.match(path[PATH_PATTERN], request.getRequestURI())
                    &&path[HTTP_METHOD].equals(request.getMethod())){
                return true;
            }
        }
        return false;
    }
}
