/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 13/03/2004
 * Time: 23:19:19
 */
package com.atlassian.theplugin.jira.api;

import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import static com.atlassian.theplugin.commons.util.UrlUtil.encodeUrl;
import com.atlassian.theplugin.jira.model.JIRAServerCache;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import org.apache.commons.httpclient.HttpMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JIRARssClient extends AbstractHttpSession {
    private static final Logger LOGGER = Logger.getInstance(JIRARssClient.class.getName());
  private final String myServerVersion;

  public JIRARssClient(final Server server, final HttpSessionCallback callback, String serverVersion) throws RemoteApiMalformedUrlException {
		super(server, callback);
    myServerVersion = serverVersion;
  }

	@Override
	protected void adjustHttpHeader(HttpMethod method) {
	}

	@Override
	protected void preprocessResult(Document doc) throws JDOMException, RemoteApiSessionExpiredException {
	}


    public List<JIRAIssue> getIssues(List<JIRAQueryFragment> fragments,
						  String sortBy,
						  String sortOrder, int start, int max) throws JIRAException {

        StringBuilder url = getIssuesRequest();


	    List<JIRAQueryFragment> fragmentsWithoutAnys = new ArrayList<JIRAQueryFragment>();
	    for (JIRAQueryFragment jiraQueryFragment : fragments) {
		    if (jiraQueryFragment.getId() != JIRAServerCache.ANY_ID) {
			    fragmentsWithoutAnys.add(jiraQueryFragment);
		    }
	    }

        for (JIRAQueryFragment fragment : fragmentsWithoutAnys) {
            if (fragment.getQueryStringFragment() != null) {
                url.append("&").append(fragment.getQueryStringFragment());
            }
        }

		url.append("&reset=true");
		url.append("&sorter/field=").append(sortBy);
		url.append("&sorter/order=").append(sortOrder);
		url.append("&pager/start=").append(start);
		url.append("&tempMax=").append(max);
        url.append(appendAuthentication(false));

		try {
                  LOGGER.info("Requesting " + url);
            Document doc = retrieveGetResponse(url.toString());
            Element root = doc.getRootElement();
            Element channel = root.getChild("channel");
            if (channel != null) {
              List children = channel.getChildren("item");
              if (!children.isEmpty()) {
                  return makeIssues(children);
              } else {
                LOGGER.info("No <item> tags");
              }
            } else {
              LOGGER.warn("Channel not found");
            }
            return Collections.emptyList();
        } catch (IOException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (RemoteApiSessionExpiredException e) {
			throw new JIRAException(e.getMessage(), e);
		}

	}

  private StringBuilder getIssuesRequest() {
    LOGGER.info("JIRA server version: " + myServerVersion);
    if (myServerVersion == null || StringUtil.compareVersionNumbers(myServerVersion, "3.7") < 0) {
      return new StringBuilder(getBaseUrl() + "/secure/IssueNavigator.jspa?view=rss&decorator=none&");      
    }
    return new StringBuilder(getBaseUrl() + "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?");
  }

  public List<JIRAIssue> getAssignedIssues(String assignee) throws JIRAException {
        String url = getBaseUrl() + "/sr/jira.issueviews:searchrequest-xml"
                + "/temp/SearchRequest.xml?resolution=-1&assignee=" + encodeUrl(assignee)
                + "&sorter/field=updated&sorter/order=DESC&tempMax=100" + appendAuthentication(false);

        try {
            Document doc = retrieveGetResponse(url);
            Element root = doc.getRootElement();
            Element channel = root.getChild("channel");
            if (channel != null && !channel.getChildren("item").isEmpty()) {
                return makeIssues(channel.getChildren("item"));
            }
            

            return Collections.emptyList();
        } catch (IOException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (RemoteApiSessionExpiredException e) {
            throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAIssue> getSavedFilterIssues(JIRAQueryFragment fragment,
									 String sortBy,
									 String sortOrder,
									 int start, 
									 int max) throws JIRAException {

		StringBuilder url = new StringBuilder(getBaseUrl() + "/sr/jira.issueviews:searchrequest-xml/");

		if (fragment.getQueryStringFragment() != null) {
			url.append(fragment.getQueryStringFragment())
					.append("/SearchRequest-")
					.append(fragment.getQueryStringFragment())
					.append(".xml");
		}

		url.append("?sorter/field=").append(sortBy);
		url.append("&sorter/order=").append(sortOrder);
		url.append("&pager/start=").append(start);
		url.append("&tempMax=").append(max);
			
		url.append(appendAuthentication(false));

		try {
			Document doc = retrieveGetResponse(url.toString());
			Element root = doc.getRootElement();
			Element channel = root.getChild("channel");
			if (channel != null && !channel.getChildren("item").isEmpty()) {
				return makeIssues(channel.getChildren("item"));
			}
			return Collections.emptyList();
		} catch (IOException e) {
			throw new JIRAException(e.getMessage(), e);
		} catch (JDOMException e) {
			throw new JIRAException(e.getMessage(), e);
		} catch (RemoteApiSessionExpiredException e) {
			throw new JIRAException(e.getMessage(), e);
		}

	}

	public JIRAIssue getIssue(String issueKey) throws JIRAException {

		StringBuffer url = new StringBuffer(getBaseUrl() + "/si/jira.issueviews:issue-xml/");
		url.append(issueKey).append('/').append(issueKey).append(".xml");

		url.append(appendAuthentication(true));

		try {
			Document doc = retrieveGetResponse(url.toString());
			Element root = doc.getRootElement();
			Element channel = root.getChild("channel");
			if (channel != null) {
				@SuppressWarnings("unchecked")
				final List<Element> items = channel.getChildren("item");
				if (!items.isEmpty()) {
					return new JIRAIssueBean(getBaseUrl(), items.get(0), true);
				}
			}
			throw new JIRAException("Cannot parse response from JIRA: " + doc.toString());
		} catch (IOException e) {
			throw new JIRAException(e.getMessage(), e);
		} catch (JDOMException e) {
			throw new JIRAException(e.getMessage(), e);
		} catch (RemoteApiSessionExpiredException e) {
			throw new JIRAException(e.getMessage(), e);
		}
	}

	private List<JIRAIssue> makeIssues(@NotNull List<Element> issueElements) {
        List<JIRAIssue> result = new ArrayList<JIRAIssue>(issueElements.size());
		for (final Element issueElement : issueElements) {
			result.add(new JIRAIssueBean(getBaseUrl(), issueElement, false));
		}
        return result;
    }

    private String appendAuthentication(boolean firstItem) {
		final String username = getUsername();
		if (username != null) {
            return (firstItem ? "?" : "&") + "os_username=" + encodeUrl(username)
                    + "&os_password=" + encodeUrl(getPassword());
        }
        return "";
    }
}