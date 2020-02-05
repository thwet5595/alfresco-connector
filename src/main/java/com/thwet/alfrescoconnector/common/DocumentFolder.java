/**
 * 
 */
package com.thwet.alfrescoconnector.common;

import org.apache.chemistry.opencmis.client.api.Folder;

/**
 * @author Thwet Thwet Mar
 *
 *         Feb 5, 2020
 */
public interface DocumentFolder {
	public Folder getFolder();

	public void setFolder(Folder folder);

	public Folder createSubFolder(String folderName);
}
