package io.onedev.server.plugin.imports.gitlab;

import static io.onedev.server.plugin.imports.gitlab.ImportUtils.NAME;
import static io.onedev.server.plugin.imports.gitlab.ImportUtils.importIssues;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.imports.IssueImporter;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.web.util.WicketUtils;

public class GitLabIssueImporter extends IssueImporter<ImportServer, IssueImportSource, IssueImportOption> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public String doImport(ImportServer where, IssueImportSource what, IssueImportOption how, Project project,
			boolean retainIssueNumbers, boolean dryRun, TaskLogger logger) {
		logger.log("Importing issues from project " + what.getProject() + "...");
		Map<String, Optional<User>> users = new HashMap<>();
		return importIssues(where, what.getProject(), project, retainIssueNumbers, how, users, dryRun, logger)
				.toHtml("Issues imported successfully");
	}

	@Override
	public IssueImportSource getWhat(ImportServer where, TaskLogger logger) {
		WicketUtils.getPage().setMetaData(ImportServer.META_DATA_KEY, where);
		return new IssueImportSource();
	}

	@Override
	public IssueImportOption getHow(ImportServer where, IssueImportSource what, TaskLogger logger) {
		return ImportUtils.buildImportOption(where, Lists.newArrayList(what.getProject()), logger);
	}
	
}