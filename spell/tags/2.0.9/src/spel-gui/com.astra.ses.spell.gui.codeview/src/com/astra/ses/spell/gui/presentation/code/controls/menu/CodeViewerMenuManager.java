///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.presentation.code.controls.menu
// 
// FILE      : CodeViewerMenuManager.java
//
// DATE      : 2010-08-26
//
// Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.presentation.code.controls.menu;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableItem;

import com.astra.ses.spell.gui.core.model.types.BreakpointType;
import com.astra.ses.spell.gui.model.commands.ClearBreakpoints;
import com.astra.ses.spell.gui.model.commands.CmdRun;
import com.astra.ses.spell.gui.model.commands.CmdGotoLine;
import com.astra.ses.spell.gui.model.commands.SetBreakpoint;
import com.astra.ses.spell.gui.model.commands.helpers.CommandHelper;
import com.astra.ses.spell.gui.presentation.code.controls.CodeViewer;
import com.astra.ses.spell.gui.presentation.code.dialogs.SearchDialog;
import com.astra.ses.spell.gui.procs.interfaces.exceptions.UninitProcedureException;
import com.astra.ses.spell.gui.procs.interfaces.model.IProcedureDataProvider;

/*******************************************************************************
 * 
 * {@link CodeViewerMenuManager} manages the CodeViewer's popup menu
 * 
 ******************************************************************************/
public class CodeViewerMenuManager
{

	/** Popup's parent widget */
	private CodeViewer	           m_viewer;
	/** Procedure data provider */
	private IProcedureDataProvider	m_data;
	/** Menu */
	private Menu	               m_menu;
	/** Procedure id */
	private String	               m_procedureId;

	/***************************************************************************
	 * Constructor
	 * 
	 * @param parent
	 **************************************************************************/
	public CodeViewerMenuManager(CodeViewer viewer, String procedureId,
	        IProcedureDataProvider procData)
	{
		m_viewer = viewer;
		m_data = procData;
		m_procedureId = procedureId;
		m_menu = new Menu(viewer.getTable());

		m_menu.addMenuListener(new MenuListener()
		{

			@Override
			public void menuShown(MenuEvent e)
			{
				fillMenu();
			}

			@Override
			public void menuHidden(MenuEvent e)
			{
			}
		});

		m_viewer.getTable().setMenu(m_menu);
	}

