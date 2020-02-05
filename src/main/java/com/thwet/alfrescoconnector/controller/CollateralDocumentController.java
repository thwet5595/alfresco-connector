/**
 * 
 */
package com.thwet.alfrescoconnector.controller;

import java.util.List;
import java.util.Properties;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
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
public class CollateralDocumentController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CollateralDocumentController.class);

	@Autowired
	Properties configs;
	@Autowired
	DocumentService docService;

	// TODO: Delete this, use the below one.
	@GetMapping(path = "/document/{id}", consumes = MediaType.ALL_VALUE)
	public HttpEntity<Resource> retrieve(@PathVariable(name = "id") String documentId) {
		LOGGER.info("Inside retrieve....." + documentId);
		ContentStream content;
		try {
			content = docService.getDocument(documentId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(content.getMimeType()));
			headers.setContentDisposition(ContentDisposition.builder("inline").filename(content.getFileName()).build());
			return new HttpEntity<>(new InputStreamResource(content.getStream()), headers);
		} catch (Exception e) {
			LOGGER.error("Exception! File could not obtained" + e.getMessage(), e);
			return null;
		}
	}

	@GetMapping(path = "/download/collateral/{id}", consumes = MediaType.ALL_VALUE)
	@ResponseBody
	public ResponseEntity<InputStreamResource> downloadCollateral(@PathVariable(name = "id") String documentId) {
		LOGGER.info("Inside retrieve..." + documentId);
		ContentStream content;
		try {
			content = docService.getDocument(documentId);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(content.getMimeType()));
			headers.setContentDisposition(ContentDisposition.builder("inline").filename(content.getFileName()).build());

			return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType(content.getMimeType()))
					.body(new InputStreamResource(content.getStream()));
		} catch (Exception e) {
			LOGGER.error("Exception! File could not obtained" + e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/create/collateral", method = RequestMethod.POST)
	public ResponseEntity<DocumentResponseModel> create(
			@RequestParam(value = "file", required = true) MultipartFile file,
			@RequestParam(value = "collateral", required = true) String model) throws Exception {
		LOGGER.info("Inside create");
		DocumentResponseModel response = new DocumentResponseModel();

		try {
			Gson gson = new Gson();
			DocumentRequestModel collModel = gson.fromJson(model, DocumentRequestModel.class);
			Document document = docService.createDocument(file, collModel);

			if (document != null) {
				LOGGER.info("Document created: " + document.getId());
				response.setUrl(document.getContentUrl());
				response.setId(document.getId());
				response.setName(document.getName());
				response.setSuccessMsg("Success");
				LOGGER.info(" Document URL: " + document.getContentUrl());
				LOGGER.info(" Document ID: " + document.getId());
				LOGGER.info(" Document Name: " + document.getName());

				return new ResponseEntity<>(response, HttpStatus.CREATED);
			} else {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (CmisContentAlreadyExistsException e) {
			response.setErrorMsg(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.FOUND);
		} catch (Exception e) {
			LOGGER.error("Exception! " + e.getMessage(), e);
			response.setErrorMsg(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/find/collateral/folders", method = RequestMethod.GET)
	public ResponseEntity<String> getFolders() {
		LOGGER.info("Inside getFolders()");
		try {
			docService.getChildFolders();
			return new ResponseEntity<>(HttpStatus.FOUND);
		} catch (Exception e) {
			LOGGER.error("Exception! " + e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/find/collateral/documents/{path}", method = RequestMethod.GET)
	public ResponseEntity<String> getDocuments(@PathVariable(name = "path") String folderName) {
		LOGGER.info("Inside getDocuments()");
		try {
			docService.getFiles(folderName);
			return new ResponseEntity<>(HttpStatus.FOUND);
		} catch (Exception e) {
			LOGGER.error("Exception! " + e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value = "/search/collateral/content/", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getDocumentsByContent(
			@RequestParam(value = "search", required = true) String criteria) {
		LOGGER.info("Inside getDocumentsByContent()" + criteria);
		try {
			List<String> list = docService.getDocumentByContent(criteria);
			if (list != null && list.size() > 0)
				return new ResponseEntity<>(list, HttpStatus.FOUND);
			else
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			LOGGER.error("Exception! " + e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/search/collateral/name/", method = RequestMethod.GET)
	public ResponseEntity<List<String>> getDocumentsByName(
			@RequestParam(value = "search", required = true) String name) {
		LOGGER.info("Inside getDocumentsByName()" + name);
		try {
			List<String> list = docService.getDocumentByContent(name);
			if (list != null && list.size() > 0)
				return new ResponseEntity<>(list, HttpStatus.FOUND);
			else
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			LOGGER.error("Exception! " + e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/employees", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public String getTest() {
		// model.addAttribute("employees", getEmployeesCollection());
		return "Success";
	}
}
