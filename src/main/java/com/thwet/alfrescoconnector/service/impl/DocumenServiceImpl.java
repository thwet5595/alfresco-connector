/**
 * 
 */
package com.thwet.alfrescoconnector.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.util.FileUtils;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.thwet.alfrescoconnector.common.DocumentFolder;
import com.thwet.alfrescoconnector.model.DocumentRequestModel;
import com.thwet.alfrescoconnector.service.DocumentService;
import com.thwet.alfrescoconnector.util.AlfrescoUtils;

/**
 * @author Thwet Thwet Mar
 *
 *         Feb 5, 2020
 */
@Service
public class DocumenServiceImpl implements DocumentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumenServiceImpl.class);

	@Autowired
	Session session;
	@Autowired
	Properties configs;
	@Autowired
	DocumentFolder collateralBaseFolder;

	@Override
	public ContentStream getDocument(String documentId) throws Exception {
		LOGGER.info("Inside getDocument");
		CmisObject obj = session.getObject(documentId);

		if (!(obj instanceof Document)) {
			throw new IllegalArgumentException("The object Id entered is not a document");
		}
		return session.getContentStream(obj);
	}

	@Override
	public ItemIterable<CmisObject> getChildFolders() {
		ItemIterable<CmisObject> listOfObj = null;
		try {
			Folder collBaseFolder = this.collateralBaseFolder.getFolder();
			listOfObj = collBaseFolder.getChildren();
			LOGGER.info("Folders:");
			for (CmisObject obj : listOfObj) {
				LOGGER.info(obj.getName());
			}
			LOGGER.info("Folders:");
		} catch (Exception e) {
			LOGGER.error("Exception in finding folders: " + e.getMessage(), e);
		}
		return listOfObj;
	}

	@Override
	public ItemIterable<CmisObject> getFiles(String path) {
		ItemIterable<CmisObject> listOfObj = null;
		try {
			Folder collBaseFolder = this.collateralBaseFolder.getFolder();
			String fullPath = collBaseFolder.getPath() + "/" + path;

			Folder folder = AlfrescoUtils.findFolder(session, fullPath);
			listOfObj = folder.getChildren();
			LOGGER.info("Children:");
			for (CmisObject obj : listOfObj) {
				if (obj instanceof Document)
					LOGGER.info(obj.getName());
			}
			LOGGER.info("Folders:");
		} catch (Exception e) {
			LOGGER.error("Exception in finding files: " + e.getMessage(), e);
		}
		return listOfObj;
	}

	@Override
	public List<String> getDocumentByContent(String criteria) {
		Folder collBaseFolder = this.collateralBaseFolder.getFolder();
		LOGGER.info("collBaseFolder>>>>>" + collBaseFolder.getPath());
		LOGGER.info("Sub Folder >>>>"
				+ AlfrescoUtils.findFolder(session, collBaseFolder.getPath() + "/" + "100010001").getPath());
		List<String> list = AlfrescoUtils.searchDocumentByContent(session,
				AlfrescoUtils.findFolder(session, collBaseFolder.getPath() + "/" + "100010001").getId(), criteria);
		return list;
	}

	@Override
	public List<String> getDocumentByName(String name) {
		Folder collBaseFolder = this.collateralBaseFolder.getFolder();
		LOGGER.info(" CollBaseFolder in getDocumentByName ..." + collBaseFolder.getId());
		List<String> list = AlfrescoUtils.searchDocumentByName(session,
				AlfrescoUtils.findFolder(session, collBaseFolder.getPath() + "/" + "100010001").getPath(), name);
		return list;
	}

	@Override
	public Document createDocument(MultipartFile file, DocumentRequestModel model) throws Exception {

		Document document = null;
		String fileName = model.getName();

		Map<String, Object> props = new HashMap<String, Object>();
		props.put("cmis:objectTypeId", "cmis:document");
		props.put("cmis:name", fileName);

		LOGGER.info("File: " + fileName + ", " + file.getSize() + ", " + file.getContentType());

		try {

			// Get the Collateral Base Folder
			Folder collBaseFolder = this.collateralBaseFolder.getFolder();
			// Create Sub Folder with colleteralId
			Folder collSubFolder = this.collateralBaseFolder.createSubFolder(model.getFolder());
			if (collSubFolder != null) {
				ContentStream contentStream = session.getObjectFactory().createContentStream(fileName, file.getSize(),
						file.getContentType(), file.getInputStream());

				document = collSubFolder.createDocument(props, contentStream, null);
				LOGGER.info("Created new document: " + document.getId() + " , " + document.getName() + " , "
						+ document.getContentUrl());
			} else {
				LOGGER.error("Exception! Sub Folder Creation failed inside the folder " + collBaseFolder.getName());
				throw new CmisObjectNotFoundException("Sub Folder Creation Failed");
			}
		} catch (CmisContentAlreadyExistsException ccaee) {
			LOGGER.error("Exception! File already exists: " + fileName);
			throw ccaee;
		} catch (Exception e) {
			LOGGER.error("Exception! Document creation failed: " + fileName);
			throw e;
		}

		return document;
	}

	/** Create Document By FileUtils **/
	@Override
	public Folder createFolderByFileUtils() throws Exception {
		Folder alfrescoRootFolder = session.getRootFolder();
		Folder rootFolder = (Folder) session.getObjectByPath(
				alfrescoRootFolder.getPath() + "/" + configs.getProperty("alfresco.thwet.root.folder.name"));

		Folder cfolder = FileUtils.createFolder(rootFolder, "100100100", null);
		return cfolder;
	}

	@Override
	public void downloadDocument() throws IOException {
		Folder collBaseFolder = this.collateralBaseFolder.getFolder();
		// String documentID = AlfUtils.getDocumentByName(session,
		// AlfUtils.findFolder(session, collBaseFolder.getPath() + "/" +
		// "100010001").getPath(), "Export");
		List<String> documentIds = AlfrescoUtils.searchDocuments(session,
				AlfrescoUtils.findFolder(session, collBaseFolder.getPath() + "/" + "100010001").getId());
		for (String documentID : documentIds) {
			LOGGER.info("Document ID ...." + documentID);
			CmisObject doc = session.getObject(documentID);
			Document document = (Document) doc;

			LOGGER.info(" Name ..." + document.getName());
			if (doc == null) {
				return;
			}

			FileUtils.download(document, "D:\\TTM_Projects\\Download\\" + document.getName());

			/*
			 * FileOutputStream out = new
			 * FileOutputStream("D:\\TTM_Projects\\Download\\"+document.getName(
			 * ));
			 * 
			 * ContentStream stream = null; try { stream = ((Document)
			 * doc).getContentStream(); if (stream != null) {
			 * IOUtils.copy(stream.getStream(), out, 64 * 1024); } } finally {
			 * IOUtils.closeQuietly(out); IOUtils.closeQuietly(stream); }
			 */
		}
	}

	@Override
	public void downloadDocumentByURL(String documentID) throws IOException {
		Folder collBaseFolder = this.collateralBaseFolder.getFolder();
		// String documentID = AlfUtils.getDocumentByName(session,
		// AlfUtils.findFolder(session, collBaseFolder.getPath() + "/" +
		// "100010001").getPath(), "Export");
		// List<String> documentIds = AlfUtils.searchDocuments(session,
		// AlfUtils.findFolder(session, collBaseFolder.getPath() + "/" +
		// "100010001").getId());
		LOGGER.info("Document ID ...." + documentID);
		CmisObject doc = session.getObject(documentID);
		Document document = (Document) doc;

		LOGGER.info(" Name ..." + document.getName());
		if (doc == null) {
			return;
		}

		FileUtils.download(document, "D:\\THWET\\Download\\" + document.getName());
		LOGGER.info(" Download Completed...");

		/*
		 * FileOutputStream out = new
		 * FileOutputStream("D:\\TTM_Projects\\Download\\"+document.getName());
		 * 
		 * ContentStream stream = null; try { stream = ((Document)
		 * doc).getContentStream(); if (stream != null) {
		 * IOUtils.copy(stream.getStream(), out, 64 * 1024); } } finally {
		 * IOUtils.closeQuietly(out); IOUtils.closeQuietly(stream); }
		 */

	}

	@Override
	public List<String> getDocuments() {
		Folder collBaseFolder = this.collateralBaseFolder.getFolder();
		List<String> list = AlfrescoUtils.searchDocuments(session,
				AlfrescoUtils.findFolder(session, collBaseFolder.getPath() + "/" + "100010001").getId());
		return list;
	}
}
