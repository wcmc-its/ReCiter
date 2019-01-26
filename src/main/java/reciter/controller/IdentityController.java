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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reciter.model.identity.Identity;
import reciter.service.IdentityService;

import java.util.List;

@Controller
public class IdentityController {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(IdentityController.class);

    @Autowired
    private IdentityService identityService;

    @ApiOperation(value = "Add an identity to Identity table in DynamoDb", notes = "This api creates an identity in the Identity table in dynamoDb by collecting identity data from different system of records.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Identity creation successful"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/identity/", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity addIdentity(@RequestBody Identity identity) {
        identityService.save(identity);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Add list of identities to Identity table in DynamoDb", notes = "This api creates list of identities in the Identity table in dynamoDb by collecting identity data from different system of records.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Identity List creation successful"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/save/identities/", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public void saveIdentities(@RequestBody List<Identity> identities) {
        slf4jLogger.info("calling saveIdentities with number of identities=" + identities.size());
        identityService.save(identities);
    }

    @ApiOperation(value = "Search the identity table for a given ID", response = ResponseEntity.class, notes = "This api searches for a given identity in identity table.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Identity found successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/find/identity/by/uid/", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity findByUid(@RequestParam String uid) {
        slf4jLogger.info("calling findByUid with size of uids=" + uid);
        Identity identity;
        try {
            identity = identityService.findByUid(uid);
        } catch (NullPointerException ne) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uid provided '" + uid + "' was not found in the Identity table");
        }
        return new ResponseEntity<>(identity, HttpStatus.OK);
    }

    @ApiOperation(value = "Search the identity table for a list of ID supplied", response = Identity.class, notes = "This api searches for a list of identities in identity table.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Identity List found successfully"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/find/identity/by/uids/", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity findByUids(@RequestParam List<String> uids) {
        slf4jLogger.info("calling findByUid with size of uids=" + uids);
        List<Identity> identities;
        try {
            identities = identityService.findByUids(uids);
        } catch (NullPointerException ne) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uids provided '" + uids + "' was not found in the Identity table");
        }
        if (identities.size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uids provided '" + uids + "' was not found in the Identity table");
        }
        return new ResponseEntity<>(identities, HttpStatus.OK);
    }
}
