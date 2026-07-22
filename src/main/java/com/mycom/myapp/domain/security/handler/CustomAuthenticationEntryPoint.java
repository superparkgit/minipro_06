package com.mycom.myapp.domain.security.handler;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 인증되지 않은 사용자가 인증이 필요한 API에 접근했을 떄 실행. HTTP 401 응답 반환.
 * 
 * Controller 실행 전 Security Filter에서 처리.
 * 
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		// data 요청에 대한 로그인이 필요한 상황을 프론트에게 전달.
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		String json = """
				{
					"status": 401,
					"error": "UNAUTHORIZED",
					"message": "로그인이 필요합니다."
				}
				""";
		response.getWriter().write(json);
		
	}

}
