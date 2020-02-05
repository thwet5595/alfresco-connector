/**
 * 
 */
package com.thwet.alfrescoconnector.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.thwet.alfrescoconnector.common.DocumentFolder;
import com.thwet.alfrescoconnector.service.DocumentService;

/**
 * @author Thwet Thwet Mar
 *
 *         Feb 5, 2020
 */

@Component
public class AppStartupRunner implements ApplicationRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppStartupRunner.class);

	@Autowired
	Properties configs;
	@Autowired
	Session session;
	@Autowired
	DocumentFolder rootFolder;
	@Autowired
	DocumentService docService;

	@Override
	public void run(ApplicationArguments args) throws Exception {

		LOGGER.info("Application Start Up -Alfresco Connector!");
		/* Create The Folders */
		Folder rootFolder = this.createRootFolder();
		if (rootFolder != null) {
			// set the Folder to avoid null pointer at later stage
			this.rootFolder.setFolder(rootFolder);
			createModuleFolders(rootFolder);
		}
		this.searchDocumentByContent();
	}

	private Folder createRootFolder() {
		Folder rootFolder = null;
		String rootFolderName = configs.getProperty("alfresco.thwet.root.folder.name");
		Folder alfrescoRootFolder = session.getRootFolder();

		try {
			rootFolder = (Folder) session.getObjectByPath(alfrescoRootFolder.getPath() + "/" + rootFolderName);
			LOGGER.info("Root Folder already exist!");
		} catch (CmisObjectNotFoundException onfe) {
			Map<String, Object> props = new HashMap<String, Object>();
			props.put("cmis:objectTypeId", "cmis:folder");
			props.put("cmis:name", rootFolderName);
			rootFolder = alfrescoRootFolder.createFolder(props);
			String subFolderId = rootFolder.getId();
			LOGGER.info("Created Root Folder: ID:[" + subFolderId + "], Name:[" + rootFolder.getName() + "]");
		} catch (Exception e) {
			LOGGER.error("Exception! Creating Root Folder: " + e.getMessage(), e);
		}

		return rootFolder;
	}

	private void createModuleFolders(Folder rootFolder) {
		String modulesFolders = configs.getProperty("alfresco.thwet.modules.folder.names");
		String[] modulesArr = modulesFolders.split(",");

		for (String moduleFolderName : modulesArr) {
			this.rootFolder.createSubFolder(moduleFolderName);
		}
	}

	private void searchDocumentByContent() throws Exception {
		/*
		 * List<String> list = docService.getDocumentByName("Core Java");
		 * LOGGER.info(" Inside searchDocumentByName()..."+list.size());
		 * 
		 * List<String> listContents = docService.getDocumentByContent("reims");
		 * LOGGER.info(" Inside searchDocumentByContent()..."+listContents.size(
		 * ));
		 * 
		 * List<String> documents = docService.getDocuments();
		 * LOGGER.info(" Documents size ...."+documents.size()); for(String s:
		 * documents){ CmisObject doc = session.getObject(s); Document document
		 * = (Document) doc; LOGGER.info(" ID ..."+s); for(Document d:
		 * document.getAllVersions()){
		 * LOGGER.info(" Doc Version..."+d.getVersionLabel()); } } Folder folder
		 * = docService.createFolderByFileUtils();
		 * LOGGER.info(" Folder by file utils..."+folder);
		 */

		// docService.downloadDocumentByURL("58cb1643-b9d7-4314-9160-9164f97be0d7;1.0");
	}
}