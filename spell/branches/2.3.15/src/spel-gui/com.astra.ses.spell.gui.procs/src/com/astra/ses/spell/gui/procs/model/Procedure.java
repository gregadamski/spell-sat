////////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.procs.model
// 
// FILE      : Procedure.java
//
// DATE      : 2010-07-30
//
// Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
//
// By using this software in any way, you are agreeing to be bound by
// the terms of this license.
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// NO WARRANTY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, THE PROGRAM IS PROVIDED
// ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER
// EXPRESS OR IMPLIED INCLUDING, WITHOUT LIMITATION, ANY WARRANTIES OR
// CONDITIONS OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY OR FITNESS FOR A
// PARTICULAR PURPOSE. Each Recipient is solely responsible for determining
// the appropriateness of using and distributing the Program and assumes all
// risks associated with its exercise of rights under this Agreement ,
// including but not limited to the risks and costs of program errors,
// compliance with applicable laws, damage to or loss of data, programs or
// equipment, and unavailability or interruption of operations.
//
// DISCLAIMER OF LIABILITY
// EXCEPT AS EXPRESSLY SET FORTH IN THIS AGREEMENT, NEITHER RECIPIENT NOR ANY
// CONTRIBUTORS SHALL HAVE ANY LIABILITY FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING WITHOUT LIMITATION
// LOST PROFITS), HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THE PROGRAM OR THE
// EXERCISE OF ANY RIGHTS GRANTED HEREUNDER, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGES.
//
// Contributors:
//    SES ENGINEERING - initial API and implementation and/or initial documentation
//
// PROJECT   : SPELL
//
// SUBPROJECT: SPELL GUI Client
//
////////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.procs.model;

import java.util.Map;

import com.astra.ses.spell.gui.core.extensionpoints.IProcedureRuntimeExtension;
import com.astra.ses.spell.gui.core.interfaces.IExecutorInfo;
import com.astra.ses.spell.gui.core.model.server.ExecutorConfig;
import com.astra.ses.spell.gui.core.model.server.ExecutorInfo;
import com.astra.ses.spell.gui.core.model.types.ClientMode;
import com.astra.ses.spell.gui.core.model.types.ProcProperties;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionInformation;
import com.astra.ses.spell.gui.procs.interfaces.model.IExecutionStatusManager;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedure;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureController;
import com.astra.ses.spell.gui.procs.interfaces.model.ISourceCodeProvider;
import com.astra.ses.spell.gui.procs.interfaces.model.priv.IExecutionInformationHandler;

/*******************************************************************************
 * 
 * Procedure holds the static and interactive procedure information: - Holds the
 * source code blocks to execute - Holds the runtime information that can be
 * handled by the user * Breakpoints, run into mode, step by step execution
 * 
 ******************************************************************************/
public class Procedure implements IProcedure
{
	// =========================================================================
	// STATIC DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	// PROTECTED ---------------------------------------------------------------
	// PUBLIC ------------------------------------------------------------------
	// =========================================================================
	// INSTANCE DATA MEMBERS
	// =========================================================================

	// PRIVATE -----------------------------------------------------------------
	/** Holds the procedure identifier */
	private String m_instanceId;
	/** Procedure properties */
	private Map<ProcProperties, String> m_properties;
	/** Execution information */
	private IExecutionInformationHandler m_executionInformation;
	/** Execution controller */
	private IProcedureController m_procedureController;
	/** Procedure runtime manager */
	private IExecutionStatusManager m_executionStatusManager;
	/** Source provider */
	private ISourceCodeProvider m_sourceCodeProvider;
	/** Holds the runtime processor */
	private IProcedureRuntimeExtension m_runtimeProcessor;

	/***************************************************************************
	 * Constructor
	 **************************************************************************/
	public Procedure(String instanceId, Map<ProcProperties, String> properties, ClientMode mode)
	{
		m_instanceId = instanceId;
		m_properties = properties;
		m_sourceCodeProvider = new SourceCodeProvider();
		m_procedureController = new ProcedureController(this);
		m_executionInformation = new ExecutionInformationHandler(mode,this);
		m_executionStatusManager = new ExecutionStatusManager(instanceId,this);
		m_runtimeProcessor = new ProcedureRuntimeProcessor(this);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IProcedureController getController()
	{
		return m_procedureController;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IExecutionStatusManager getExecutionManager()
	{
		return m_executionStatusManager;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IProcedureRuntimeExtension getRuntimeProcessor()
	{
		return m_runtimeProcessor;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getProcId()
	{
		return m_instanceId;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getProcName()
	{
		return this.getProperty(ProcProperties.PROC_NAME);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getParent()
	{
		return getRuntimeInformation().getParent();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public String getProperty(ProcProperties property)
	{
		return m_properties.get(property);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public IExecutionInformation getRuntimeInformation()
	{
		return m_executionInformation;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void setReplayMode(boolean doingReplay)
	{
		m_executionStatusManager.setReplay(doingReplay);
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public boolean isInReplayMode()
	{
		return m_executionStatusManager.isInReplay();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void onClose()
	{
		m_executionStatusManager.dispose();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter)
	{
		Object result = null;
		if (adapter.equals(ExecutorInfo.class))
		{
			IExecutorInfo info = new ExecutorInfo(m_instanceId);
			// TODO info.setParent(getParent().getProcId());
			getRuntimeInformation().visit(info);
			result = info;
		}
		else if (adapter.equals(ExecutorConfig.class))
		{
			ExecutorConfig cfg = new ExecutorConfig(getProcId());
			getRuntimeInformation().visit(cfg);
			result = cfg;
		}
		return result;
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
	public void reset()
	{
		m_executionStatusManager.reset();
		m_sourceCodeProvider.reset();
		m_executionInformation.reset();
	}

	/***************************************************************************
	 * 
	 **************************************************************************/
	@Override
    public ISourceCodeProvider getSourceCodeProvider()
    {
	    return m_sourceCodeProvider;
    }
}
