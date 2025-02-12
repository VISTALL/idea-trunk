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

package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.util.Collection;

public final class Constants {

	// TODO - very uncool to hardcode the particular action ID.
	// What if Anton's people decide to change the ID of this action?
	public enum JiraActionId {
		START_PROGRESS(4),
		STOP_PROGRESS(301);

		JiraActionId(long id) {
			this.id = id;
		}

		public long getId() {
			return id;
		}

		private long id;
	}

	public static final String FILE_TREE = "theplugin.tree";
	public static final String CRUCIBLE_COMMENT_TREE = "theplugin.crucible.comment.tree";
	public static final String BUILD_CHANGES_WINDOW = "theplugin.build_changes_window";
	public static final String CRUCIBLE_BOTTOM_WINDOW = "theplugin.crucible_bottom_window";
	public static final String REVIEW_TOOL_WINDOW = "theplugin.crucible.bottom.crucible_tool_window";
	public static final String CRUCIBLE_FILE_NODE = "theplugin.crucible_file_node";
	public static final Icon CRUCIBLE_ICON = IconLoader.getIcon("/icons/crucible-16.png");
	public static final DataKey<Boolean> REVIEW_TOOL_WINDOW_KEY = DataKey.create(REVIEW_TOOL_WINDOW);

	public static final String SERVER_CONFIG_PANEL = "theplugin.server_config_panel";
	public static final String SERVER = "theplugin.server";
	public static final String SERVER_TYPE = "theplugin.servertype";
	public static final String SERVERS = "theplugin.servers";

	public static final DataKey<Collection<ServerCfg>> SERVERS_KEY = DataKey.create(SERVERS);
	public static final DataKey<ServerCfg> SERVER_KEY = DataKey.create(SERVER);

	public static final DataKey<BambooBuild> BAMBOO_BUILD_KEY = DataKey.create("atlassian.connector.bamoo.build");

	public static final String ISSUE = "com.atlassian.connector.issue";
	public static final DataKey<JIRAIssue> ISSUE_KEY = DataKey.create(ISSUE);
	public static final String REVIEW = "com.atlassian.connector.review";
	public static final DataKey<ReviewAdapter> REVIEW_KEY = DataKey.create(REVIEW);
	public static final String REVIEW_WINDOW_ENABLED = "com.atlassian.connector.review_window_enabled";
	public static final DataKey<Boolean> REVIEW_WINDOW_ENABLED_KEY = DataKey.create(REVIEW_WINDOW_ENABLED);

	private Constants() {
	}

	public static final int DIALOG_MARGIN = 12;

	public static final int BG_COLOR_R = 238;
	public static final int BG_COLOR_G = 238;
	public static final int BG_COLOR_B = 238;

	public static final String HELP_URL_BASE = "theplugin.help.url.prefix";
	public static final String HELP_CONFIG_PANEL = "theplugin.config";
	public static final String HELP_BAMBOO = "theplugin.bamboo";

	public static final String HELP_CRUCIBLE = "theplugin.crucible";
	public static final String HELP_JIRA = "theplugin.jira";
	public static final String HELP_JIRA_WORKLOG = "theplugin.jira.worklog";
	public static final String HELP_TEST_CONNECTION = "theplugin.testconnection";
	public static final Icon HELP_ICON = IconLoader.getIcon("/actions/help.png");
	public static final Icon JIRA_ISSUE_TAB_ICON = IconLoader.getIcon("/icons/tab_jira-white.png");
	public static final Icon JIRA_ISSUE_PANEL_ICON = IconLoader.getIcon("/icons/tab_jira.png");
	public static final Icon BAMBOO_BUILD_TAB_ICON = IconLoader.getIcon("/icons/tab_bamboo-white.png");
	public static final Icon BAMBOO_BUILD_PANEL_ICON = IconLoader.getIcon("/icons/tab_bamboo.png");
	public static final Icon BAMBOO_TRACE_ICON = IconLoader.getIcon("/icons/bamboo-traces.png");
	public static final Icon BAMBOO_COMMITS_ICON = IconLoader.getIcon("/icons/bamboo-commits.png");
	public static final Icon CRUCIBLE_REVIEW_TAB_ICON = IconLoader.getIcon("/icons/tab_crucible-white.png");
	public static final Icon CRUCIBLE_REVIEW_PANEL_ICON = IconLoader.getIcon("/icons/tab_crucible.png");
}
