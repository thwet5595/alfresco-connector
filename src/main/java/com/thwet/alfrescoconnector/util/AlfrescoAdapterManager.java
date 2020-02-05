/**
 * 
 */
package com.thwet.alfrescoconnector.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Thwet Thwet Mar
 *
 *         Feb 5, 2020
 */

@Configuration
public class AlfrescoAdapterManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlfrescoAdapterManager.class);
	private static Properties configs;
	private static Session session;

	static {
		loadConfigs();
		createAlfrescoSession();
		getProductInfo();
	}

	private static void loadConfigs() {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("application.properties");
			configs = new Properties();
			configs.load(is);
		} catch (Exception e) {
			LOGGER.error("Error! Loading Configs, " + e.getMessage(), e);
		}
	}

	private static void createAlfrescoSession() {
		try {
			if (session == null) {
				SessionFactory factory = SessionFactoryImpl.newInstance();

				Map<String, String> parameter = new HashMap<String, String>();
				parameter.put(SessionParameter.USER, configs.getProperty("alfresco.user.admin.name"));
				parameter.put(SessionParameter.PASSWORD, configs.getProperty("alfresco.user.admin.password"));
				parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
				parameter.put(SessionParameter.BROWSER_URL, configs.getProperty("alfresco.browser-url"));
				parameter.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());

				List<Repository> repositories = factory.getRepositories(parameter);

				session = repositories.get(0).createSession();
			}
		} catch (Exception e) {
			LOGGER.error("***********************************************************");
			LOGGER.error("Error! Creating Session: " + e.getMessage(), e);
			LOGGER.error("***********************************************************");
		}
	}

	private static void getProductInfo() {
		LOGGER.info("****************" + session.getRepositoryInfo().getProductName() + "****************");
		LOGGER.info("****************" + session.getRepositoryInfo().getProductVersion() + "****************");
		LOGGER.info("****************" + session.getRepositoryInfo().getId() + "****************");
	}

	@Bean
	public Properties configs() {
		return configs;
	}

	@Bean
	public Session session() {
		return session;
	}
}