package org.jvnet.hyperjaxb_annox.tests.issues.tests;

import java.util.ArrayList;

import org.apache.maven.model.Dependency;
import org.junit.jupiter.api.Test;
import org.jvnet.higherjaxb.mojo.HigherjaxbMojo;
import org.jvnet.higherjaxb.mojo.testing.AbstractMojoTest;
import org.jvnet.higherjaxb.mojo.testing.SLF4JLogger;

public class RunIssuesPluginTest extends AbstractMojoTest
{
	@Test
	public void testExecute() throws Exception
	{
		//
		// Dependencies
		//

		final Dependency basicjaxb = new Dependency();
		basicjaxb.setGroupId("org.patrodyne.jvnet");
		basicjaxb.setArtifactId("hisrc-basicjaxb-plugins");
		basicjaxb.setVersion(DEFAULT_BASICJAXB_VERSION);
		
		//
		// MOJO Execution
		//

		HigherjaxbMojo mojo = new HigherjaxbMojo();
		mojo.setLog(new SLF4JLogger(getLogger()));

		mojo.getRemoteRepos().add(REMOTE_REPOSITORY);
		mojo.setRepoSession(REPOSITORY_SYSTEM_SESSION);
		mojo.setRepoSystem(repositorySystem);
		
		mojo.setProject(createMavenProject());
		mojo.setSchemaDirectory(fullpath("src/main/resources"));
		mojo.setGenerateDirectory(fullpath("target/generated-sources/xjc")); 
		mojo.setVerbose(true);
		mojo.setDebug(true);
		mojo.setWriteCode(true);
		mojo.setForceRegenerate(true);
		mojo.setNoFileHeader(true);
		mojo.setExtension(true);
		
		mojo.setArgs(new ArrayList<>());
		mojo.getArgs().add("-Xcustomizations");
		mojo.getArgs().add("-Xcustomizations-directory=src/main/resources");
		mojo.getArgs().add("-Xcustomizations-verbose=true");
		mojo.getArgs().add("-Xcustomizations-debug=true");
		mojo.getArgs().add("-Xannotate");
		mojo.getArgs().add("-XremoveAnnotation");
		
		mojo.setPlugins(new Dependency[] { basicjaxb });
		
		mojo.execute();
	}
}
