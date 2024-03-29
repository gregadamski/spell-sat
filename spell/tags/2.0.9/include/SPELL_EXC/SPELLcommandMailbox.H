// ################################################################################
// FILE       : SPELLcommandMailbox.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Executor command mailbox. Holds the incoming commands for further
//				processing.
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

#ifndef __SPELL_CMD_MAILBOX_H__
#define __SPELL_CMD_MAILBOX_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_EXC/SPELLcommand.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLqueue.H"


/** \addtogroup SPELL_EXC */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Command mailbox
 **
 ** \par Description and usage:
 **
 **		Thread used by the SPELLcontroller to keep a queue of the incoming
 **		executor commands. There is a normal queue and a high priority
 **		queue.
 **
 **	\see SPELLqueue
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLcommandMailbox
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLcommandMailbox();

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLcommandMailbox();

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Put a command in the queues.
		 *
		 * \param cmd IN: comand to process
		 * \param hp IN: true if the high priority queue is to be used
		**////////////////////////////////////////////////////////////////////
		void push( const ExecutorCommand cmd, const bool hp );

		//////////////////////////////////////////////////////////////////////
		/** Get a command from the queues. Will block the caller until a command
		 * comes in.
		 *
		 * \return The newest executor command.
		**////////////////////////////////////////////////////////////////////
		ExecutorCommand pull();

		//////////////////////////////////////////////////////////////////////
		/** Reset
		**////////////////////////////////////////////////////////////////////
		void reset();

		//////////////////////////////////////////////////////////////////////
		/** Clears the last command executed, so that it can be repeated
		 * even if it is not repeatable.
		**////////////////////////////////////////////////////////////////////
		void commandProcessed() { m_lastCommand = ""; };

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////

		bool isCommandRepeatable( const std::string& id );

    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Last command pushed */
		std::string m_lastCommand;
		/** Normal priority comand queue */
		SPELLqueue<ExecutorCommand> m_queue;
		/** High priority command queue */
		SPELLqueue<ExecutorCommand> m_hqueue;
		/** Synchronization event */
		SPELLevent m_lock;
};

/*@}*/
#endif
