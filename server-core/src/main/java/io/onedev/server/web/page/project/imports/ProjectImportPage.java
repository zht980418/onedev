package io.onedev.server.web.page.project.imports;

import java.util.concurrent.Callable;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.imports.ProjectImporter;
import io.onedev.server.imports.ProjectImporterContribution;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.component.wizard.WizardPanel;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.project.ProjectListPage;

@SuppressWarnings("serial")
public class ProjectImportPage extends LayoutPage {

	private static final String PARAM_IMPORTER = "importer";
	
	private ProjectImporter importer;
	
	public ProjectImportPage(PageParameters params) {
		super(params);
		
		String importerName = params.get(PARAM_IMPORTER).toString();
		for (ProjectImporterContribution contribution: OneDev.getExtensions(ProjectImporterContribution.class)) {
			for (ProjectImporter importer: contribution.getImporters()) {
				if (importer.getName().equals(importerName)) {
					this.importer = importer;
					break;
				}
			}
		}
		
		if (importer == null)
			throw new RuntimeException("Undefined importer: " + importerName);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WizardPanel("wizard", importer.getSteps()) {

			@Override
			protected WebMarkupContainer newEndActions(String componentId) {
				Fragment fragment = new Fragment(componentId, "endActionsFrag", ProjectImportPage.this);
				
				fragment.add(new TaskButton("import") {

					@Override
					protected void onCompleted(AjaxRequestTarget target, boolean successful) {
						super.onCompleted(target, successful);

						if (successful) {
							EntitySort sort = new EntitySort();
							sort.setField(Project.NAME_UPDATE_DATE);
							sort.setDirection(Direction.DESCENDING);
							ProjectQuery query = new ProjectQuery(null, Lists.newArrayList(sort));
							PageParameters params = ProjectListPage.paramsOf(query.toString(), 0, 0);
							throw new RestartResponseException(ProjectListPage.class, params); 
						}
					}

					@Override
					protected String runTask(TaskLogger logger) {
						return OneDev.getInstance(TransactionManager.class).call(new Callable<String>() {

							@Override
							public String call() throws Exception {
								return importer.doImport(false, logger);
							}
							
						});
					}
					
					@Override
					protected String getTitle() {
						return "Importing from " + importer.getName();
					}

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(form);
					}

				});		
				
				fragment.add(new TaskButton("dryRun") {

					@Override
					protected String runTask(TaskLogger logger) {
						return OneDev.getInstance(TransactionManager.class).call(new Callable<String>() {

							@Override
							public String call() throws Exception {
								return importer.doImport(true, logger);
							}
							
						});
					}
					
					@Override
					protected String getTitle() {
						return "Test importing from " + importer.getName();
					}

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(form);
					}

				});		
				
				return fragment;
			}
			
		});
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getUser() != null;
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Importing projects from " + importer.getName());
	}
	
	public static PageParameters paramsOf(String importer) {
		PageParameters params = new PageParameters();
		params.add(PARAM_IMPORTER, importer);
		return params;
	}
	
}
