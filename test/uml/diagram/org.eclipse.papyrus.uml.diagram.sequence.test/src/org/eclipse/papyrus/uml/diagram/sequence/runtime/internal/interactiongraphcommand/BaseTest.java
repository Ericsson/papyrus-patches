package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraphcommand;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.internal.databinding.viewers.ViewerUpdater;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraph.ViewUtilities;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionGraphCommandHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionModelHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionNotationHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.WorkspaceAndPapyrusEditor;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Interaction;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;

public class BaseTest {
	protected interface TestExecution {
		public Object test(Diagram diagram, InteractionGraphCommandHelper helper) throws ExecutionException;
	}
	
	@BeforeClass
	public static void init() {
	}
	
	@After
	public void cleanUp() {
		InteractionModelHelper.clearTransactionStates();
		editor.closeDiagrams();		
	}

	public void test(TestExecution ...tests) throws ExecutionException {
		String name = getTestMethodName();
		test(name, tests);
	}		
	
	public void test(String name, TestExecution ...tests) throws ExecutionException {
		ModelSet modelSet = editor.getResourceSet();
		InteractionGraphCommandHelper helper = new InteractionGraphCommandHelper(modelSet);
		
		InteractionModelHelper.startTransaction(modelSet);				
		Diagram diagram = helper.createSequenceDiagram(name);
		editor.initDiagramAndModel((Interaction)diagram.getElement(), diagram);				
		InteractionModelHelper.endTransaction();
		
		editor.openDiagram(diagram);

		try {
			for (TestExecution t: tests) {
				t.test(diagram,helper);
			}
			editor.waitForClose(diagram);
		} catch (Throwable e) {
			editor.waitForClose(diagram);
			throw e;
		}

	}
	
	protected Rectangle[] getViewBounds(Diagram dia, Element... elements) {
		 EditPartViewer viewer = InteractionNotationHelper.getEditPartViewer(dia);
		return Arrays.asList(elements).stream().map(d->ViewUtilities.getViewForElement((View)viewer.getContents().getModel(), d)).
				map(d->ViewUtilities.getBounds(viewer, d)).toArray(d->new Rectangle[d]);
	}

	private static Class<?>[] EMPTY = new Class<?>[0];
	private String getTestMethodName() {
		
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		for (int i=0; i<stackTrace.length; i++) {
			Class<?> cls = stackTrace[0].getClass();
			try {
				Method m = cls.getMethod(stackTrace[0].getMethodName(), EMPTY);
				if (m.getAnnotationsByType(org.junit.Test.class) != null || 
					m.getAnnotationsByType(org.junit.jupiter.api.Test.class) != null) {
					return m.getName();
				}
			} catch (Exception e) {} 
		}
		
		return stackTrace[2].getMethodName();
	}
	
	@Rule
	public WorkspaceAndPapyrusEditor editor = new WorkspaceAndPapyrusEditor();

}
