// ################################################################################
// FILE       : SPELLwsListDataHandler.C
// DATE       : Mar 17, 2011
// PROJECT    : SPELL
// DESCRIPTION: Implementation of the data handler for lists
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

// FILES TO INCLUDE ////////////////////////////////////////////////////////
// Local includes ----------------------------------------------------------
#include "SPELL_WS/SPELLwsListDataHandler.H"
#include "SPELL_WS/SPELLwsDataHandlerFactory.H"
// Project includes --------------------------------------------------------
#include "SPELL_UTIL/SPELLlog.H"
#include "SPELL_UTIL/SPELLpythonHelper.H"
// System includes ---------------------------------------------------------

//=============================================================================
// CONSTRUCTOR: SPELLwsListDataHandler::SPELLwsListDataHandler
//=============================================================================
SPELLwsListDataHandler::SPELLwsListDataHandler( PyObject* object )
: SPELLwsObjectDataHandler( object )
{
	/** \todo Review the need of storing additional information */
	setCode( DATA_CLASS );
}

//=============================================================================
// DESTRUCTOR: SPELLwsListDataHandler::~SPELLwsListDataHandler
//=============================================================================
SPELLwsListDataHandler::~SPELLwsListDataHandler()
{
}

//=============================================================================
// METHOD    : SPELLwsListDataHandler::write()
//=============================================================================
void SPELLwsListDataHandler::write()
{
	assert( PyList_Check(getObject()));

	unsigned int numItems = PyList_Size( getObject() );

	// Store the number of items
	getStorage()->storeLong( (long) numItems );

	// Store each list item
	for( unsigned int index = 0; index < numItems; index++)
	{
		PyObject* item = PyList_GetItem( getObject(), index );
		SPELLwsDataHandler* handler = SPELLwsDataHandlerFactory::createDataHandler(item);
		handler->setStorage(getStorage());
		// Store the item data code. We need it for reading operation, in order to
		// know which type of handler to create.
		handler->storeDataCode();

		// IMPORTANT in the case of lists and dictionaries, we want to be able to continue
		// the storage evenif there is a problem in the handler processing at this point.
		// If that is the case, a fake empty object will be replaced by the object being
		// processed by the handler, and the dumping of this collection will continue.
		try
		{
			// Store the data
			handler->write();
		}
		catch(SPELLcoreException& ex)
		{
			std::string msg = "WARNING! Storage of element " + ISTR(index) + " failed: " + STR(ex.what());
			LOG_WARN(msg);
			std::cerr << msg << std::endl;
			storeFakeObject( handler->getCode() );
		}
		delete handler;
	}
}

//=============================================================================
// METHOD    : SPELLwsListDataHandler::read()
//=============================================================================
void SPELLwsListDataHandler::read()
{
	// Load the number of items
	unsigned int numItems = getStorage()->loadLong();

	// Create a list
	PyObject* listObject = PyList_New(numItems);

	for( unsigned int index = 0; index < numItems; index++)
	{
		// Load the item code
		SPELLwsDataCode code = loadDataCode();
		// Create an appropriate handler
		SPELLwsDataHandler* handler = SPELLwsDataHandlerFactory::createDataHandler(code);
		handler->setStorage(getStorage());
		// Read the data
		handler->read();
		// Add the item to the list
		PyList_SetItem(listObject, index, handler->getObject());
		delete handler;
	}

	// Set it as associated object
	setObject( listObject );
}
