/**
 * 
 */
package com.thwet.alfrescoconnector.controller;

import java.util.Properties;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.thwet.alfrescoconnector.model.DocumentRequestModel;
import com.thwet.alfrescoconnector.model.DocumentResponseModel;
import com.thwet.alfrescoconnector.service.DocumentService;

/**
 * @author Thwet Thwet Mar
 *
 *         Feb 5, 2020
 */

@RequestMapping("api")
@RestController
public class DocumentController {
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

	@Autowired
	Properties configs;
	@Autowired
	DocumentService docService;

	@RequestMapping(value = "/create/vendordocument", method = RequestMethod.POST)
	public ResponseEntity<DocumentResponseModel> create(
			@RequestParam(value = "file", required = true) MultipartFile file,
			@RequestParam(value = "document", required = true) String model) throws Exception {
		LOGGER.info("Inside create");
		DocumentResponseModel response = new DocumentResponseModel();

		try {
			Gson gson = new Gson();
			DocumentRequestModel collModel = gson.fromJson(model, DocumentRequestModel.class);
			Document document = docService.createDocument(file, collModel);

			if (document != null) {
				response.setUrl(document.getContentUrl());
				response.setId(document.getId());
				response.setName(document.getName());
				response.setSuccessMsg("Success");

				return new ResponseEntity<>(response, HttpStatus.CREATED);
			} else {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (CmisContentAlreadyExistsException e) {
			response.setErrorMsg("File Already Exist!");
			return new ResponseEntity<>(response, HttpStatus.FOUND);
		} catch (Exception e) {
			response.setErrorMsg(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(path = "/retrieve/document/{id}", consumes = MediaType.ALL_VALUE)
	@ResponseBody
	public ResponseEntity<InputStreamResource> retireveDocumentById(@PathVariable(name = "id") String documentId) {
		LOGGER.info("Inside retireveDocumentById()..." + documentId);
		ContentStream content;
		try {
			content = docService.getDocument(documentId);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(content.getMimeType()));
			headers.setContentDisposition(ContentDisposition.builder("inline").filename(content.getFileName()).build());

			return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType(content.getMimeType()))
					.body(new InputStreamResource(content.getStream()));
		} catch (Exception e) {
			LOGGER.error("Exception! Fail to retrieve document " + e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
}
