package org.jvnet.hyperjaxb_annox.tests.issues.tests;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.ConsoleErrorReporter;
import com.sun.tools.xjc.ModelLoader;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.model.Model;

public class RunIssuesBuildTest
{
	@BeforeEach
	public void setUp()
	{
		System.setProperty("javax.xml.accessExternalSchema", "all");
	}

	@Test
	public void compilesSchema()
		throws Exception
	{
		new File("target/generated-sources/xjc").mkdirs();
		String schemaDirectory = new File(getClass().getResource("/schema.xsd").toURI()).getParentFile().getAbsolutePath();
		String bindingDirectory = new File(getClass().getResource("/binding.xjb").toURI()).getParentFile().getAbsolutePath();
		final String[] arguments = new String[]
		{
		 	"-debug",
		 	"-verbose",
		 	"-xmlschema", schemaDirectory, 
		 	"-b", bindingDirectory,
		 	"-d", "target/generated-sources/xjc",
		 	"-extension",
		 	"-Xannotate",
		 	"-XremoveAnnotation"
		 };
		Options options = new Options();
		options.parseArguments(arguments);
		
		// Report errors to the console.
		ConsoleErrorReporter receiver = new ConsoleErrorReporter();
		
		// XJC Root of the object model that represents the code that needs to be generated.
		Model model = ModelLoader.load(options, new JCodeModel(), receiver);
		model.generateCode(options, receiver);
		
		// Receives generated code and writes to the appropriate storage.
		CodeWriter cw = options.createCodeWriter();
		
		// XJC Generates Java source code using a JCodeModel.
		model.codeModel.build(cw);
	}
}
