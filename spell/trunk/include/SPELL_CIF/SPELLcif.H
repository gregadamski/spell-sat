// ################################################################################
// FILE       : SPELLcif.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Main CIF object interface
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2014 SES ENGINEERING, Luxembourg S.A.R.L.
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
#include "SPELL_CIF/SPELLpromptDefinition.H"
#include "SPELL_CIF/SPELLcifStartupInfo.H"
// Project includes --------------------------------------------------------
#include "SPELL_EXC/SPELLvarInfo.H"
#include "SPELL_PRD/SPELLprocedureSourceCode.H"

#include "SPELL_SYN/SPELLthread.H"
#include "SPELL_SYN/SPELLevent.H"

#include "SPELL_EXC/SPELLexecutorConfiguration.H"

/** \addtogroup SPELL_CIF */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////

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
		 * \param info IN: configuration parameters
		**////////////////////////////////////////////////////////////////////
		virtual void setup( const SPELLcifStartupInfo& info );

		//////////////////////////////////////////////////////////////////////
		/** Cleanup the client interface.
		**////////////////////////////////////////////////////////////////////
		virtual void cleanup( bool force );

		//////////////////////////////////////////////////////////////////////
		/** Set the close condition to true.
		**////////////////////////////////////////////////////////////////////
		virtual void canClose();

		//////////////////////////////////////////////////////////////////////
		/** Check if the user/server wants the executor to be closed.
		**////////////////////////////////////////////////////////////////////
		virtual void waitClose();

		//////////////////////////////////////////////////////////////////////
		/** Reset the user/server close condition.
		**////////////////////////////////////////////////////////////////////
		virtual void resetClose();

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
		/** Get SPELL Executor Configuration
		 *
		 * \returns SPELLExecutorConfig
		**////////////////////////////////////////////////////////////////////
		SPELLexecutorConfig& getExecutorConfig() {
			return m_config;
		}

		//////////////////////////////////////////////////////////////////////
		/** Set the maximum allowed verbosity level.
		 *
		 * \param verbosityFilter IN: The maximum verbosity level
		**////////////////////////////////////////////////////////////////////
		void setVerbosityFilter( int vFilter);

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
		//virtual void notifyLine() = 0;
		virtual void notifyLine();

		//////////////////////////////////////////////////////////////////////
		/** Send a call notification to the client
		**////////////////////////////////////////////////////////////////////
		//virtual void notifyCall() = 0;
		virtual void notifyCall();

		//////////////////////////////////////////////////////////////////////
		/** Send a return notification to the client
		**////////////////////////////////////////////////////////////////////
		//virtual void notifyReturn() = 0;
		virtual void notifyReturn();

		//////////////////////////////////////////////////////////////////////
		/** Send a status notification to the client
		 *
		 * \param st IN: current status information
		**////////////////////////////////////////////////////////////////////
		//virtual void notifyStatus( const SPELLstatusInfo& st ) = 0;
		virtual void notifyStatus( const SPELLstatusInfo& st );

		//////////////////////////////////////////////////////////////////////
		/** Notify user action set
		**////////////////////////////////////////////////////////////////////
		virtual void notifyUserActionSet( const std::string& label, const unsigned int severity );

		//////////////////////////////////////////////////////////////////////
		/** Notify user action unset
		**////////////////////////////////////////////////////////////////////
		virtual void notifyUserActionUnset();

		//////////////////////////////////////////////////////////////////////
		/** Notify user action enable/disable
		**////////////////////////////////////////////////////////////////////
		virtual void notifyUserActionEnable( bool enable );

		//////////////////////////////////////////////////////////////////////
		/** Send an item notification to the client
		 *
		 * \param notification IN: notification data
		**////////////////////////////////////////////////////////////////////
		//virtual void notify( ItemNotification notification ) = 0;
		virtual void notify( ItemNotification notification );

		//////////////////////////////////////////////////////////////////////
		/** Send an error notification to the client
		 *
		 * \param error IN: error message
		 * \param reason IN: error cause, if known
		 * \param fatal IN: true if the error cannot be recovered
		**////////////////////////////////////////////////////////////////////
		virtual void notifyError( const std::string& error, const std::string& reason, bool fatal );

		//////////////////////////////////////////////////////////////////////
		/** Send a write message to the client with default configuration
		 *
		 * \param msg IN: message text
		 * \param scope IN: Scope unsigned integer
		**////////////////////////////////////////////////////////////////////
		//virtual void write( const std::string& msg, unsigned int scope = 2) = 0;
		virtual void write( const std::string& msg, unsigned int scope = 2);

		//////////////////////////////////////////////////////////////////////
		/** Send a warning message to the client
		 *
		 * \param msg IN: message text
		**////////////////////////////////////////////////////////////////////
		//virtual void warning( const std::string& msg, unsigned int scope = 2) = 0;
		virtual void warning( const std::string& msg, unsigned int scope = 2);

		//////////////////////////////////////////////////////////////////////
		/** Send a error message to the client
		 *
		 * \param msg IN: message text
		**////////////////////////////////////////////////////////////////////
		//virtual void error( const std::string& msg, unsigned int scope = 2 ) = 0;
		virtual void error( const std::string& msg, unsigned int scope = 2 );

		//////////////////////////////////////////////////////////////////////
		/** Send a log message
		 *
		 * \param msg IN: message text
		**////////////////////////////////////////////////////////////////////
		virtual void log( const std::string& msg ) {;};

		//////////////////////////////////////////////////////////////////////
		/** Issue a prompt to the client
		 *
		 * \param definition IN: prompt parameters
		**////////////////////////////////////////////////////////////////////
		//virtual std::string prompt( const SPELLpromptDefinition& definition ) = 0;
		virtual std::string prompt( const SPELLpromptDefinition& definition );

		//////////////////////////////////////////////////////////////////////
		/** Notify a variable change.
		**////////////////////////////////////////////////////////////////////
		virtual void notifyVariableChange( const std::vector<SPELLvarInfo>& added,
				                           const std::vector<SPELLvarInfo>& changed,
				                           const std::vector<SPELLvarInfo>& deleted );

		//////////////////////////////////////////////////////////////////////
		/** Notify a variable scope change.
		**////////////////////////////////////////////////////////////////////
		virtual void notifyVariableScopeChange( const std::string& scopeName,
				                                const std::vector<SPELLvarInfo>& globals,
				                                const std::vector<SPELLvarInfo>& locals );

		//////////////////////////////////////////////////////////////////////
		/** Substitute the current ASRUN file by the given one
		 *
		 * \param asrunFile IN: the new asrun file
		**////////////////////////////////////////////////////////////////////
		void useAsRun( const std::string& asrunFile );


		//////////////////////////////////////////////////////////////////////
		/** Open subprocedure.
		 *
		 * \param procId IN: identifier of the subprocedure
		 * \param callingLine IN: line where the procedure start call is
		 * \param args IN: arguments for the subprocedure
		 * \param automatic IN: automatic mode flag
		 * \param blocking IN: blocking mode flag
		 * \param visible IN: visible mode flag
		 *
		 * \returns The procedure identifier with instance number.
		**////////////////////////////////////////////////////////////////////
		std::string openSubprocedure( const std::string& procId,
									  int callingLine,
									  const std::string& args, bool automatic, bool blocking, bool visible );

		//////////////////////////////////////////////////////////////////////
		/** Close the given child procedure.
		 *
		 * \param procId IN: child procedure identifier.
		**////////////////////////////////////////////////////////////////////
		void closeSubprocedure( const std::string& procId );

		//////////////////////////////////////////////////////////////////////
		/** Kill the given child procedure.
		 *
		 * \param procId IN: child procedure identifier.
		**////////////////////////////////////////////////////////////////////
		void killSubprocedure( const std::string& procId );


    //====================================================================
    // Python bindings end
    //====================================================================

		void enableNotifications() { m_notificationsEnabled = true; };
		void disableNotifications() { m_notificationsEnabled = false; };
		bool notificationsEnabled() const { return m_notificationsEnabled; };

	//====================================================================
	// Shared data handling
	//====================================================================
		virtual std::string setSharedData( const std::string& name, const std::string& value, const std::string& expected, const std::string& scope ) { return "";};
		virtual std::string clearSharedData( const std::string& name, const std::string& scope )  { return "";};
		virtual std::string getSharedData( const std::string& name, const std::string& scope )  { return "";};
		virtual std::string getSharedDataKeys( const std::string& scope )  { return ""; };
		virtual std::string getSharedDataScopes()  { return ""; };
		virtual void removeSharedDataScope( const std::string& scope )  { return; };
		virtual void removeSharedDataScopes()  { return; };
		virtual void addSharedDataScope( const std::string& scope )  { return; };

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

		//////////////////////////////////////////////////////////////////////
		/** Return the FullStack or the Stack when the fullstack
		 * is not available.
		**////////////////////////////////////////////////////////////////////
		std::string getAvailableStack();

		//////////////////////////////////////////////////////////////////////
		/** Specific code of NotifyLine
		 *////////////////////////////////////////////////////////////////////
		virtual void specificNotifyLine() {;};

		//////////////////////////////////////////////////////////////////////
		/** Specific code of NotifyCall
		 *////////////////////////////////////////////////////////////////////
		virtual void specificNotifyCall() {;};

		//////////////////////////////////////////////////////////////////////
		/** Specific code of NotifyReturn
		 *////////////////////////////////////////////////////////////////////
		virtual void specificNotifyReturn() {;};

		//////////////////////////////////////////////////////////////////////
		/** Specific code of NotifyStatus
		 *////////////////////////////////////////////////////////////////////
		virtual void specificNotifyStatus( const SPELLstatusInfo& st ) {;};

		//////////////////////////////////////////////////////////////////////
		/** Specific code of Notify
		 *////////////////////////////////////////////////////////////////////
		virtual void specificNotify( ItemNotification notification ) {;};

		//////////////////////////////////////////////////////////////////////
		/** Specific code of NotifyError
		 *////////////////////////////////////////////////////////////////////
		virtual void specificNotifyError( const std::string& error, const std::string& reason, bool fatal ) {;};

		//////////////////////////////////////////////////////////////////////
		/** Specific code of Write
		 *////////////////////////////////////////////////////////////////////
		virtual void specificWrite( const std::string& msg, unsigned int scope = 2) {;};


		//////////////////////////////////////////////////////////////////////
		/** Specific code of Warning
		 *////////////////////////////////////////////////////////////////////
		virtual void specificWarning( const std::string& msg, unsigned int scope = 2 ) {;};

		//////////////////////////////////////////////////////////////////////
		/** Specific code of Error
		 *////////////////////////////////////////////////////////////////////
		virtual void specificError( const std::string& msg, unsigned int scope = 2 ) {;};

		//////////////////////////////////////////////////////////////////////
		/** Specific code for Prompt
		 *////////////////////////////////////////////////////////////////////
		virtual void specificPrompt( const SPELLpromptDefinition& def, std::string& rawAnswer, std::string& answerToShow ) {;};

		//////////////////////////////////////////////////////////////////////
		/** Specific code for SetupgetParentProcId
		 *////////////////////////////////////////////////////////////////////
		virtual void specificSetup( const SPELLcifStartupInfo& info ) {;};

		//////////////////////////////////////////////////////////////////////
		/** Specific code for Setup
		 *////////////////////////////////////////////////////////////////////
		virtual void specificCleanup( bool force ) {;};

		//////////////////////////////////////////////////////////////////////
		/** See Notify user action set
		**////////////////////////////////////////////////////////////////////
		virtual void specificNotifyUserActionSet( const std::string& label, const unsigned int severity ) {;};

		//////////////////////////////////////////////////////////////////////
		/** See Notify user action unset
		**////////////////////////////////////////////////////////////////////
		virtual void specificNotifyUserActionUnset() {;};

		//////////////////////////////////////////////////////////////////////
		/** See Notify user action enable/disable
		**////////////////////////////////////////////////////////////////////
		virtual void specificNotifyUserActionEnable( bool enable ) {;};


		//////////////////////////////////////////////////////////////////////
		/** See Notify a variable change.
		**////////////////////////////////////////////////////////////////////
		virtual void specificNotifyVariableChange( const std::vector<SPELLvarInfo>& added,
				                           const std::vector<SPELLvarInfo>& changed,
				                           const std::vector<SPELLvarInfo>& deleted ) {;};

		//////////////////////////////////////////////////////////////////////
		/** See Notify a variable scope change.
		**////////////////////////////////////////////////////////////////////
		virtual void specificNotifyVariableScopeChange( const std::string& scopeName,
				                                const std::vector<SPELLvarInfo>& globals,
				                                const std::vector<SPELLvarInfo>& locals ) {;};


		//////////////////////////////////////////////////////////////////////
		/** See OpenSubprocedure
		**////////////////////////////////////////////////////////////////////
		virtual std::string specificOpenSubprocedure( const std::string& procId,
											  int callingLine,
											  const std::string& args, bool automatic, bool blocking, bool visible ) { return ""; };

		//////////////////////////////////////////////////////////////////////
		/** See CloseSubprocedure
		**////////////////////////////////////////////////////////////////////
		virtual void specificCloseSubprocedure( const std::string& procId ) {;};

		//////////////////////////////////////////////////////////////////////
		/** See KillSubprocedure
		**////////////////////////////////////////////////////////////////////
		virtual void specificKillSubprocedure( const std::string& procId ) {;};


    // DATA MEMBERS //////////////////////////////////////////////////////////

	    /** Holds the timestamp identifier used for file generation like AsRun */
		std::string m_timeId;
		/** Holds the context name, if any */
		std::string m_ctxName;
		/** Holds the context port, if any */
		int m_ctxPort;
		/** Holds the AsRUN file generator */
		SPELLasRun* m_asRun;
		/** Notifications enabled flag */
		bool m_notificationsEnabled;


		/** Last line notification time */
		/** Optimization: prevent redundant line notifications */
		std::string m_lastStack;

		/** True if error was reported */
		bool m_errorState;

		/** Event for cleaning up connections */
		SPELLevent m_finishEvent;

		/** Holds the executor startup configuration */
		SPELLexecutorConfig     	 m_config;



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

		/** Synchronization with line notification timer */
		SPELLmutex m_lineLock;

};

/*@}*/
#endif
