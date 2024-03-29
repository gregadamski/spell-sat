// ################################################################################
// FILE       : SPELLsharedData.H
// DATE       : Apr 11, 2011
// PROJECT    : SPELL
// DESCRIPTION: Shared data blackboard
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

#ifndef __SPELL_SHARED_DATA_H__
#define __SPELL_SHARED_DATA_H__

// FILES TO INCLUDE //////////////////////////////////////////////////////////
// Local includes ------------------------------------------------------------
// Project includes ----------------------------------------------------------
#include "SPELL_CTX/SPELLdataTable.H"

/** \addtogroup SPELL_CTX */
/*@{*/


// FORWARD REFERENCES ////////////////////////////////////////////////////////
// TYPES /////////////////////////////////////////////////////////////////////
// DEFINES ///////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
/**
 ** \brief
 **
**////////////////////////////////////////////////////////////////////////////
class SPELLsharedData
{
public: //--------------------------------------------------------------------

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////
	// LIFECYCLE /////////////////////////////////////////////////////////////

		//////////////////////////////////////////////////////////////////////
		/** Constructor.
		**////////////////////////////////////////////////////////////////////
		SPELLsharedData();

		//////////////////////////////////////////////////////////////////////
		/** Destructor.
		**////////////////////////////////////////////////////////////////////
		virtual ~SPELLsharedData();

	// METHODS ///////////////////////////////////////////////////////////////

	    //////////////////////////////////////////////////////////////////////
	    /** Remove all variables from a variable scope. Does not block on
	     * locked variables.
	    **////////////////////////////////////////////////////////////////////
	    void clearScope( const std::string& scope );

	    //////////////////////////////////////////////////////////////////////
	    /** Add a new scope
	    **////////////////////////////////////////////////////////////////////
	    void addScope( const std::string& scope );

	    //////////////////////////////////////////////////////////////////////
	    /** Remove a scope
	    **////////////////////////////////////////////////////////////////////
	    void removeScope( const std::string& scope );

	    //////////////////////////////////////////////////////////////////////
	    /** Check if a scope is present
	    **////////////////////////////////////////////////////////////////////
	    bool hasScope( const std::string& scope );

	    //////////////////////////////////////////////////////////////////////
	    /** Remove all variables. Does not block on locked variables.
	    **////////////////////////////////////////////////////////////////////
	    void clear();

	    //////////////////////////////////////////////////////////////////////
	    /** Remove a variable in the given scope.
	    **////////////////////////////////////////////////////////////////////
	    bool clear( const std::string& name, const std::string& scope );

	    //////////////////////////////////////////////////////////////////////
	    /** Remove a variable in the global scope.
	    **////////////////////////////////////////////////////////////////////
	    bool clear( const std::string& name )
	    {
	    	return clear(name,GLOBAL_SCOPE);
	    }

	    //////////////////////////////////////////////////////////////////////
	    /** Set a variable value. Blocks if the variable is locked.
	    **////////////////////////////////////////////////////////////////////
	    bool set( const std::string& name, const std::string& value, const std::string& expected, const std::string& scope );

	    //////////////////////////////////////////////////////////////////////
	    /** Set a variable value in the global scope.
	    **////////////////////////////////////////////////////////////////////
	    bool set( const std::string& name, const std::string& value, const std::string& expected )
	    {
	    	return set(name,value,expected,GLOBAL_SCOPE);
	    }

	    //////////////////////////////////////////////////////////////////////
	    /** Set a variable value in the global scope.
	    **////////////////////////////////////////////////////////////////////
	    bool set( const std::string& name, const std::string& value )
	    {
	    	return set(name,value,NO_EXPECTED,GLOBAL_SCOPE);
	    }

	    //////////////////////////////////////////////////////////////////////
	    /** Get a variable value.
	    **////////////////////////////////////////////////////////////////////
	    std::string get( const std::string& name, const std::string& scope );

	    //////////////////////////////////////////////////////////////////////
	    /** Get a variable value in the global scope.
	    **////////////////////////////////////////////////////////////////////
	    std::string get( const std::string& name )
	    {
	    	return get(name,GLOBAL_SCOPE);
	    }

	    //////////////////////////////////////////////////////////////////////
	    /** Get all variable names in the given scope
	    **////////////////////////////////////////////////////////////////////
	    SPELLdataTable::KeyList getVariables( const std::string& scope );

	    //////////////////////////////////////////////////////////////////////
	    /** Get all existing scopes
	    **////////////////////////////////////////////////////////////////////
	    SPELLdataTable::KeyList getScopes();

	    //////////////////////////////////////////////////////////////////////
	    /** Get all variable names in the global scope.
	    **////////////////////////////////////////////////////////////////////
	    SPELLdataTable::KeyList getVariables()
	    {
	    	return getVariables(GLOBAL_SCOPE);
	    }

	// DATA MEMBERS //////////////////////////////////////////////////////////

protected: //-----------------------------------------------------------------

		friend class SPELLclientInterestList;

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////
	// STATIC ////////////////////////////////////////////////////////////////
	// LIFECYCLE /////////////////////////////////////////////////////////////
	// METHODS ///////////////////////////////////////////////////////////////

private: //-------------------------------------------------------------------

	// EXCEPTIONS ////////////////////////////////////////////////////////////
	// ENUMS /////////////////////////////////////////////////////////////////
	// TYPES /////////////////////////////////////////////////////////////////

		typedef std::map<std::string,SPELLdataTable*> DataTableMap;

	// STATIC ////////////////////////////////////////////////////////////////
	// LIFECYCLE /////////////////////////////////////////////////////////////
	// METHODS ///////////////////////////////////////////////////////////////
	// DATA MEMBERS //////////////////////////////////////////////////////////

	    DataTableMap m_tables;
	    SPELLmutex m_lock;

};

/*@}*/
#endif
