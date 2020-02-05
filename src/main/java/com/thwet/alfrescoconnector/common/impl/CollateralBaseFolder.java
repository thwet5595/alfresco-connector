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

@Component("collateralBaseFolder")
public class CollateralBaseFolder implements DocumentFolder {

	private static final Logger LOGGER = LoggerFactory.getLogger(CollateralBaseFolder.class);
	private Folder folder;

	@Autowired
	Properties configs;
	@Autowired
	Session session;
	@Autowired
	DocumentFolder documentRootFolder;

	@Override
	public Folder getFolder() {

		if (this.folder != null) {
			return this.folder;
		} else {
			return this.getCollBaseFolder();
		}
	}

	@Override
	public void setFolder(Folder folder) {
		this.folder = folder;
	}

	private Folder getCollBaseFolder() {
		Folder collBaseFolder = null;
		try {
			String rootFolderPath = documentRootFolder.getFolder().getPath();
			collBaseFolder = (Folder) session.getObjectByPath(
					rootFolderPath + "/" + configs.getProperty("alfresco.thwet.module.coll.folder.name"));

			this.folder = collBaseFolder;

		} catch (CmisObjectNotFoundException onfe) {
			LOGGER.error("Collateral Base Folder Not exist!" + onfe.getMessage(), onfe);
		} catch (Exception e) {
			LOGGER.error("Exception getting Collateral Base Folder!" + e.getMessage(), e);
		}
		return collBaseFolder;
	}

	public Folder createSubFolder(String folderName) {
		Folder collateralFolder = null;
		try {
			collateralFolder = (Folder) session.getObjectByPath(this.folder.getPath() + "/" + folderName);
			LOGGER.info("Sub Folder already exist!");
		} catch (CmisObjectNotFoundException onfe) {
			Map<String, Object> props = new HashMap<String, Object>();
			props.put("cmis:objectTypeId", "cmis:folder");
			props.put("cmis:name", folderName);

			collateralFolder = this.folder.createFolder(props);
			LOGGER.info("Created Sub folder: ID:[" + collateralFolder.getId() + "], Name:[" + collateralFolder.getName()
					+ "]");
		}
		return collateralFolder;
	}

}