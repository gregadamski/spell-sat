///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.dev.database
// 
// FILE      : DatabaseDriverManager.java
//
// DATE      : 2008-11-21 13:54
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
// SUBPROJECT: SPELL DEV
//
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.dev.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.astra.ses.spell.dev.database.interfaces.ISpellDatabaseDriver;


public class DatabaseDriverManager {

	private static final String DB_INTERFACE_EXTENSION_ID = "com.astra.ses.spell.dev.database.DatabaseInterface";
	
	private static final String ELEMENT_NAME = "name";
	private static final String ELEMENT_DESC = "description";
	private static final String ELEMENT_CLASS = "class";

	/** Collection of available databases */
	private static Map<String, ISpellDatabaseDriver> m_drivers;
	private static DatabaseDriverManager s_instance;
	
	/**************************************************************************
	 * Get the singleton instance of this class
	 * @return
	 *************************************************************************/
	public static DatabaseDriverManager getInstance()
	{
		if (s_instance == null)
		{
			s_instance = new DatabaseDriverManager();
		}
		return s_instance;
	}
	
	/**************************************************************************
	 * Constructor
	 *************************************************************************/
	private DatabaseDriverManager()
	{
		m_drivers = new HashMap<String, ISpellDatabaseDriver>();
		loadExtensions();
	}
	
	/**************************************************************************
	 * Return the available drivers names
	 * @return
	 *************************************************************************/
	public Collection<String> getDriversNames()
	{
		Collection<ISpellDatabaseDriver> drivers = getAvailableDBManagers();
		ArrayList<String> result = new ArrayList<String>();
		for (ISpellDatabaseDriver driver : drivers)
		{
			result.add(driver.getName());
		}
		return result;
	}
	
	/**************************************************************************
	 * Return the names of the available databases in the system
	 * @return
	 *************************************************************************/
	public Collection<ISpellDatabaseDriver> getAvailableDBManagers()
	{
		return m_drivers.values();
	}
	
	/**************************************************************************
	 * Get the driver by giving its name
	 * @param name
	 * @return
	 *************************************************************************/
	public ISpellDatabaseDriver getDriver(String name)
	{
		return m_drivers.get(name);
	}
	
	/**************************************************************************
	 * Load the plugins which use the extension point provided by this plugin
	 *************************************************************************/
	private void loadExtensions() 
	{	
		System.out.println("[*] Loading extensions for point '" + DB_INTERFACE_EXTENSION_ID + "'");
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint ep = registry.getExtensionPoint(DB_INTERFACE_EXTENSION_ID);
		IExtension[] extensions = ep.getExtensions();
		System.out.println("[*] Defined extensions: "+ extensions.length);
		for(IExtension extension : extensions)
		{
			System.out.println("[*] Extension ID: "+ extension.getUniqueIdentifier());
			// Obtain the configuration element for this extension point
			IConfigurationElement cfgElem = extension.getConfigurationElements()[0];
			String elementName  = cfgElem.getAttribute(ELEMENT_NAME);
			String elementDesc  = cfgElem.getAttribute(ELEMENT_DESC);
			String elementClass = cfgElem.getAttribute(ELEMENT_CLASS);
			if (true)
			{
				System.out.println("[*] Extension properties");
				System.out.println("[*] \t- Name : "+ elementName);
				System.out.println("[*] \t- Desc : "+ elementDesc);
				System.out.println("[*] \t- Class: "+ elementClass);
			}
			try
			{
				ISpellDatabaseDriver extensionInterface = (ISpellDatabaseDriver) ISpellDatabaseDriver.class.cast(cfgElem.createExecutableExtension(ELEMENT_CLASS));
				m_drivers.put(extensionInterface.getName(), extensionInterface);
				System.out.println("[*] Extension loaded: " + extensionInterface.getName());
			}
			catch(CoreException ex)
			{
				ex.printStackTrace();
			}
			System.out.println("[*] --------------------------------------------");
		}
		System.out.println("[*] All extensions loaded for this point\n");
	}
}
