// ################################################################################
// FILE       : SPELLmonitor.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Monitor mechanism
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

#ifndef __SPELL_MONITOR_H__
#define __SPELL_MONITOR_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_SYN/SPELLmutex.H"
// Project includes --------------------------------------------------------

/** \addtogroup SPELL_SYN */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief Encapsulates a monitor mechanism
 **
 ** \par Description: utility class to easily implement synchronized method
 ** calls.
 **
 ** \par Usage: any method (function) constructing a Monitor object as the
 ** first operation in the method body will make it synchronized with any
 ** other method (function) that does the same, provided that both monitor
 ** objects share the same mutex object. The monitor locks automatically
 ** the mutex on creation.
 **
 ** Since the monitor is local to the method (function) it is automatically
 ** destroyed on body exit. The monitor unlocks the mutex automatically
 ** on deletion.
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLmonitor
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    /** Constructor.
     *
     * \param m INOUT: mutex object to be used for synchronization
    **////////////////////////////////////////////////////////////////////
    SPELLmonitor( SPELLmutex& m )
        : m_mutex(m)
    {
        m_mutex.lock();
    };

    //////////////////////////////////////////////////////////////////////
    /** Destructor.
    **////////////////////////////////////////////////////////////////////
    ~SPELLmonitor()
    {
        m_mutex.unlock();
    }

    // STATIC ////////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
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
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
    	/** Reference to the mutex used for synchronization */
    	SPELLmutex& m_mutex;
};

/*@}*/
#endif