	/**************************************************************************
	 * Fill the menu with the appropiate actions
	 *************************************************************************/
	private void fillMenu()
	{
		/*
		 * Remove current items from the menu
		 */
		for (MenuItem item : m_menu.getItems())
		{
			item.dispose();
		}

		int tableSelectedItems = m_viewer.getTable().getSelectionCount();

		if (tableSelectedItems == 1)
		{
			BreakpointType type = BreakpointType.UNKNOWN;
			TableItem[] selection = m_viewer.getTable().getSelection();
			final int lineNo = m_viewer.getTable().indexOf(selection[0]) + 1;

			/*
			 * Go to line
			 */
			MenuItem gotoLine = new MenuItem(m_menu, SWT.PUSH);
			gotoLine.setText("Go to this line");
			gotoLine.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					HashMap<String, String> args = new HashMap<String, String>();
					args.put(CmdGotoLine.ARG_PROCID, m_procedureId);
					args.put(CmdGotoLine.ARG_LINENO, String.valueOf(lineNo));
					CommandHelper.execute(CmdGotoLine.ID, args);
				}
			});

			/*
			 * Run to this line
			 */
			MenuItem runToLine = new MenuItem(m_menu, SWT.PUSH);
			runToLine.setText("Run until this line");
			runToLine.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					BreakpointType type = BreakpointType.TEMPORARY;

					HashMap<String, String> args = new HashMap<String, String>();
					args.put(SetBreakpoint.ARG_PROCID, m_procedureId);
					args.put(SetBreakpoint.ARG_LINENO, String.valueOf(lineNo));
					args.put(SetBreakpoint.ARG_TYPE, type.toString());
					CommandHelper.execute(SetBreakpoint.ID, args);

					HashMap<String, String> runArgs = new HashMap<String, String>();
					runArgs.put(CmdRun.ARG_PROCID, m_procedureId);
					CommandHelper.execute(CmdRun.ID, runArgs);
				}
			});

			/*
			 * Separator
			 */
			new MenuItem(m_menu, SWT.SEPARATOR);

			/*
			 * Get the line's breakpoint type
			 */
			try
			{
				type = m_data.getBreakpoint(lineNo);
			}
			catch (UninitProcedureException e1)
			{
			}

			if (type.equals(BreakpointType.PERMANENT))
			{
				/*
				 * Remove permanent breakpoint
				 */
				MenuItem removeBreakpoint = new MenuItem(m_menu, SWT.PUSH);
				removeBreakpoint.setText("Remove breakpoint");
				removeBreakpoint.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						TableItem[] selection = m_viewer.getTable()
						        .getSelection();
						int lineNo = m_viewer.getTable().indexOf(selection[0]) + 1;
						BreakpointType type = BreakpointType.UNKNOWN;

						HashMap<String, String> args = new HashMap<String, String>();
						args.put(SetBreakpoint.ARG_PROCID, m_procedureId);
						args.put(SetBreakpoint.ARG_LINENO,
						        String.valueOf(lineNo));
						args.put(SetBreakpoint.ARG_TYPE, type.toString());
						CommandHelper.execute(SetBreakpoint.ID, args);
					}
				});
			}
			else
			{
				/*
				 * Add permanent breakpoint
				 */
				MenuItem addBreakpoint = new MenuItem(m_menu, SWT.PUSH);
				addBreakpoint.setText("Add breakpoint at this line");
				addBreakpoint.addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(SelectionEvent e)
					{
						TableItem[] selection = m_viewer.getTable()
						        .getSelection();
						int lineNo = m_viewer.getTable().indexOf(selection[0]) + 1;
						BreakpointType type = BreakpointType.PERMANENT;

						HashMap<String, String> args = new HashMap<String, String>();
						args.put(SetBreakpoint.ARG_PROCID, m_procedureId);
						args.put(SetBreakpoint.ARG_LINENO,
						        String.valueOf(lineNo));
						args.put(SetBreakpoint.ARG_TYPE, type.toString());
						CommandHelper.execute(SetBreakpoint.ID, args);
					}
				});
			}
		}

		/*
		 * Clear breakpoints
		 */
		MenuItem clearBreakpoint = new MenuItem(m_menu, SWT.PUSH);
		clearBreakpoint.setText("Remove all breakpoints");
		clearBreakpoint.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				HashMap<String, String> args = new HashMap<String, String>();
				args.put(ClearBreakpoints.ARG_PROCID, m_procedureId);
				CommandHelper.execute(ClearBreakpoints.ID, args);
			}
		});

		/*
		 * Separator
		 */
		new MenuItem(m_menu, SWT.SEPARATOR);

		/*
		 * Copy selected code
		 */
		MenuItem copyCode = new MenuItem(m_menu, SWT.PUSH);
		copyCode.setText("Copy source");
		copyCode.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				m_viewer.copySelected();
			}
		});

		/*
		 * Separator
		 */
		new MenuItem(m_menu, SWT.SEPARATOR);

		/*
		 * Search code
		 */
		MenuItem search = new MenuItem(m_menu, SWT.PUSH);
		search.setText("Search...");
		search.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				SearchDialog dialog = new SearchDialog(m_viewer.getTable()
				        .getShell(), m_viewer);
				dialog.open();
			}
		});

		if (m_viewer.hasMatches())
		{
			/*
			 * Clear search
			 */
			MenuItem clear = new MenuItem(m_menu, SWT.PUSH);
			clear.setText("Clear search");
			clear.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					m_viewer.clearMatches();
				}
			});
		}

	}
}
