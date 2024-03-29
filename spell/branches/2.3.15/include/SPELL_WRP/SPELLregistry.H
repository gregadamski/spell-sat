// ################################################################################
// FILE       : SPELLregistry.H
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: SPELL registry wrapper
// --------------------------------------------------------------------------------
//
//  Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
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

#ifndef __SPELL_REGISTRY_H__
#define __SPELL_REGISTRY_H__

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// System includes ---------------------------------------------------------
// Local includes ----------------------------------------------------------
#include "SPELL_UTIL/SPELLbase.H"
// Project includes --------------------------------------------------------

/** \addtogroup SPELL_WRP */
/*@{*/


// FORWARD REFERENCES //////////////////////////////////////////////////////
// TYPES ///////////////////////////////////////////////////////////////////
// DEFINES /////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 *  \brief Wrapper for the SPELL registry.
 *
 *  \par Description and usage:
 *
 *  	This class wraps the SPELL registry object existing
 *   	on the Python layer. It provides basic access to the most relevant
 *   	methods of the spell.lib.Registry class. This wrapper is implemented as
 *   	a singleton, and it is used to register the Python bindings objects
 *   	on the Python side, so that C++ resources are made available to Python
 *   	code (e.g. the language and adapter modules).
 *
 * \see SPELLexecutor, SPELLbase
 */
//////////////////////////////////////////////////////////////////////////////
class SPELLregistry
{
public: //--------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		~SPELLregistry();

    // STATIC ////////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Obtain the singleton instance.
		 *
		 * \return The singleton instance.
		**////////////////////////////////////////////////////////////////////
		static SPELLregistry& instance();

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Obtain the instance of the given service.
		 *
		 * \param key IN: name of the service registered.
		 *
		 * \returns The instance of the service.
		**////////////////////////////////////////////////////////////////////
		PyObject* get( std::string key );

		//////////////////////////////////////////////////////////////////////
		/** Register a service (Python binding) on the SPELL registry.
		 *
		 * \param key IN: name of the service.
		 * \param obj IN: instance of the service (binding)
		**////////////////////////////////////////////////////////////////////
		void set( PyObject* obj, std::string key );

    // DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLregistry();

    // METHODS ///////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Access the Registry python class instance.
		 *
		 * \return The python object corresponding to the instance.
		**////////////////////////////////////////////////////////////////////
		PyObject* getRegistryObject();

    // DATA MEMBERS //////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

    // EXCEPTIONS ////////////////////////////////////////////////////////////
    // ENUMS /////////////////////////////////////////////////////////////////
    // TYPES /////////////////////////////////////////////////////////////////
    // LIFECYCLE /////////////////////////////////////////////////////////////
    // METHODS ///////////////////////////////////////////////////////////////
    // DATA MEMBERS //////////////////////////////////////////////////////////
		/** Holds the registry instance */
		PyObject* m_registry;
		/** Holds the singleton instance */
		static SPELLregistry* s_instance;
};

/*@}*/
#endif
