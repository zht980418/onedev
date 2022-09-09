package io.onedev.server.plugin.imports.bitbucketcloud;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.model.Role;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.RoleChoice;

@Editable
public class ProjectImportOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private String publicRoleName;
	
	@Editable(order=100, name="Public Role", description="If specified, all public repositories imported from GitHub "
			+ "will use this as default role. Private repositories are not affected")
	@RoleChoice
	@Nullable
	public String getPublicRoleName() {
		return publicRoleName;
	}

	public void setPublicRoleName(String publicRoleName) {
		this.publicRoleName = publicRoleName;
	}
	
	@Nullable
	public Role getPublicRole() {
		if (publicRoleName != null)
			return OneDev.getInstance(RoleManager.class).find(publicRoleName);
		else
			return null;
	}

}
