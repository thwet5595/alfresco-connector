/**
 * 
 */
package com.thwet.alfrescoconnector.util;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.ExtensionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thwet Thwet Mar
 *
 *         Feb 5, 2020
 */
public class AlfrescoUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(AlfrescoUtils.class);

	public static Folder findFolder(@NotNull Session session, @NotNull String path) {
		CmisObject obj = session.getObjectByPath(path);
		if (obj instanceof Folder) {
			return (Folder) obj;
		} else {
			return null;
		}
	}

	public static boolean doesFolderExist(@NotNull Session session, @NotNull String folderName) {
		String queryString = "select cmis:objectId from cmis:folder where cmis:name = '" + folderName + "'";
		ItemIterable<QueryResult> results = session.query(queryString, false);
		if (results.getTotalNumItems() > 0) {
			return true;
		} else {
			return false;
		}
	}

	public static List<String> searchDocumentByContent(@NotNull Session session, @NotNull String folderId,
			@NotNull String criteria) {
		String queryString = "select * from cmis:document where contains('" + criteria + "') and in_folder('" + folderId
				+ "')";
		// String queryString = "select * from cmis:document where contains('"+
		// criteria +"')";
		LOGGER.info(" Query String in searchContent.." + queryString);
		return getQueryResults(session, queryString);
	}

	public static List<String> searchDocumentByName(@NotNull Session session, @NotNull String folderId,
			@NotNull String name)

	{
		String queryString = "select * from cmis:document where cmis:name LIKE '%" + name + "%'";

		LOGGER.info(" Query Search ...." + queryString);
		return getQueryResults(session, queryString);
	}

	public static List<String> searchDocuments(@NotNull Session session, @NotNull String folderId) {
		String queryString = "select * from cmis:document where in_folder('" + folderId + "')";

		LOGGER.info(" Query Search in searchDocuments()...." + queryString);
		return getQueryResultsList(session, queryString);
	}

	public static String getDocumentByName(@NotNull Session session, @NotNull String folderId, @NotNull String name)

	{
		// String queryString = "select * from cmis:document where cmis:name =
		// '" + name + "' and in_folder('" + folderId + "')";
		String queryString = "select * from cmis:document where cmis:name LIKE '%" + name + "%'";

		LOGGER.info(" Query Search in getDocumentByName ...." + queryString);
		return getQueryResultsDownload(session, queryString);
	}

	public static List<String> getQueryResults(@NotNull Session session, @NotNull String queryString) {

		List<String> objList = new ArrayList<String>();

		// execute query
		ItemIterable<QueryResult> results = session.query(queryString, false).getPage(5);
		for (QueryResult qResult : results) {
			String objectId = "";
			PropertyData<?> propData = qResult.getPropertyById("cmis:objectId"); // Atom
																					// Pub
																					// binding
			if (propData != null) {
				objectId = (String) propData.getFirstValue();
			} else {
				objectId = qResult.getPropertyValueByQueryName("d.cmis:objectId"); // Web
																					// Services
																					// binding
			}
			CmisObject obj = session.getObject(session.createObjectId(objectId));
			if (obj instanceof Document)
				objList.add(obj.getName());

		}
		return objList;
	}

	public static String getQueryResultsDownload(@NotNull Session session, @NotNull String queryString) {

		List<String> objList = new ArrayList<String>();
		CmisObject obj = null;
		// execute query
		ItemIterable<QueryResult> results = session.query(queryString, false).getPage(5);
		for (QueryResult qResult : results) {
			String objectId = "";
			PropertyData<?> propData = qResult.getPropertyById("cmis:objectId"); // Atom
																					// Pub
																					// binding
			if (propData != null) {
				objectId = (String) propData.getFirstValue();
				LOGGER.info("objectId in IF ...." + objectId);
			} else {
				objectId = qResult.getPropertyValueByQueryName("d.cmis:objectId"); // Web
																					// Services
																					// binding
				LOGGER.info("objectId in Else ...." + objectId);
			}
			obj = session.getObject(session.createObjectId(objectId));
			List<CmisExtensionElement> extensions = obj.getExtensions(ExtensionLevel.PROPERTIES);

			if (extensions == null) {
				return null;
			}

			for (CmisExtensionElement ext : extensions) {
				for (CmisExtensionElement child : ext.getChildren()) {
					LOGGER.info("Extension...." + child.getName() + ": " + child.getValue());
				}
			}
			LOGGER.info(" document name ...." + obj.getName());
			if (obj instanceof Document)
				objList.add(obj.getName());

		}
		return obj.getId();
	}

	public static List<String> getQueryResultsList(@NotNull Session session, @NotNull String queryString) {

		List<String> objList = new ArrayList<String>();
		CmisObject obj = null;
		// execute query
		ItemIterable<QueryResult> results = session.query(queryString, false).getPage(5);
		for (QueryResult qResult : results) {
			String objectId = "";
			PropertyData<?> propData = qResult.getPropertyById("cmis:objectId"); // Atom
																					// Pub
																					// binding
			if (propData != null) {
				objectId = (String) propData.getFirstValue();
				LOGGER.info("objectId in IF ...." + objectId);
			} else {
				objectId = qResult.getPropertyValueByQueryName("d.cmis:objectId"); // Web
																					// Services
																					// binding
				LOGGER.info("objectId in Else ...." + objectId);
			}
			obj = session.getObject(session.createObjectId(objectId));
			List<CmisExtensionElement> extensions = obj.getExtensions(ExtensionLevel.PROPERTIES);

			if (extensions == null) {
				return null;
			}

			for (CmisExtensionElement ext : extensions) {
				for (CmisExtensionElement child : ext.getChildren()) {
					LOGGER.info("Extension...." + child.getName() + ": " + child.getValue());
				}
			}

			LOGGER.info(" document name ...." + obj.getName());
			/*
			 * if (obj instanceof Document) objList.add(obj.getName());
			 */
			objList.add(objectId);

		}
		return objList;
	}
}
