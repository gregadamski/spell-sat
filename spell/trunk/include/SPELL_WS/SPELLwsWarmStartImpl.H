// ################################################################################
// FILE       : SPELLwsWarmStartImpl.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the WS interface
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

#ifndef __SPELL_WARMSTART_IMPL_H__
#define __SPELL_WARMSTART_IMPL_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_WS/SPELLwarmstart.H"
#include "SPELL_WS/SPELLwsFrame.H"
#include "SPELL_WS/SPELLwsStorage.H"
// Project includes --------------------------------------------------------
#include "SPELL_SYN/SPELLmutex.H"
// System includes ---------------------------------------------------------

/** \addtogroup SPELL_WS */
/*@{*/

// FORWARD REFERENCES //////////////////////////////////////////////////////
// ENUMS ///////////////////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Implementation of a warmstart mechanism.
 **
 ** \par Description:
 **
 ** \par Usage:
 **
 ** \par Synchronization:
 **      No concurrency issues foreseen.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLwsWarmStartImpl : public SPELLwarmStart
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLwsWarmStartImpl();

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLwsWarmStartImpl();

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLwarmStart.
		**////////////////////////////////////////////////////////////////////
		void initialize( const SPELLwsStartupInfo& info );

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLwarmStart.
		**////////////////////////////////////////////////////////////////////
		void reset();

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLwarmStart.
		**////////////////////////////////////////////////////////////////////
		void cleanup();

	//========================================================================
	// METHODS TO KEEP THE SAVED STATE UPDATED
	//========================================================================

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLwarmStart.
		**////////////////////////////////////////////////////////////////////
		void notifyCall( const std::string& id, PyFrameObject* newFrame );

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLwarmStart.
		**////////////////////////////////////////////////////////////////////
		void notifyReturn();

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLwarmStart.
		**////////////////////////////////////////////////////////////////////
		void notifyLine();

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLwarmStart.
		**////////////////////////////////////////////////////////////////////
		void notifyStage();

	//========================================================================
	// METHODS TO CONTROL THE STORAGE PROCESS
	//========================================================================

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLwarmStart.
		**////////////////////////////////////////////////////////////////////
		SPELLwsWorkingMode getWorkingMode() const
		{
			return m_startup.workingMode;
		}

	//========================================================================
	// METHODS TO CONTROL STATE MANAGEMENT
	//========================================================================

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLwarmStart.
		**////////////////////////////////////////////////////////////////////
		PyFrameObject* restoreState();

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLwarmStart.
		**////////////////////////////////////////////////////////////////////
		PyFrameObject* fixState();

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLwarmStart.
		**////////////////////////////////////////////////////////////////////
		void saveState();

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLwarmStart.
		**////////////////////////////////////////////////////////////////////
		static void setGlobalsFilter( PyObject* dict );

		//////////////////////////////////////////////////////////////////////
		/** \see SPELLwarmStart.
		**////////////////////////////////////////////////////////////////////
		static bool shouldFilter( PyObject* key, PyObject* item );

	// DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////
	// LIFECYCLE /////////////////////////////////////////////////////////////
	// METHODS ///////////////////////////////////////////////////////////////
	// DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////
		/** Represents a list of frame data managers */
		typedef std::vector<SPELLwsFrame*> FrameList;
	// LIFECYCLE /////////////////////////////////////////////////////////////
	// METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Add a new frame manager as a result of a function call in the
		 * Python layer.
		 *
		 * \param frame IN: the newly created frame.
		**////////////////////////////////////////////////////////////////////
		void addFrame( const std::string& id, PyFrameObject* frame );

		//////////////////////////////////////////////////////////////////////
		/** Remove the frame manager on top, as a result of a return event
		 * in the Python layer. Do NOT delete Python objects associated to it.
		**////////////////////////////////////////////////////////////////////
		void removeTopFrame();

	// DATA MEMBERS //////////////////////////////////////////////////////////

		/** Synchronizes the ws operations */
		SPELLmutex m_lock;
		/** Holds the list of frame data managers */
		FrameList m_frames;
		/** Holds the top frame always */
		SPELLwsFrame* m_topFrame;
		/** Warmstart startup information */
		SPELLwsStartupInfo m_startup;
		/** Holds the file name of the associated persistent file */
		std::string m_persistentFile;
		/** Holds the file name of the associated recovery file */
		std::string m_recoveryFile;
		/** Recursion depth counter */
		unsigned int m_recursionDepth;
		/** Storage for the main data */
		SPELLwsStorage* m_storage;
		/** Stores the globals filter */
		static PyObject* s_filterKeys;
};

/*@}*/
#endif
