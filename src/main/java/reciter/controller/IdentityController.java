/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package reciter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.service.IdentityService;
@Slf4j
@Controller
public class IdentityController {


    @Autowired
    private IdentityService identityService;
    
    @Value("${identity.dynamodb.mandatory.fields}")
    private String mandatoryFields;

    @ApiOperation(value = "Add an identity to Identity table in DynamoDb", notes = "This api creates an identity in the Identity table in dynamoDb by collecting identity data from different system of records.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Identity creation successful"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/identity/", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity addIdentity(@RequestBody Identity identity) {
        StopWatch stopWatch = new StopWatch("Add an identity to Identity table in DynamoDb");
        stopWatch.start("Add an identity to Identity table in DynamoDb");
     // Validate the mandatory fields
        try
        {
        	validateMandatoryFields(identity);
        }
        catch(IllegalArgumentException iae)
        {
        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(iae.getLocalizedMessage());
        }
        identityService.save(identity);
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Add list of identities to Identity table in DynamoDb", notes = "This api creates list of identities in the Identity table in dynamoDb by collecting identity data from different system of records.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Identity List creation successful"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/save/identities/", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public void saveIdentities(@RequestBody List<Identity> identities) {
        StopWatch stopWatch = new StopWatch("Add list of identities to Identity table in DynamoDb");
        stopWatch.start("Add list of identities to Identity table in DynamoDb");
        log.info("calling saveIdentities with number of identities=" + identities.size());
     
        for(Identity identity : identities)
        {	
        	// Validate the mandatory fields
        	validateMandatoryFields(identity);
        }	
        identityService.save(identities);
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
    }

    @ApiOperation(value = "Search the identity table for a given ID", response = ResponseEntity.class, notes = "This api searches for a given identity in identity table.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Identity found successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/find/identity/by/uid", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity findByUid(@RequestParam String uid) {
        StopWatch stopWatch = new StopWatch("Search the identity table for a given ID");
        stopWatch.start("Search the identity table for a given ID");
        log.info("calling findByUid with size of uids=" + uid);
        Identity identity = identityService.findByUid(uid);
        if(identity == null) {
            stopWatch.stop();
            log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uid provided '" + uid + "' was not found in the Identity table");
        }
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return new ResponseEntity<>(identity, HttpStatus.OK);
    }

    @ApiOperation(value = "Search the identity table for a list of ID supplied", response = Identity.class, notes = "This api searches for a list of identities in identity table.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Identity List found successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/find/identity/by/uids/", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity findByUids(@RequestParam List<String> uids) {
        StopWatch stopWatch = new StopWatch("Search the identity table for a list of ID supplied");
        stopWatch.start("Search the identity table for a list of ID supplied");
        log.info("calling findByUid with size of uids=" + uids);
        List<Identity> identities;
        try {
            identities = identityService.findByUids(uids);
        } catch (NullPointerException ne) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uids provided '" + uids + "' was not found in the Identity table");
        }
        if (identities.size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uids provided '" + uids + "' was not found in the Identity table");
        }
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return new ResponseEntity<>(identities, HttpStatus.OK);
    }
    
    @ApiOperation(value = "Get all identity from Identity table", response = Identity.class, notes = "This api scans identity table and returns all identitites.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Identities found successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/find/all/identity", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity findAll() {
        StopWatch stopWatch = new StopWatch("Identity All api performance");
        stopWatch.start("findAllIdentities");
        List<Identity> identities;
        try {
            identities = identityService.findAll();
        } catch (Exception ne) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Issue with the request" + ne);
        }
        if (identities.size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Identity table is empty");
        }
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return new ResponseEntity<>(identities, HttpStatus.OK);
    }
    
    public String[] getMandatoryFields() {
        return mandatoryFields.split(",");
    }
    
    // Validate the mandatory fields based on application.properties
    private void validateMandatoryFields(Identity identity) {
        String[] mandatoryFields = getMandatoryFields();

     // Validate each AuthorName in the alternateNames list
        if (identity.getAlternateNames() == null || identity.getAlternateNames().isEmpty()) {
            throw new IllegalArgumentException("Field 'alternateNames' is required but not provided.");
        }
        List<AuthorName> listofAuthorNames = identity.getAlternateNames();
        
        if (identity.getUid() == null || identity.getUid().isEmpty()) {
            throw new IllegalArgumentException("Field 'Uid' in Identity is required but not provided.");
        }
  
        for (AuthorName authorName : listofAuthorNames) {
            // Ensure each field is present and valid
        	
            for (String field : mandatoryFields) {
                switch (field) {
                    case "firstName":
                        if (authorName.getFirstName() == null || authorName.getFirstName().isEmpty()) {
                            throw new IllegalArgumentException("Field 'firstName' in AuthorName is required but not provided.");
                        }
                        break;

                    case "lastName":
                        if (authorName.getLastName() == null || authorName.getLastName().isEmpty()) {
                            throw new IllegalArgumentException("Field 'lastName' in AuthorName is required but not provided.");
                        }
                        break;

                    case "firstInitial":
                        if (authorName.getFirstInitial() == null || authorName.getFirstInitial().isEmpty()) {
                            throw new IllegalArgumentException("Field 'firstInitial' in AuthorName is required but not provided.");
                        }
                        break;
                }
            }
        }
    }
}
