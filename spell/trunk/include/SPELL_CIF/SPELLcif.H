// ################################################################################
// FILE       : SPELLcif.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Main CIF object interface
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2011 SES ENGINEERING, Luxembourg S.A.R.L.
//
//  This file is part of SPELL.
//
// SPELL is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// SPELL is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with SPELL. If not, see <http://www.gnu.org/licenses/>.
//
// ################################################################################

#ifndef __SPELL_CLIENTIF_H__
#define __SPELL_CLIENTIF_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_CIF/SPELLasRun.H"
#include "SPELL_CIF/SPELLnotifications.H"
#include "SPELL_CIF/SPELLstatusInfo.H"
// Project includes --------------------------------------------------------
#include "SPELL_EXC/SPELLvarInfo.H"
#include "SPELL_EXC/SPELLscopeInfo.H"
#include "SPELL_PRD/SPELLprocedureSourceCode.H"

/** \addtogroup SPELL_CIF */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////

//============================================================================
/** Represents the list of available options for a prompt */
typedef std::list<std::string> PromptOptions;
//============================================================================

//============================================================================
/** Return value for cancelled prompts */
#define PROMPT_CANCELLED "<CANCEL>"
/** Return value for timed-out prompts */
#define PROMPT_TIMEOUT   "<TIMEOUT>"
/** Return value for failed prompts */
#define PROMPT_ERROR     "<ERROR>"
/** Return value for prompts without clients to attend*/
#define PROMPT_NOCLIENT  "<NOCLIENT>"
//============================================================================

//============================================================================
/** Python representation of the SPELL client interface */
typedef struct PyClientIFObject_
{
    PyObject_HEAD;
}
PyClientIFObject;
//============================================================================

//============================================================================
/** Installs the CIF binding object in the SPELL registry */
void ClientIF_Install();
//============================================================================


// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Base class of client interfaces.
 **
 ** \par Description and usage:
 **
 ** 	The CIF or client interface is the communication pathway
 **  	between the executor process and the controlling clients, if any.
 **
 **  	Depending on the type of CIF, the communication is done via IPC
 **  	(\c SPELLserverCif), using NCurses directly (\c SPELLstandaloneCif)
 **  	or there is no communication (\c SPELLatomaticCif).
 **
 ** 	Client interfaces are to be instantiated in the main function
 **  	of every SPELL program and passed to the SPELLinterpreter instance for
 **  	their usage. CIFs are setup internally in the interpreter.
 **
 **     Concurrency issues may require special management, depending on
 **     the type of CIF.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLcif
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLcif();

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLcif();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Setup the client interface.
		 *
		 * \param procId IN: name of the current procedure
		 * \param ctxName IN: name of the current context, for configuration
		 * \param ctxPort IN: port of the current context, if any
		 * \param timeId IN: used for AsRun file naming
		**////////////////////////////////////////////////////////////////////
		virtual void setup( const std::string& procId,
							const std::string& ctxName,
							int ctxPort,
							const std::string& timeId );

		//////////////////////////////////////////////////////////////////////
		/** Cleanup the client interface.
		**////////////////////////////////////////////////////////////////////
		virtual void cleanup( bool force );

		//////////////////////////////////////////////////////////////////////
		/** Set the close condition to true.
		**////////////////////////////////////////////////////////////////////
		virtual void canClose() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Check if the user/server wants the executor to be closed.
		**////////////////////////////////////////////////////////////////////
		virtual void waitClose() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Reset the user/server close condition.
		**////////////////////////////////////////////////////////////////////
		virtual void resetClose() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the currently configured verbosity level.
		 *
		 * \returns The verbosity level
		**////////////////////////////////////////////////////////////////////
		const int getVerbosity() const;

		//////////////////////////////////////////////////////////////////////
		/** Get the maximum allowed verbosity level.
		 *
		 * \returns The maximum verbosity level
		**////////////////////////////////////////////////////////////////////
		const int getVerbosityFilter() const;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the current procedure identifier.
		 *
		 * \returns The procedure identifier
		**////////////////////////////////////////////////////////////////////
		const std::string getProcId() const;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the current call stack.
		 *
		 * \returns The current stack information
		**////////////////////////////////////////////////////////////////////
		const std::string getStack() const;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the current code name.
		 *
		 * \returns The current code name string
		**////////////////////////////////////////////////////////////////////
		const std::string getCodeName() const;

		//////////////////////////////////////////////////////////////////////
		/** Obtain the current stage.
		 *
		 * \returns The current stage information
		**////////////////////////////////////////////////////////////////////
		const std::string getStage() const;

		//////////////////////////////////////////////////////////////////////
		/** Check if the CIF is in manual mode.
		 *
		 * \returns True if the CIF is in manual mode
		**////////////////////////////////////////////////////////////////////
		const bool isManual() const;

		//////////////////////////////////////////////////////////////////////
		/** Check if the user library is browsable
		 *
		 * \returns True if the library is browsable
		**////////////////////////////////////////////////////////////////////
		const bool isBrowsableLib() const {
			return m_browsableLib;
		};

		//////////////////////////////////////////////////////////////////////
		/** Get the procedure arguments if any.
		 *
		 *\returns The procedure argument string if any
		**////////////////////////////////////////////////////////////////////
		virtual std::string getArguments() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Get the procedure condition if any.
		 *
		 * \returns The procedure condition string if any
		**////////////////////////////////////////////////////////////////////
		virtual std::string getCondition() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Get the procedure open mode 'automatic' parameter.
		 *
		 * \returns True if the procedure will run in automatic mode.
		**////////////////////////////////////////////////////////////////////
		virtual bool isAutomatic() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Get the procedure open mode 'visible' parameter.
		 *
		 * \returns True if the procedure will run in visible mode.
		**////////////////////////////////////////////////////////////////////
		virtual bool isVisible() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Get the procedure open mode 'blocking' parameter.
		 *
		 * \returns True if the procedure will run in blocking mode.
		**////////////////////////////////////////////////////////////////////
		virtual bool isBlocking() = 0;

    //====================================================================
    // Python bindings start
    //====================================================================

		//////////////////////////////////////////////////////////////////////
		/** Set the manual mode flag. In manual mode, item notifications
		 * are discarded.
		 *
		 * \param manual IN: True if manual mode is to be enabled
		**////////////////////////////////////////////////////////////////////
		void setManualMode( bool manual );

		//////////////////////////////////////////////////////////////////////
		/** Set current verbosity level. This levels determines which message
		 * displays actually reach the clients and which dont.
		 *
		 * \param verbosity IN: verbosity level to be used
		**////////////////////////////////////////////////////////////////////
		void setVerbosity( int verbosity );

		//////////////////////////////////////////////////////////////////////
		/** Set the current verbosity to the maximum allowed value
		**////////////////////////////////////////////////////////////////////
		void setMaxVerbosity();

		//////////////////////////////////////////////////////////////////////
		/** Set verbosity level to initial status.
		**////////////////////////////////////////////////////////////////////
		void resetVerbosity();

		//////////////////////////////////////////////////////////////////////
		/** Get the AsRUN file name
		 *
		 * \return The AsRUN file name
		**////////////////////////////////////////////////////////////////////
		const std::string getAsRunName() const;

		//////////////////////////////////////////////////////////////////////
		/** Reset (clear) the AsRUN file
		**////////////////////////////////////////////////////////////////////
		void clearAsRun();

		//////////////////////////////////////////////////////////////////////
		/** Send a line notification to the client
		**////////////////////////////////////////////////////////////////////
		virtual void notifyLine() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Send a call notification to the client
		**////////////////////////////////////////////////////////////////////
		virtual void notifyCall() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Send a return notification to the client
		**////////////////////////////////////////////////////////////////////
		virtual void notifyReturn() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Send a status notification to the client
		 *
		 * \param st IN: current status information
		**////////////////////////////////////////////////////////////////////
		virtual void notifyStatus( const SPELLstatusInfo& st ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Notify user action set
		**////////////////////////////////////////////////////////////////////
		virtual void notifyUserActionSet( const std::string& label, const unsigned int severity ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Notify user action unset
		**////////////////////////////////////////////////////////////////////
		virtual void notifyUserActionUnset() = 0;

		//////////////////////////////////////////////////////////////////////
		/** Notify user action enable/disable
		**////////////////////////////////////////////////////////////////////
		virtual void notifyUserActionEnable( bool enable ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Send an item notification to the client
		 *
		 * \param notification IN: notification data
		**////////////////////////////////////////////////////////////////////
		virtual void notify( ItemNotification notification ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Send an error notification to the client
		 *
		 * \param error IN: error message
		 * \param reason IN: error cause, if known
		 * \param fatal IN: true if the error cannot be recovered
		**////////////////////////////////////////////////////////////////////
		virtual void notifyError( const std::string& error, const std::string& reason, bool fatal ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Send a write message to the client with default configuration
		 *
		 * \param msg IN: message text
		**////////////////////////////////////////////////////////////////////
		virtual void write( const std::string& msg, unsigned int scope ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Send a warning message to the client
		 *
		 * \param msg IN: message text
		**////////////////////////////////////////////////////////////////////
		virtual void warning( const std::string& msg, unsigned int scope ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Send a error message to the client
		 *
		 * \param msg IN: message text
		**////////////////////////////////////////////////////////////////////
		virtual void error( const std::string& msg, unsigned int scope ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Send a log message
		 *
		 * \param msg IN: message text
		**////////////////////////////////////////////////////////////////////
		virtual void log( const std::string& msg ) {;};

		//////////////////////////////////////////////////////////////////////
		/** Issue a prompt to the client
		 *
		 * \param message IN: prompt message text
		 * \param options IN: prompt options, for prompt list type
		 * \param ptype    IN: prompt type
		**////////////////////////////////////////////////////////////////////
		virtual std::string prompt( const std::string& message, PromptOptions options, unsigned int ptype, unsigned int scope ) = 0;

		//////////////////////////////////////////////////////////////////////
		/** Notify a variable change.
		**////////////////////////////////////////////////////////////////////
		virtual void notifyVariableChange( const std::vector<SPELLvarInfo>& changed ) {;};

		//////////////////////////////////////////////////////////////////////
		/** Notify a variable scope change.
		**////////////////////////////////////////////////////////////////////
		virtual void notifyVariableScopeChange( const SPELLscopeInfo& info ) {;};

    //====================================================================
    // Python bindings end
    //====================================================================

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // STATIC ////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Obtain the number of executions for the current line.
		**////////////////////////////////////////////////////////////////////
		unsigned int getNumExecutions() const;

    // DATA MEMBERS //////////////////////////////////////////////////////////

	    /** Holds the timestamp identifier used for file generation like AsRun */
		std::string m_timeId;
		/** Holds the context name, if any */
		std::string m_ctxName;
		/** Holds the context port, if any */
		int m_ctxPort;
		/** Holds the AsRUN file generator */
		SPELLasRun* m_asRun;
		/** Borwsable lib flag */
		bool m_browsableLib;

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Holds the procedure identifier */
		std::string m_procId;
		/** True if manual mode is enabled */
		bool m_manualMode;
		/** Current verbosity level */
		int m_verbosity;
		/** Maximum verbosity level */
		int m_verbosityFilter;
};

/*@}*/
#endif