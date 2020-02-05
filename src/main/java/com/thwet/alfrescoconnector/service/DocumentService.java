/**
 * 
 */
package com.thwet.alfrescoconnector.service;

import java.io.IOException;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.springframework.web.multipart.MultipartFile;

import com.thwet.alfrescoconnector.model.DocumentRequestModel;

/**
 * @author Thwet Thwet Mar
 *
 *         Feb 5, 2020
 */
public interface DocumentService {
	public ContentStream getDocument(String documentId) throws Exception;

	public Document createDocument(MultipartFile file, DocumentRequestModel model) throws Exception;

	public ItemIterable<CmisObject> getChildFolders();

	public ItemIterable<CmisObject> getFiles(String path);

	public List<String> getDocumentByContent(String criteria);

	public List<String> getDocumentByName(String name);

	public List<String> getDocuments();

	Folder createFolderByFileUtils() throws Exception;

	public void downloadDocument() throws IOException;

	public void downloadDocumentByURL(String objectId) throws IOException;
}
