package org.jvnet.hyperjaxb_annox.tests.annotate;

import java.util.ArrayList;
import java.util.List;

import org.jvnet.higherjaxb.mojo.AbstractHigherjaxbParmMojo;
import org.jvnet.higherjaxb.mojo.test.RunHigherjaxbMojo;

public class RunAnnotatePlugin extends RunHigherjaxbMojo {
	
	@Override
	protected void configureMojo(AbstractHigherjaxbParmMojo<?> mojo) {
		super.configureMojo(mojo);
		mojo.setExtension(true);
		mojo.setForceRegenerate(true);
	}

	@Override
	public List<String> getArgs() {
		final List<String> args = new ArrayList<String>(super.getArgs());
		args.add("-Xannotate");
		return args;
	}

}
