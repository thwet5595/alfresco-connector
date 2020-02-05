/**
 * 
 */
package com.thwet.alfrescoconnector.common.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.thwet.alfrescoconnector.common.DocumentFolder;

/**
 * @author Thwet Thwet Mar
 *
 *         Feb 5, 2020
 */
@Component("documentRootFolder")
public class DocumentRootFolder implements DocumentFolder {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentRootFolder.class);
	private Folder folder;

	@Autowired
	Properties configs;
	@Autowired
	Session session;

	@Override
	public Folder getFolder() {
		if (this.folder != null) {
			return this.folder;
		} else {
			return this.getRootFolder();
		}
	}

	@Override
	public void setFolder(Folder folder) {
		this.folder = folder;
	}

	private Folder getRootFolder() {
		Folder documentRootFolder = null;
		try {
			Folder rootFolder = session.getRootFolder();
			documentRootFolder = (Folder) session.getObjectByPath(
					rootFolder.getPath() + "/" + configs.getProperty("alfresco.thwet.root.folder.name"));

			this.folder = documentRootFolder;

		} catch (CmisObjectNotFoundException onfe) {
			LOGGER.error("Root Folder Not exist!" + onfe.getMessage(), onfe);
		} catch (Exception e) {
			LOGGER.error("Exception getting Root Folder!" + e.getMessage(), e);
		}
		return documentRootFolder;
	}

	@Override
	public Folder createSubFolder(String folderName) {
		Folder moduleFolder = null;
		try {
			moduleFolder = (Folder) session.getObjectByPath(this.folder.getPath() + "/" + folderName);
			LOGGER.info("Sub Folder already exist!");
		} catch (CmisObjectNotFoundException onfe) {
			Map<String, Object> props = new HashMap<String, Object>();
			props.put("cmis:objectTypeId", "cmis:folder");
			props.put("cmis:name", folderName);

			moduleFolder = this.folder.createFolder(props);
			LOGGER.info("Created Sub folder: ID:[" + moduleFolder.getId() + "], Name:[" + moduleFolder.getName() + "]");
		}
		return moduleFolder;
	}
}
