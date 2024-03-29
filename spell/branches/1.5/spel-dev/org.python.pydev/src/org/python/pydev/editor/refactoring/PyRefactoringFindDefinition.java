package org.python.pydev.editor.refactoring;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.python.pydev.core.FullRepIterable;
import org.python.pydev.core.ICompletionCache;
import org.python.pydev.core.IDefinition;
import org.python.pydev.core.IModule;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.REF;
import org.python.pydev.core.Tuple;
import org.python.pydev.core.Tuple3;
import org.python.pydev.core.docutils.PySelection;
import org.python.pydev.editor.codecompletion.revisited.CompletionCache;
import org.python.pydev.editor.codecompletion.revisited.CompletionStateFactory;
import org.python.pydev.editor.codecompletion.revisited.modules.SourceModule;
import org.python.pydev.editor.codecompletion.revisited.visitors.Definition;
import org.python.pydev.editor.model.ItemPointer;
import org.python.pydev.editor.model.Location;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.nature.SystemPythonNature;

/**
 * @note This class was refactored and moved from the Pydev Extensions version to be able to provide a context-sensitive
 * find definition that can be properly used in the open source version of Pydev.
 */
public class PyRefactoringFindDefinition {

    /**
     * @param request OUT: the request to be used in the find definition. It'll be prepared inside this method, and if it's not
     * a suitable request for the find definition, the return of this function will be null, otherwise, it was correctly
     * prepared for a find definition action.
     * 
     * @param completionCache the completion cache to be used
     * @param selected OUT: fills the array with the found definitions
     * 
     * @return an array with 2 strings: the activation token and the qualifier used. The return may be null, in which case
     *      the refactoring request is not valid for a find definition.
     */
    public static String[] findActualDefinition(RefactoringRequest request, CompletionCache completionCache,
            ArrayList<IDefinition> selected) {
        //ok, let's find the definition.
        request.createSubMonitor(50);
        request.getMonitor().beginTask("Find definition", 5);
        request.communicateWork("Finding Definition");
        
        IModule mod = prepareRequestForFindDefinition(request);
        if(mod == null){
            return null;
        }
        
        String modName = request.moduleName;
        request.communicateWork("Module name found:"+modName);
        

        //1. we have to know what we're looking for (activationToken)
        String[] tokenAndQual = PySelection.getActivationTokenAndQual(request.getDoc(), request.ps.getAbsoluteCursorOffset(), true);
        String tok = tokenAndQual[0] + tokenAndQual[1];
        
        //2. check findDefinition (SourceModule)
        try {
            int beginLine = request.getBeginLine();
            int beginCol = request.getBeginCol()+1;
            IPythonNature pythonNature = request.nature;

            PyRefactoringFindDefinition.findActualDefinition(request, mod, tok, selected, beginLine, beginCol, pythonNature, completionCache);
        } catch (OperationCanceledException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tokenAndQual;
    }
    

    /**
     * Prepares a request to a find definition operation.
     *  
     * @param request IN/OUT the request that's being used for a find definition operation. Will change it so that
     * a find definition can be done.
     * 
     * @return the module to be used or null if the given request is not suitable for a find definition operation.
     */
    private static IModule prepareRequestForFindDefinition(RefactoringRequest request) {
        String modName = null;
        
        //all that to try to give the user a 'default' interpreter manager, for whatever he is trying to search
        //if it is in some pythonpath, that's easy, but if it is some file somewhere else in the computer, this
        //might turn out a little tricky.
        if(request.nature == null){
            //the request is not associated to any project. It is probably a system file. So, let's check it...
            Tuple<SystemPythonNature,String> infoForFile = PydevPlugin.getInfoForFile(request.file);
            if(infoForFile != null){
                modName = infoForFile.o2;
                request.nature = infoForFile.o1;
                request.inputName = modName;
            }else{
                return null;
            }
        }
        
        
        if(modName == null){
            modName = request.resolveModule();
        }
        
        if(request.nature == null){
            PydevPlugin.logInfo("Unable to resolve nature for find definition request (python or jython interpreter may not be configured).");
            return null;
        }
        
        //check if it is already initialized....
        try {
            if(request.nature.isPython()){
                if(!PydevPlugin.isPythonInterpreterInitialized()){
                    PydevPlugin.logInfo("Python interpreter manager not initialized.");
                    return null;
                }
            }else if(request.nature.isJython()){
                if(!PydevPlugin.isJythonInterpreterInitialized()){
                    PydevPlugin.logInfo("Jython interpreter manager not initialized.");
                    return null;
                }
            }else{
                throw new RuntimeException("Project is neither python nor jython?");
            }
        } catch (CoreException e1) {
            throw new RuntimeException(e1);
        }
        //end check if it is already initialized....
        
        IModule mod = request.getModule();
        if(mod == null){
            PydevPlugin.logInfo("Unable to resolve module for find definition request.");
            return null;
        }

        if(modName == null){
            if(mod.getName() == null){
                if(mod instanceof SourceModule){
                    SourceModule m = (SourceModule) mod;
                    modName = "__module_not_in_the_pythonpath__";
                    m.setName(modName);
                }
            }
            if(modName == null){
                PydevPlugin.logInfo("Unable to resolve module for find definition request (modName == null).");
                return null;
            }
        }
        request.moduleName = modName;
        return mod;
    }
    

    /**
     * This method will try to find the actual definition given all the needed parameters (but it will not try to find
     * matches in the whole workspace if it's not able to find an exact match in the context)
     * 
     * Note that the request must be already properly configured to be used in this function. Otherwise, the
     * function that should be used is {@link #findActualDefinition(RefactoringRequest, CompletionCache, ArrayList)}
     * 
     * 
     * @param request: used only to communicateWork and checkCancelled
     * @param mod this is the module where we should find the definition
     * @param tok the token we're looking for (complete with dots)
     * @param selected OUT: this is where the definitions should be added
     * @param beginLine starts at 1
     * @param beginCol starts at 1
     * @param pythonNature the nature that we should use to find the definition
     * @param completionCache cache to store completions
     * 
     * @throws Exception
     */
    public static void findActualDefinition(RefactoringRequest request, IModule mod, String tok, ArrayList<IDefinition> selected, 
            int beginLine, int beginCol, IPythonNature pythonNature, ICompletionCache completionCache) throws Exception {
        
        IDefinition[] definitions = mod.findDefinition(CompletionStateFactory.getEmptyCompletionState(tok, pythonNature, 
                beginLine-1, beginCol-1, completionCache), beginLine, beginCol, pythonNature);
        
        request.communicateWork("Found:"+definitions.length+ " definitions");
        for (IDefinition definition : definitions) {
            boolean doAdd = true;
            if(definition instanceof Definition){
                Definition d = (Definition) definition;
                doAdd = !findActualTokenFromImportFromDefinition(pythonNature, tok, selected, d, completionCache);
            }
            request.checkCancelled();
            if(doAdd){
                selected.add(definition);
            }
        }
    }
    
    
    /** 
     * Given some definition, find its actual token (if that's possible)
     * @param request the original request
     * @param tok the token we're looking for
     * @param lFindInfo place to store info
     * @param selected place to add the new definition (if found)
     * @param d the definition found before (this function will only work if this definition
     * maps to an ImportFrom)
     *  
     * @return true if we found a new definition (and false otherwise)
     * @throws Exception
     */
    private static boolean findActualTokenFromImportFromDefinition(IPythonNature nature, String tok, ArrayList<IDefinition> selected, 
            Definition d, ICompletionCache completionCache) throws Exception {
        boolean didFindNewDef = false;
        
        Set<Tuple3<String, Integer, Integer>> whereWePassed = new HashSet<Tuple3<String, Integer, Integer>>();
        
        tok = FullRepIterable.getLastPart(tok); //in an import..from, the last part will always be the token imported 
        
        while(d.ast instanceof ImportFrom){
            Tuple3<String, Integer, Integer> t1 = getTupFromDefinition(d);
            if(t1 == null){
                break;
            }
            whereWePassed.add(t1);
            
            Definition[] found = (Definition[]) d.module.findDefinition(CompletionStateFactory.getEmptyCompletionState(tok, nature, completionCache), d.line, d.col, nature);
            if(found != null && found.length == 1){
                Tuple3<String,Integer,Integer> tupFromDefinition = getTupFromDefinition(found[0]);
                if(tupFromDefinition == null){
                    break;
                }
                if(!whereWePassed.contains(tupFromDefinition)){ //avoid recursions
                    didFindNewDef = true;
                    d = found[0];
                }else{
                    break;
                }
            }else{
                break;
            }
        }
        
        if(didFindNewDef){
            selected.add(d);
        }
        
        return didFindNewDef;
    }
    
    
    /**
     * @return a tuple with the absolute path to the definition, its line and col.
     */
    private static Tuple3<String, Integer, Integer> getTupFromDefinition(Definition d) {
        if(d == null){
            return null;
        }
        File file = d.module.getFile();
        if(file == null){
            return null;
        }
        return new Tuple3<String, Integer, Integer>(REF.getFileAbsolutePath(file), d.line, d.col);
    }


    /**
     * @param pointers: OUT: list where the pointers will be added
     * @param definitions the definitions that will be gotten as pointers
     */
    public static void getAsPointers(List<ItemPointer> pointers, IDefinition[] definitions) {
        for (IDefinition definition : definitions) {
            File file = definition.getModule().getFile();
            int line = definition.getLine();
            int col = definition.getCol();
            
            pointers.add(new ItemPointer(file,
                    new Location(line-1, col-1),
                    new Location(line-1, col-1), 
                    (Definition)definition,
                    definition.getModule().getZipFilePath())
                    );
        }
    }
}
