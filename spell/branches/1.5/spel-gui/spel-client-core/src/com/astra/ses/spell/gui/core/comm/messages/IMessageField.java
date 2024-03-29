///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.core.comm.messages
// 
// FILE      : IMessageField.java
//
// DATE      : 2008-11-21 08:58
//
// Copyright (C) 2008, 2010 SES ENGINEERING, Luxembourg S.A.R.L.
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
package com.astra.ses.spell.gui.core.comm.messages;

interface IMessageField
{
	// Basics
	/** Field identifier for the message id */
	public static final String FIELD_ID = "Id";
	/** Field identifier for the message GUI source key */
	public static final String FIELD_IPC_KEY = "IpcKey";
	/** Field identifier for the originator of the message */
	public static final String FIELD_SENDER_ID = "SenderId";
	/** Field identifier for the originator of the message */
	public static final String FIELD_RECEIVER_ID = "ReceiverId";
	/** Field identifier for the message sequence count */
	public static final String FIELD_SEQUENCE = "IpcSeq";
	/** Field identifier for the host */
	public static final String FIELD_HOST = "Host";
	/** Field identifier for the time */
	public static final String FIELD_TIME = "Time";
	/** Field identifier for chunk number */
	public static final String FIELD_CHUNK = "CurrentChunk";
	/** Field identifier for total chunk number */
	public static final String FIELD_TOTAL_CHUNKS = "TotalChunks";
	
	////////////////////////////////////////////////////////////////////////////
	// Procedure-related message fields
	////////////////////////////////////////////////////////////////////////////
	
	public static final String FIELD_PROC_ID 	    = "ProcId";
	public static final String FIELD_PARENT_PROC    = "ParentId";
	public static final String FIELD_SERVER_FILE_ID = "ServerFileId";
	public static final String FIELD_INSTANCE_ID    = "InstanceId";
	public static final String FIELD_PROC_NAME 	    = "ProcName";
	public static final String FIELD_PROC_LIST 	    = "ProcList";

	////////////////////////////////////////////////////////////////////////////
	// Execution-related message fields
	////////////////////////////////////////////////////////////////////////////

	public static final String FIELD_CSP        = "Csp";
	public static final String FIELD_STAGE_ID   = "StageId";
	public static final String FIELD_STAGE_TL   = "StageTl";
	public static final String FIELD_TEXT 		= "Text";
	public static final String FIELD_CLINE      = "CurrentLine";
	public static final String FIELD_GOTO_LINE  = "GotoLine";
	public static final String FIELD_GOTO_LABEL = "GotoLabel";
	public static final String FIELD_SCRIPT     = "Script";
	public static final String FIELD_CODE 		= "ProcCode";
	public static final String FIELD_SERVER_FILE= "ServerFile";
	public static final String FIELD_ARGS		= "Arguments";
	public static final String FIELD_SO         = "So";
	public static final String FIELD_CONDITION  = "Condition";
	public static final String FIELD_BACKGROUND = "Background";

	////////////////////////////////////////////////////////////////////////////
	// Executor configuration-related message fields
	////////////////////////////////////////////////////////////////////////////
	public static final String FIELD_RUN_INTO   = "RunInto";
	public static final String FIELD_EXEC_DELAY = "ExecDelay";
	public static final String FIELD_BY_STEP    = "ByStep";

	////////////////////////////////////////////////////////////////////////////
	// Failure-related message fields
	////////////////////////////////////////////////////////////////////////////
	
	public static final String FIELD_ERROR      = "ErrorMsg";
	public static final String FIELD_REASON  	= "ErrorReason";
	
	////////////////////////////////////////////////////////////////////////////
	// Context-related message fields
	////////////////////////////////////////////////////////////////////////////
	
	public static final String FIELD_CTX_NAME  	= "ContextName";
	public static final String FIELD_CTX_LIST   = "ContextList";
	public static final String FIELD_CTX_STATUS = "ContextStatus";
	public static final String FIELD_CTX_SC     = "ContextSC";
	public static final String FIELD_CTX_PORT   = "ContextPort";
	public static final String FIELD_CTX_DRV    = "ContextDriver";
	public static final String FIELD_CTX_FAM    = "ContextFamily";
	public static final String FIELD_CTX_GCS    = "ContextGCS";
	public static final String FIELD_CTX_DESC   = "ContextDescription";
	public static final String FIELD_CTX_MAXPROC= "MaxProc";

	////////////////////////////////////////////////////////////////////////////
	// Executor-related message fields
	////////////////////////////////////////////////////////////////////////////

	public static final String FIELD_EXEC_LIST  = "ExecutorList";
	public static final String FIELD_EXEC_STATUS= "ExecutorStatus";
	public static final String FIELD_EXEC_PORT  = "ExecutorPort";
	public static final String FIELD_GUI_LIST   = "GuiList";
	public static final String FIELD_GUI_CONTROL= "GuiControl";
	public static final String FIELD_OPEN_MODE  = "OpenMode";

	////////////////////////////////////////////////////////////////////////////
	// Client-related message fields
	////////////////////////////////////////////////////////////////////////////
	public static final String FIELD_GUI_MODE   = "GuiMode";
	public static final String FIELD_GUI_KEY    = "GuiKey";

	////////////////////////////////////////////////////////////////////////////
	// Operation message fields
	////////////////////////////////////////////////////////////////////////////
	public static final String FIELD_EXEC_OP    = "ExecOp";
	public static final String FIELD_CLIENT_OP  = "CltOp";
	public static final String FIELD_CTX_OP     = "CtxOp";

	////////////////////////////////////////////////////////////////////////////
	// User-related message fields
	////////////////////////////////////////////////////////////////////////////

	// Writes
	public static final String FIELD_MSG_TYPE 	= "MsgType";
	public static final String FIELD_LEVEL 		= "Level";
	
	// Prompts
	public static final String FIELD_EXPECTED 	= "ExpectedValues";
	public static final String FIELD_OPTIONS 	= "OptionValues";
	public static final String FIELD_RVALUE 	= "ReturnValue";
	
	// Notifications
	public static final String FIELD_DATA_TYPE      = "DataType";
	public static final String FIELD_ITEM_TYPE      = "ItemType";
	public static final String FIELD_ITEM_NAME      = "ItemName";
	public static final String FIELD_ITEM_VALUE     = "ItemValue";
	public static final String FIELD_ITEM_STATUS    = "ItemStatus";
	public static final String FIELD_ITEM_COMMENT   = "ItemReason";
	public static final String FIELD_ITEM_TIME      = "ItemTime";
	public static final String FIELD_SCOUNT         = "SCount";
	public static final String FIELD_ITEM_EOS       = "EndOfScript";
	public static final String FIELD_EXECUTION_MODE = "ExecutorMode";
}
