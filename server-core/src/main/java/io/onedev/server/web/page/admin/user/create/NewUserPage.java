package io.onedev.server.web.page.admin.user.create;

import com.google.common.collect.Sets;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.children.ProjectChildrenPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.page.project.setting.general.DefaultRoleBean;
import io.onedev.server.web.page.project.setting.general.ParentBean;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.commons.loader.AppLoader;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.admin.user.UserCssResourceReference;
import io.onedev.server.web.page.admin.user.membership.UserMembershipsPage;
import io.onedev.server.web.util.editablebean.NewUserBean;

import java.util.Collection;

import static io.onedev.server.model.Project.PROP_CODE_MANAGEMENT;
import static io.onedev.server.model.Project.PROP_DESCRIPTION;
import static io.onedev.server.model.Project.PROP_ISSUE_MANAGEMENT;
import static io.onedev.server.model.Project.PROP_NAME;
import static io.onedev.server.model.Project.PROP_FORK_PERMISSION;

@SuppressWarnings("serial")
public class NewUserPage extends AdministrationPage {
	private static final String PARAM_PARENT = "parent";

	private final Long parentId;
	private NewUserBean newUserBean = new NewUserBean();
	
	private boolean continueToAdd;
	
	public NewUserPage(PageParameters params) {
		super(params);
		parentId = params.get(PARAM_PARENT).toOptionalLong();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BeanEditor editor = BeanContext.edit("editor", newUserBean);
		
//		demand 3
//		Project editProject = new Project();
//		
//		Collection<String> properties = Sets.newHashSet(PROP_NAME, PROP_DESCRIPTION,
//				PROP_CODE_MANAGEMENT, PROP_ISSUE_MANAGEMENT,PROP_FORK_PERMISSION);
//		
//		DefaultRoleBean defaultRoleBean = new DefaultRoleBean();
//		ParentBean parentBean = new ParentBean();
//		if(parentBean!=null)
//			parentBean.setParentPath(getProjectManager().load(parentId).getPath());
//		BeanEditor projectEditor = BeanContext.edit("projectEditor",editProject,properties,false);
//		BeanEditor parentEditor = BeanContext.edit("parentEditor",parentBean);
//		if(parentId!=null){
//			parentEditor.setVisible(false);
//		}
//		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				UserManager userManager = OneDev.getInstance(UserManager.class);
				EmailAddressManager emailAddressManager = OneDev.getInstance(EmailAddressManager.class);
				
				User userWithSameName = userManager.findByName(newUserBean.getName());
				if (userWithSameName != null) {
					editor.error(new Path(new PathNode.Named(User.PROP_NAME)),
							"Login name already used by another account");
				} 
				
				if (emailAddressManager.findByValue(newUserBean.getEmailAddress()) != null) {
					editor.error(new Path(new PathNode.Named(NewUserBean.PROP_EMAIL_ADDRESS)),
							"Email address already used by another user");
				} 
				if (editor.isValid()){
					User user = new User();
					user.setName(newUserBean.getName());
					user.setFullName(newUserBean.getFullName());
					user.setPassword(AppLoader.getInstance(PasswordService.class).encryptPassword(newUserBean.getPassword()));
//					demand 3 set personal project
//					try {
//						editProject.setName(user.getName()+"-personal-project");
//						String projectPath = editProject.getName();
//						if(parentBean.getParentPath()!=null)
//							projectPath = parentBean.getParentPath()+"/"+projectPath;
//						Project newProject = getProjectManager().initialize(projectPath);
//						newProject.setDescription("this is the personal project belongs to user"+user.getName());
//						newProject.setCodeManagement(true);
//						newProject.setIssueManagement(true);
//						newProject.setForkPermission(false);
//						newProject.setDefaultRole(defaultRoleBean.getRole());
//						getProjectManager().create(newProject);
//						Session.get().success("New Personal project created");
//						if(newProject.isCodeManagement())
//							setResponsePage(ProjectBlobPage.class,ProjectBlobPage.paramsOf(newProject));
//						else if(newProject.isIssueManagement())
//							setResponsePage(ProjectIssueListPage.class,ProjectIssueListPage.paramsOf(newProject));
//						else 
//							setResponsePage(ProjectChildrenPage.class,ProjectChildrenPage.paramsOf(newProject));
//					} catch (UnauthorizedException e){
//						if(parentEditor.isVisible())
//							parentEditor.error(new Path(new PathNode.Named("parentPath")),e.getMessage());
//						else 
//							throw e;
//					}
					
					
					EmailAddress emailAddress = new EmailAddress();
					emailAddress.setValue(newUserBean.getEmailAddress());
					emailAddress.setOwner(user);
					emailAddress.setVerificationCode(null);
					
					OneDev.getInstance(TransactionManager.class).run(new Runnable() {

						@Override
						public void run() {
							userManager.save(user);
							emailAddressManager.save(emailAddress);
						}
						
					});
					
					Session.get().success("New user created");
					if (continueToAdd) {
						newUserBean = new NewUserBean();
						replace(BeanContext.edit("editor", newUserBean));
					} else {
						setResponsePage(UserMembershipsPage.class, UserMembershipsPage.paramsOf(user));
					}
				}
			}
			
		};
		form.add(editor);
		form.add(new CheckBox("continueToAdd", new IModel<Boolean>() {

			@Override
			public void detach() {
			}

			@Override
			public Boolean getObject() {
				return continueToAdd;
			}

			@Override
			public void setObject(Boolean object) {
				continueToAdd = object;
			}
			
		}));
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new UserCssResourceReference()));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Create User");
	}

	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
}
