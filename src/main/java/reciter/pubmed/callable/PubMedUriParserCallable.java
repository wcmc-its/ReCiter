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
package reciter.pubmed.callable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import reciter.model.pubmed.PubMedArticle;
import reciter.pubmed.xmlparser.PubmedEFetchHandler;

public class PubMedUriParserCallable implements Callable<List<PubMedArticle>> {

	private final PubmedEFetchHandler xmlHandler;
	private final String uri;
	
	public PubMedUriParserCallable(PubmedEFetchHandler xmlHandler, String uri) {
		this.xmlHandler = xmlHandler;
		this.uri = uri;
	}

	public List<PubMedArticle> parse(String uri) throws ParserConfigurationException, SAXException, IOException {
		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		saxParser.parse(uri, xmlHandler);
		return xmlHandler.getPubmedArticles();
	}

	public List<PubMedArticle> call() throws Exception {
		return parse(uri);
	}
}
