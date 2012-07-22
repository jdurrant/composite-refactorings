/**
 * Copyright (c) 2008-2012 University of Illinois at Urbana-Champaign.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package edu.illinois.compositerefactorings.refactorings.usesupertypeinvariabledeclarations;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringContribution;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jdt.internal.corext.refactoring.JavaRefactoringArguments;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Refactoring contribution for the use super type refactoring in variable declarations and casts.
 * 
 */
@SuppressWarnings("restriction")
public final class UseSuperTypeInVariableDeclarationsRefactoringContribution extends JavaRefactoringContribution {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Refactoring createRefactoring(JavaRefactoringDescriptor descriptor, RefactoringStatus status) throws CoreException {
		@SuppressWarnings("unchecked")
		JavaRefactoringArguments arguments= new JavaRefactoringArguments(descriptor.getProject(), retrieveArgumentMap(descriptor));
		return new UseSuperTypeInVariableDeclarationsRefactoring(arguments, status);
	}

	@Override
	public RefactoringDescriptor createDescriptor() {
		return new UseSuperTypeInVariableDeclarationsDescriptor();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map arguments, int flags) {
		return new UseSuperTypeInVariableDeclarationsDescriptor(project, description, comment, arguments, flags);
	}
}
