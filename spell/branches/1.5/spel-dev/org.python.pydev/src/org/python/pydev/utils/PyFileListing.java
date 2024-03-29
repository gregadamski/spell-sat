package org.python.pydev.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.python.pydev.core.structure.FastStringBuffer;
import org.python.pydev.editor.codecompletion.revisited.PythonPathHelper;
import org.python.pydev.plugin.PydevPlugin;

/**
 * Helper class for finding out about python files below some source folder.
 * 
 * @author Fabio
 */
public class PyFileListing {
    
    /**
     * Information about a python file found (the actual file and the way it was resolved as a python module)
     */
    public static final class PyFileInfo {

        private final String relPath;

        private final File file;

        public PyFileInfo(File file, String relPath) {
            this.file = file;
            this.relPath = relPath;
        }

        /** File object. */
        public File getFile() {
            return file;
        }

        /** Returns fully qualified name of module. */
        public String getModuleName() {
            return relPath;
        }
    }

    /**
     * Returns the directories and python files in a list.
     * 
     * @param addSubFolders indicates if sub-folders should be added
     * @param canonicalFolders used to know if we entered a loop in the listing (with symlinks)
     * @return An object with the results of making that listing.
     */
    @SuppressWarnings("unchecked")
    private static PyFileListing getPyFilesBelow(PyFileListing result, File file, FileFilter filter, 
            IProgressMonitor monitor, boolean addSubFolders, int level, boolean checkHasInit, String currModuleRep,
            Set<File> canonicalFolders) {
        

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        
        if (file != null && file.exists()) {
            //only check files that actually exist
    
            if (file.isDirectory()) {
                if(level != 0){
                    FastStringBuffer newModuleRep = new FastStringBuffer(currModuleRep, 128); 
                    if(newModuleRep.length() != 0){
                        newModuleRep.append(".");
                    }
                    newModuleRep.append(file.getName());
                    currModuleRep = newModuleRep.toString();
                }
                
                // check if it is a symlink loop
                try {
                    File canonicalizedDir = file.getCanonicalFile();
                    if (!canonicalizedDir.equals(file)) {
                        if (canonicalFolders.contains(canonicalizedDir)) {
                            return result;
                        } 
                    }
                    canonicalFolders.add(canonicalizedDir);
                } catch (IOException e) {
                    PydevPlugin.log(e);
                }

                File[] files = null;
    
                if (filter != null) {
                    files = file.listFiles(filter);
                } else {
                    files = file.listFiles();
                }
    
                boolean hasInit = false;
    
                List<File> foldersLater = new LinkedList<File>();
                
                for (File file2 : files) {
                    
                    if(monitor.isCanceled()){
                        break;
                    }
                    
                    if(file2.isFile()){
                        result.addPyFileInfo(new PyFileInfo(file2, currModuleRep));

                        monitor.worked(1);
                        monitor.setTaskName("Found:" + file2.toString());
                        
                        if (checkHasInit && hasInit == false){
                            //only check if it has __init__ if really needed
                            if(PythonPathHelper.isValidInitFile(file2.getName())){
                                hasInit = true;
                            }
                        }
                        
                    }else{
                        foldersLater.add(file2);
                    }
                }
                
                if(!checkHasInit || hasInit || level == 0){
                    result.foldersFound.add(file);
    
                    for (File folder : foldersLater) {
                        
                        if(monitor.isCanceled()){
                            break;
                        }
                        
                        if(folder.isDirectory() && addSubFolders){
                            
                            getPyFilesBelow(result, folder, filter, monitor, addSubFolders, level+1,
                                    checkHasInit, currModuleRep, canonicalFolders);
                            
                            monitor.worked(1);
                        }
                    }
                }
    
                
            } else if (file.isFile()) {
                result.addPyFileInfo(new PyFileInfo(file, currModuleRep));
                
            } else{
                throw new RuntimeException("Not dir nor file... what is it?");
            }
        }
        
        return result;
    }

    private static PyFileListing getPyFilesBelow(File file, FileFilter filter, IProgressMonitor monitor, boolean addSubFolders, boolean checkHasInit) {
        PyFileListing result = new PyFileListing();
        return getPyFilesBelow(result, file, filter, monitor, addSubFolders, 0, checkHasInit, "", new HashSet<File>());
    }

    public static PyFileListing getPyFilesBelow(File file, FileFilter filter, IProgressMonitor monitor, boolean checkHasInit) {
        return getPyFilesBelow(file, filter, monitor, true, checkHasInit);
    }

    /**
     * @param includeDirs determines if we can include subdirectories
     * @return a file filter only for python files (and other dirs if specified)
     */
    public static FileFilter getPyFilesFileFilter(final boolean includeDirs) {
        return new FileFilter() {
    
            public boolean accept(File pathname) {
                if (includeDirs){
                    if(pathname.isDirectory()){
                        return true;
                    }
                    if(PythonPathHelper.isValidSourceFile(pathname.toString())){
                        return true;
                    }
                    return false;
                }else{
                    if(pathname.isDirectory()){
                        return false;
                    }
                    if(PythonPathHelper.isValidSourceFile(pathname.toString())){
                        return true;
                    }
                    return false;
                }
            }
    
        };
    }

    /**
     * Returns the directories and python files in a list.
     * 
     * @param file
     * @return tuple with files in pos 0 and folders in pos 1
     */
    public static PyFileListing getPyFilesBelow(File file, IProgressMonitor monitor, final boolean includeDirs, boolean checkHasInit) {
        FileFilter filter = getPyFilesFileFilter(includeDirs);
        return getPyFilesBelow(file, filter, monitor, true, checkHasInit);
    }

    /**
     * @return All the IFiles below the current folder that are python files (does not check if it has an __init__ path)
     */
    public static List<IFile> getAllIFilesBelow(IFolder member) {
        final ArrayList<IFile> ret = new ArrayList<IFile>();
        try {
            member.accept(new IResourceVisitor(){
    
                public boolean visit(IResource resource) {
                    if(resource instanceof IFile){
                        ret.add((IFile) resource);
                        return false; //has no members
                    }
                    return true;
                }
                
            });
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }

    /**
     * The files we found as being valid for the given filter
     */
    private final List<PyFileInfo> pyFileInfos = new ArrayList<PyFileInfo>();
    
    /**
     * The folders we found as being valid for the given filter
     */
    private List<File> foldersFound = new ArrayList<File>();
    
    public PyFileListing() {
    }
    
    public Collection<PyFileInfo> getFoundPyFileInfos() {
      return pyFileInfos;
    }
    
    public Collection<File> getFoundFolders() {
      return foldersFound;
    }
    
    private void addPyFileInfo(PyFileInfo info) {
      pyFileInfos.add(info);
    }

}
