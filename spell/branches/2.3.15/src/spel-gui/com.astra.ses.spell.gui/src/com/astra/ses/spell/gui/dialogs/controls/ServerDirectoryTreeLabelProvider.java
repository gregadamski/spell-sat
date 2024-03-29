///////////////////////////////////////////////////////////////////////////////
//
// PACKAGE   : com.astra.ses.spell.gui.dialogs.controls
// 
// FILE      : ServerDirectoryTreeLabelProvider.java
//
// DATE      : Feb 8, 2012
//
// Copyright (C) 2008, 2012 SES ENGINEERING, Luxembourg S.A.R.L.
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
///////////////////////////////////////////////////////////////////////////////
package com.astra.ses.spell.gui.dialogs.controls;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.astra.ses.spell.gui.Activator;
import com.astra.ses.spell.gui.core.model.server.DirectoryFile;
import com.astra.ses.spell.gui.core.model.server.DirectoryTree;

/**
 * @author Rafael Chinchilla
 *
 */
public class ServerDirectoryTreeLabelProvider extends LabelProvider  
{
	private Image m_folderImg;
	private Image m_fileImg;

	public ServerDirectoryTreeLabelProvider()
	{
		super();
		m_folderImg = Activator.getImageDescriptor("icons/16x16/folder.png").createImage();
		m_fileImg = Activator.getImageDescriptor("icons/16x16/asrun.png").createImage();
	}

	@Override
	public void dispose()
	{
		super.dispose();
		m_folderImg.dispose();
		m_fileImg.dispose();
	}
	
    @Override
    public Image getImage(Object element)
    {
    	if (element instanceof DirectoryTree)
    	{
    		return m_folderImg;
    	}
    	else if (element instanceof DirectoryFile )
    	{
    		return m_fileImg;
    	}
    	return null;
    }

    @Override
    public String getText(Object element)
    {
    	if (element instanceof DirectoryTree)
    	{
    		DirectoryTree tree = (DirectoryTree) element;
    		return tree.getName();
    	}
    	else if (element instanceof DirectoryFile)
    	{
    		DirectoryFile file = (DirectoryFile) element;
    		return file.getFilename();
    	}
    	return null;
    }
}
