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
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.service.IdentityService;

@Slf4j
@RestController
public class IdentityController {


    @Autowired
    private IdentityService identityService;
    
    @Value("${identity.dynamodb.mandatory.fields}")
    private String mandatoryFields;

    @Operation(summary  = "Add an identity to Identity table in DynamoDb", description  = "This api creates an identity in the Identity table in dynamoDb by collecting identity data from different system of records.")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource", in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Identity creation successful"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @PostMapping(value = "/reciter/identity/", produces = "application/json")
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

    @Operation(summary  = "Add list of identities to Identity table in DynamoDb", description  = "This api creates list of identities in the Identity table in dynamoDb by collecting identity data from different system of records.")
    @Parameters({
    	@Parameter(name = "api-key", description  = "api-key for this resource",in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Identity List creation successful"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @PutMapping(value = "/reciter/save/identities/", produces = "application/json")
    public void saveIdentities(@RequestBody List<Identity> identities) {
        StopWatch stopWatch = new StopWatch("Add list of identities to Identity table in DynamoDb");
        stopWatch.start("Add list of identities to Identity table in DynamoDb");
        log.info("calling saveIdentities with number of identities=" + identities.size());
     
        for(Identity identity : identities)
        {	
        	// Validate the mandatory fields
        	try
        	{
        		validateMandatoryFields(identity);
        	}
        	catch(IllegalArgumentException iae)
            {
            	//return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(iae.getLocalizedMessage());
            	iae.printStackTrace();
            }
        }	
        identityService.save(identities);
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
    }

    @Operation(summary  = "Search the identity table for a given ID", description = "This api searches for a given identity in identity table.")
    @Parameters({
    	@Parameter(name = "api-key", description  = "api-key for this resource", in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Identity found successfully"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/reciter/find/identity/by/uid", produces = "application/json")
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

    @Operation(summary = "Search the identity table for a list of ID supplied", description = "This api searches for a list of identities in identity table.")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource",in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Identity List found successfully"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/reciter/find/identity/by/uids/", produces = "application/json")
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
    
    @Operation(summary = "Get all identity from Identity table", description  = "This api scans identity table and returns all identitites.")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource", in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Identities found successfully"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/reciter/find/all/identity", produces = "application/json")
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
        if (identity ==null || identity.getAlternateNames() == null || identity.getAlternateNames().isEmpty()) {
            throw new IllegalArgumentException("Field 'alternateNames' is required but not provided.");
        }
        List<AuthorName> listofAuthorNames = identity.getAlternateNames();
        
        if (identity ==null || identity.getUid() == null || identity.getUid().isEmpty()) {
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
