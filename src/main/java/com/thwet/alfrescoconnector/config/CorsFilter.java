/**
 * 
 */
package com.thwet.alfrescoconnector.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author Thwet Thwet Mar
 *
 *         Feb 5, 2020
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)

public class CorsFilter extends OncePerRequestFilter {

	@Value("${server.host}")
	private String serverHost;
	@Value("${server.port}")
	private String serverPort;

	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// response.setHeader("Access-Control-Allow-Origin", serverHost + ":" +
		// serverPort);
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Methods", "PUT, POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers",
				"application/x-www-form-urlencoded,Accept, enctype, x-requested-with, Content-Type, origin, Authorization, client-security-token, Content-Disposition");
		response.setHeader("Access-Control-Expose-Headers", "Authorization, Content-Disposition");

		if (request.getMethod().equals("OPTIONS"))
			response.setStatus(HttpServletResponse.SC_OK);
		else
			filterChain.doFilter(request, response);

	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		doFilter(request, response, filterChain);

	}
}
