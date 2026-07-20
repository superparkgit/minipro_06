package com.mycom.myapp.domain.security.handler;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * USER 토큰으로 POST 요청 -> TRAINER 권한 없음 -> AccessDeniedHandler -> HTTP 403 Forbidden
 * Controller 실행 전 Spring Security Filter에서 처리
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		// data 요청에 대한 로그인이 필요한 상황을 프론트에게 전달.
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		String json = """
				{
					"status": 403,
					"error": "FORBIDDEN",
					"message": "접근 권한이 없습니다."
				}
				""";
		response.getWriter().write(json);
	}

}
