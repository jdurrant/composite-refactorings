/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.steps.tests;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.eclipse.jdt.testplugin.TestOptions;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.tests.core.ProjectTestSetup;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings({ "restriction", "deprecation" })
public class StepTest {

	private IJavaProject fJProject1;

	private IPackageFragmentRoot fSourceFolder;

	@BeforeClass
	public static void setUpTests() throws Exception {
		new ProjectTestSetupUtilities().setUp();
	}

	@Before
	public void setUp() throws Exception {
		@SuppressWarnings("unchecked")
		Hashtable<String, String> options= TestOptions.getDefaultOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");

		JavaCore.setOptions(options);

		IPreferenceStore store= JavaPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.CODEGEN_ADD_COMMENTS, false);
		store.setValue(PreferenceConstants.CODEGEN_KEYWORD_THIS, false);

		StubUtility.setCodeTemplate(CodeTemplateContextType.METHODSTUB_ID, "//TODO\n${body_statement}", null);

		Preferences corePrefs= JavaPlugin.getJavaCorePluginPreferences();
		corePrefs.setValue(JavaCore.CODEASSIST_FIELD_PREFIXES, "");
		corePrefs.setValue(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, "");
		corePrefs.setValue(JavaCore.CODEASSIST_FIELD_SUFFIXES, "");
		corePrefs.setValue(JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, "");

		fJProject1= ProjectTestSetup.getProject();

		fSourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
	}

	@After
	public void tearDown() throws Exception {
		JavaProjectHelper.clear(fJProject1, ProjectTestSetup.getDefaultClasspath());
	}

	@Test
	public void testCreateNewSuperclass1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("    public void m() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("C");
		AssistContext context= StepTestUtilities.getCorrectionContext(cu, offset, 0);
		List<?> proposals= StepTestUtilities.doCollectAssists(context, false);

		StepTestUtilities.assertCorrectLabels(proposals);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C extends SuperC {\n");
		buf.append("    public void m() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class SuperC {\n");
		buf.append("}\n");
		String expected1= buf.toString();

		StepTestUtilities.assertExpectedExistInProposals(proposals, new String[] { expected1 });
	}

	@Test
	public void testMoveToImmediateSuperclass1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C extends D {\n");
		buf.append("    public void m() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class D {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("m()");
		AssistContext context= StepTestUtilities.getCorrectionContext(cu, offset, 0);
		List<?> proposals= StepTestUtilities.doCollectAssists(context, false);

		StepTestUtilities.assertCorrectLabels(proposals);
		StepTestUtilities.assertProposalExists(proposals, String.format("Move '%s' to super type '%s'", "m", "D"));
	}

	@Test
	public void testMoveTypeToNewFile1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C extends D {\n");
		buf.append("    public void m() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class D {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("class D") + "class ".length();
		AssistContext context= StepTestUtilities.getCorrectionContext(cu, offset, 0);
		List<?> proposals= StepTestUtilities.doCollectAssists(context, false);

		StepTestUtilities.assertCorrectLabels(proposals);
		StepTestUtilities.assertProposalExists(proposals, String.format("Move type '%s' to a new file", "D"));
	}

	@Test
	public void testReplaceTypeBySupertype1() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("public class C {\n");
		buf.append("    public void m(E o) {\n");
		buf.append("        if (o instanceof E) {\n");
		buf.append("            o.m();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class D {\n");
		buf.append("    public void m() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class E extends D {\n");
		buf.append("}\n");
		buf.append("\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("class E") + "class ".length();
		AssistContext context= StepTestUtilities.getCorrectionContext(cu, offset, 0);

		List<?> proposals= StepTestUtilities.doCollectAssists(context, false);
		StepTestUtilities.assertCorrectLabels(proposals);
		StepTestUtilities.assertProposalExists(proposals, String.format("Replace type '%s' by super type '%s' in variable declarations", "E", "D"));
		StepTestUtilities.assertProposalExists(proposals, String.format("Replace type '%s' by super type '%s' in instanceof expressions", "E", "D"));

	}

	@Test
	public void testReplaceTypeBySupertype2() throws Exception {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("public class C {\n");
		buf.append("    public void m(F o) {\n");
		buf.append("        if (o instanceof F) {\n");
		buf.append("            o.m();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class D {\n");
		buf.append("    public void m() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class E extends D {\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("class F extends E {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		int offset= buf.toString().indexOf("class F") + "class ".length();
		AssistContext context= StepTestUtilities.getCorrectionContext(cu, offset, 0);

		List<?> proposals= StepTestUtilities.doCollectAssists(context, false);
		StepTestUtilities.assertCorrectLabels(proposals);
		StepTestUtilities.assertProposalExists(proposals, String.format("Replace type '%s' by super type '%s' in variable declarations", "F", "E"));
		StepTestUtilities.assertProposalExists(proposals, String.format("Replace type '%s' by super type '%s' in variable declarations", "F", "D"));
		StepTestUtilities.assertProposalDoesNotExist(proposals, String.format("Replace type '%s' by super type '%s' in variable declarations", "F", "Object"));
		StepTestUtilities.assertProposalExists(proposals, String.format("Replace type '%s' by super type '%s' in instanceof expressions", "F", "E"));
		StepTestUtilities.assertProposalExists(proposals, String.format("Replace type '%s' by super type '%s' in instanceof expressions", "F", "D"));
		StepTestUtilities.assertProposalDoesNotExist(proposals, String.format("Replace type '%s' by super type '%s' in instanceof expressions", "F", "Object"));
	}

}
