package org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.interactiongraphcommand;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionGraphCommandHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.InteractionModelHelper;
import org.eclipse.papyrus.uml.diagram.sequence.runtime.internal.test.utils.WorkspaceAndPapyrusEditor;
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
		ModelSet modelSet = editor.getResourceSet();
		InteractionGraphCommandHelper helper = new InteractionGraphCommandHelper(modelSet);
		
		InteractionModelHelper.startTransaction(modelSet);		
		Diagram diagram = helper.createSequenceDiagram("testSelfMessageBendpoint1");
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
	
	@Rule
	public WorkspaceAndPapyrusEditor editor = new WorkspaceAndPapyrusEditor();

}
