package org.python.pydev.editor.codecompletion.revisited;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.python.pydev.core.ExtensionHelper;
import org.python.pydev.core.FullRepIterable;
import org.python.pydev.core.ICodeCompletionASTManager;
import org.python.pydev.core.ICompletionRequest;
import org.python.pydev.core.ICompletionState;
import org.python.pydev.core.ILocalScope;
import org.python.pydev.core.IModule;
import org.python.pydev.core.IModulesManager;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.IToken;
import org.python.pydev.core.ModulesKey;
import org.python.pydev.core.REF;
import org.python.pydev.core.Tuple;
import org.python.pydev.core.Tuple3;
import org.python.pydev.core.TupleN;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.core.log.Log;
import org.python.pydev.core.structure.CompletionRecursionException;
import org.python.pydev.editor.actions.PyAction;
import org.python.pydev.editor.codecompletion.CompletionRequest;
import org.python.pydev.editor.codecompletion.IPyDevCompletionParticipant;
import org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule;
import org.python.pydev.editor.codecompletion.revisited.modules.SourceModule;
import org.python.pydev.editor.codecompletion.revisited.modules.SourceToken;
import org.python.pydev.editor.codecompletion.revisited.visitors.GlobalModelVisitor;
import org.python.pydev.logging.DebugSettings;
import org.python.pydev.parser.PyParser;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.visitors.NodeUtils;

public abstract class AbstractASTManager implements ICodeCompletionASTManager, Serializable {

    /**
     * changed to 10L on release 1.3.19
     */
    protected static final long serialVersionUID = 10L;
    
    private static final IToken[] EMPTY_ITOKEN_ARRAY = new IToken[0];
    
    private static final boolean DEBUG_CACHE = false;
    
    private transient AssignAnalysis assignAnalysis;
    
    public AbstractASTManager(){
    }
    
    public AssignAnalysis getAssignAnalysis() {
        if(assignAnalysis == null){
            assignAnalysis = new AssignAnalysis();
        }
        return assignAnalysis;
    }
    
    
    /**
     * This is the guy that will handle project things for us
     */
    public volatile IModulesManager modulesManager;
    public IModulesManager getModulesManager(){
        return modulesManager;
    }

    


    /**
     * Set the nature this ast manager works with (if no project is available and a nature is).
     */
    public void setNature(IPythonNature nature){
        getModulesManager().setPythonNature(nature);
    }
    
    public IPythonNature getNature() {
        return getModulesManager().getNature();
    }
    
    
    public abstract void setProject(IProject project, IPythonNature nature, boolean restoreDeltas) ;

    public abstract void rebuildModule(File file, IDocument doc, IProject project, IProgressMonitor monitor, IPythonNature nature) ;

    public abstract void removeModule(File file, IProject project, IProgressMonitor monitor) ;
    

    /**
     * Returns the imports that start with a given string. The comparisson is not case dependent. Passes all the modules in the cache.
     * 
     * @param original is the name of the import module eg. 'from toimport import ' would mean that the original is 'toimport'
     * or something like 'foo.bar' or an empty string (if only 'import').
     * @return a Set with the imports as tuples with the name, the docstring.
     * @throws CompletionRecursionException 
     */
    public IToken[] getCompletionsForImport(ImportInfo importInfo, ICompletionRequest r) throws CompletionRecursionException {
        String original = importInfo.importsTipperStr;
        String afterDots = null;
        int level = 0; //meaning: no absolute import
        
        boolean onlyDots = true;
        if(original.startsWith(".")){
            //if the import has leading dots, this means it is something like
            //from ...bar import xxx (new way to express the relative import)
            for(int i = 0; i< original.length(); i++){
                if(original.charAt(i) != '.'){
                    onlyDots = false;
                    afterDots = original.substring(i);
                    break;
                }
                //add one to the relative import level
                level++;
            }
        }
        CompletionRequest request = (CompletionRequest) r;
        IPythonNature nature = request.nature;
        
        String relative = null;
        String moduleName = null;
        if(request.editorFile != null){
            moduleName = nature.getAstManager().getModulesManager().resolveModule(REF.getFileAbsolutePath(request.editorFile));
            if(moduleName != null){
                
                if(level > 0){
                    //ok, it is the import added on python 2.5 (from .. import xxx)
                    String[] moduleParts = StringUtils.dotSplit(moduleName);
                    if(moduleParts.length > level){
                        relative = FullRepIterable.joinParts(moduleParts, moduleParts.length-level);
                    }
                    
                    if(!onlyDots){
                        //ok, we have to add the other part too, as we have more than the leading dots
                        //from ..bar import 
                        relative += "."+afterDots;
                    }
                    
                }else{
                    String tail = FullRepIterable.headAndTail(moduleName)[0];
                    if(original.length() > 0){
                        relative = tail+"."+original;
                    }else{
                        relative = tail;
                    }
                }
            }
        }
        
        //set to hold the completion (no duplicates allowed).
        Set<IToken> set = new HashSet<IToken>();

        String absoluteModule = original;
        if (absoluteModule.endsWith(".")) {
            absoluteModule = absoluteModule.substring(0, absoluteModule.length() - 1);
        }
        
        if(level == 0){
            //first we get the imports... that complete for the token.
            getAbsoluteImportTokens(absoluteModule, set, IToken.TYPE_IMPORT, false);
    
            //Now, if we have an initial module, we have to get the completions
            //for it.
            getTokensForModule(original, nature, absoluteModule, set);
        }

        if(relative != null && relative.equals(absoluteModule) == false){
            getAbsoluteImportTokens(relative, set, IToken.TYPE_RELATIVE_IMPORT, false);
            if(importInfo.hasImportSubstring){
                getTokensForModule(relative, nature, relative, set);
            }
        }
        
        if(level == 1 && moduleName != null){
            //has returned itself, so, let's remove it
            String strToRemove = FullRepIterable.getLastPart(moduleName);
            for(Iterator<IToken> it=set.iterator();it.hasNext();){
                IToken o = it.next();
                if(o.getRepresentation().equals(strToRemove)){
                    it.remove();
                    //don't break because the token might be different, but not the representation...
                }
            }
        }
        return set.toArray(EMPTY_ITOKEN_ARRAY);
    }

    
    
    /**
     * @param moduleToGetTokensFrom the string that represents the token from where we are getting the imports
     * @param set the set where the tokens should be added
     */
    protected void getAbsoluteImportTokens(String moduleToGetTokensFrom, Set<IToken> set, int type, boolean onlyFilesOnSameLevel) {
        SortedMap<ModulesKey,ModulesKey> modulesStartingWith = modulesManager.getAllModulesStartingWith(moduleToGetTokensFrom);
        Iterator<ModulesKey> itModules = modulesStartingWith.keySet().iterator();
        while(itModules.hasNext()){
            ModulesKey key = itModules.next();
            
            String element = key.name;
//            if (element.startsWith(moduleToGetTokensFrom)) { we don't check that anymore because we get all the modules starting with it already
                if(onlyFilesOnSameLevel && key.file != null && key.file.isDirectory()){
                    continue; // we only want those that are in the same directory, and not in other directories...
                }
                element = element.substring(moduleToGetTokensFrom.length());
                
                //we just want those that are direct
                //this means that if we had initially element = testlib.unittest.anothertest
                //and element became later = .unittest.anothertest, it will be ignored (we
                //should only analyze it if it was something as testlib.unittest and became .unittest
                //we only check this if we only want file modules (in
                if(onlyFilesOnSameLevel && PyAction.countChars('.', element) > 1){
                    continue;
                }

                boolean goForIt = false;
                //if initial is not empty only get those that start with a dot (submodules, not
                //modules that start with the same name).
                //e.g. we want xml.dom
                //and not xmlrpclib
                //if we have xml token (not using the qualifier here) 
                if (moduleToGetTokensFrom.length() != 0) {
                    if (element.length() > 0 && element.charAt(0) == ('.')) {
                        element = element.substring(1);
                        goForIt = true;
                    }
                } else {
                    goForIt = true;
                }

                if (element.length() > 0 && goForIt) {
                    String[] splitted = StringUtils.dotSplit(element);
                    if (splitted.length > 0) {
                        //this is the completion
                        set.add(new ConcreteToken(splitted[0], "", "", moduleToGetTokensFrom, type));
                    }
                }
//            }
        }
    }

    /**
     * @param original this is the initial module where the completion should happen (may have class in it too)
     * @param moduleToGetTokensFrom
     * @param set set where the tokens should be added
     * @throws CompletionRecursionException 
     */
    protected void getTokensForModule(String original, IPythonNature nature, String moduleToGetTokensFrom, Set<IToken> set) throws CompletionRecursionException {
        if (moduleToGetTokensFrom.length() > 0) {
            if (original.endsWith(".")) {
                original = original.substring(0, original.length() - 1);
            }

            Tuple<IModule, String> modTok = findModuleFromPath(original, nature, false, null); //the current module name is not used as it is not relative
            IModule m = modTok.o1;
            String tok = modTok.o2;
            
            if(m == null){
                //we were unable to find it with the given path, so, there's nothing else to do here...
                return;
            }
            
            IToken[] globalTokens;
            if(tok != null && tok.length() > 0){
                CompletionState state2 = new CompletionState(-1,-1,tok,nature,"");
                state2.builtinsGotten = true; //we don't want to get builtins here
                globalTokens = m.getGlobalTokens(state2, this);
            }else{
                CompletionState state2 = new CompletionState(-1,-1,"",nature,"");
                state2.builtinsGotten = true; //we don't want to get builtins here
                globalTokens = getCompletionsForModule(m, state2);
            }
            
            for (int i = 0; i < globalTokens.length; i++) {
                IToken element = globalTokens[i];
                //this is the completion
                set.add(element);
            }
        }
    }



    /**
     * @param file
     * @param doc
     * @param state
     * @return
     */
    public static IModule createModule(File file, IDocument doc, ICompletionState state, ICodeCompletionASTManager manager) {
        IPythonNature pythonNature = state.getNature();
        int line = state.getLine();
        IModulesManager projModulesManager = manager.getModulesManager();

        return AbstractModule.createModuleFromDoc(file, doc, pythonNature, line, projModulesManager);
    }

    /** 
     * @see org.python.pydev.core.ICodeCompletionASTManager#getCompletionsForToken(java.io.File, org.eclipse.jface.text.IDocument, org.python.pydev.editor.codecompletion.revisited.CompletionState)
     */
    public IToken[] getCompletionsForToken(File file, IDocument doc, ICompletionState state) throws CompletionRecursionException {
        IModule module = createModule(file, doc, state, this);
        return getCompletionsForModule(module, state, true, true);
    }

    /** 
     * @see org.python.pydev.editor.codecompletion.revisited.ICodeCompletionASTManage#getCompletionsForToken(org.eclipse.jface.text.IDocument, org.python.pydev.editor.codecompletion.revisited.CompletionState)
     */
    public IToken[] getCompletionsForToken(IDocument doc, ICompletionState state) {
        IToken[] completionsForModule;
        try {
            Tuple<SimpleNode, Throwable> obj = PyParser.reparseDocument(new PyParser.ParserInfo(doc, true, state.getNature(), state.getLine()));
            SimpleNode n = obj.o1;
            IModule module = AbstractModule.createModule(n);
        
            completionsForModule = getCompletionsForModule(module, state, true, true);

        } catch (CompletionRecursionException e) {
            completionsForModule = new IToken[]{ new ConcreteToken(e.getMessage(), e.getMessage(), "","", IToken.TYPE_UNKNOWN)};
        }
        
        return completionsForModule;
    }

    
    
    /**
     * By default does not look for relative import
     */
    public IModule getModule(String name, IPythonNature nature, boolean dontSearchInit) {
        return modulesManager.getModule(name, nature, dontSearchInit, false);
    }

    /**
     * This method returns the module that corresponds to the path passed as a parameter.
     * 
     * @param name the name of the module we're looking for
     * @param lookingForRelative determines whether we're looking for a relative module (in which case we should
     * not check in other places... only in the module)
     * @return the module represented by this name
     */
    public IModule getModule(String name, IPythonNature nature, boolean dontSearchInit, boolean lookingForRelative) {
        if(lookingForRelative){
            return modulesManager.getRelativeModule(name, nature);
        }else{
            return modulesManager.getModule(name, nature, dontSearchInit);
        }
    }

    /**
     * Identifies the token passed and if it maps to a builtin not 'easily recognizable', as
     * a string or list, we return it.
     * 
     * @param state
     * @return
     */
    protected IToken[] getBuiltinsCompletions(ICompletionState state){
        ICompletionState state2 = state.getCopy();

        String act = state.getActivationToken();
        
        //check for the builtin types.
        state2.setActivationToken (NodeUtils.getBuiltinType(act));

        if(state2.getActivationToken() != null){
            IModule m = getBuiltinMod(state.getNature());
            return m.getGlobalTokens(state2, this);
        }
        
        if(act.equals("__builtins__") || act.startsWith("__builtins__.")){
            act = act.substring(12);
            if(act.startsWith(".")){
                act = act.substring(1);
            }
            IModule m = getBuiltinMod(state.getNature());
            ICompletionState state3 = state.getCopy();
            state3.setActivationToken(act);
            return m.getGlobalTokens(state3, this);
        }
        return null;
    }

    /** 
     * @throws CompletionRecursionException 
     * @see org.python.pydev.editor.codecompletion.revisited.ICodeCompletionASTManage#getCompletionsForModule(org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule, org.python.pydev.editor.codecompletion.revisited.CompletionState)
     */
    public IToken[] getCompletionsForModule(IModule module, ICompletionState state) throws CompletionRecursionException {
        return getCompletionsForModule(module, state, true);
    }
    
    /** 
     * @throws CompletionRecursionException 
     * @see org.python.pydev.editor.codecompletion.revisited.ICodeCompletionASTManage#getCompletionsForModule(org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule, org.python.pydev.editor.codecompletion.revisited.CompletionState, boolean)
     */
    public IToken[] getCompletionsForModule(IModule module, ICompletionState state, boolean searchSameLevelMods) throws CompletionRecursionException {
        return getCompletionsForModule(module, state, true, false);
    }
    
    /** 
     * @see org.python.pydev.editor.codecompletion.revisited.ICodeCompletionASTManage#getCompletionsForModule(org.python.pydev.editor.codecompletion.revisited.modules.AbstractModule, org.python.pydev.editor.codecompletion.revisited.CompletionState, boolean, boolean)
     */
    @SuppressWarnings("unchecked")
    public IToken[] getCompletionsForModule(IModule module, ICompletionState state, boolean searchSameLevelMods, 
            boolean lookForArgumentCompletion) throws CompletionRecursionException{
        return getCompletionsForModule(module, state, searchSameLevelMods, lookForArgumentCompletion, false);
    }
        
    /** 
     * @see #getCompletionsForModule(IModule, ICompletionState, boolean, boolean)
     * 
     * Same thing but may handle things as if it was a wild import (in which case, the tokens starting with '_' are
     * removed and if __all__ is available, only the tokens contained in __all__ are returned)
     */
    @SuppressWarnings("unchecked")
    public IToken[] getCompletionsForModule(IModule module, ICompletionState state, boolean searchSameLevelMods, 
            boolean lookForArgumentCompletion, boolean handleAsWildImport) throws CompletionRecursionException{
        String name = module.getName();
        Object key = new TupleN("getCompletionsForModule", name!=null?name:"", state.getActivationToken(), searchSameLevelMods, 
                lookForArgumentCompletion, state.getBuiltinsGotten(), state.getLocalImportsGotten(), handleAsWildImport);
        
        IToken[] ret = (IToken[]) state.getObj(key);
        if(ret != null){
            if(DEBUG_CACHE){
                System.out.println("Checking if cache is correct for: "+key);
                IToken[] internal = internalGenerateGetCompletionsForModule(module, state, searchSameLevelMods, lookForArgumentCompletion);
                internal = filterForWildImport(module, handleAsWildImport, internal);
                //the new request may actually have no tokens if a completion exception occurred.
                if(internal.length != 0 && ret.length != internal.length){
                    throw new RuntimeException("This can't happen... it should always return the same completions!");
                }
            }
            return ret;
        }
        
        IToken[] completionsForModule = internalGenerateGetCompletionsForModule(module, state, searchSameLevelMods, lookForArgumentCompletion);
        completionsForModule = filterForWildImport(module, handleAsWildImport, completionsForModule);
        
        state.add(key, completionsForModule);
        return completionsForModule;
    }
    
    
    /**
     * Filters the tokens according to the wild import rules:
     * - the tokens starting with '_' are removed 
     * - if __all__ is available, only the tokens contained in __all__ are returned)
     */
    private IToken[] filterForWildImport(IModule module, boolean handleAsWildImport, IToken[] completionsForModule){
        if(module != null && handleAsWildImport){
            ArrayList<IToken> ret = new ArrayList<IToken>();
            
            for (int j = 0; j < completionsForModule.length; j++) {
                IToken token = completionsForModule[j];
                //on wild imports we don't get names that start with '_'
                if(!token.getRepresentation().startsWith("_")){
                    ret.add(token);
                }
            }
            
            if(module instanceof SourceModule){
                //Support for __all__: filter things if __all__ is available.
                SourceModule sourceModule = (SourceModule) module;
                GlobalModelVisitor globalModelVisitorCache = sourceModule.getGlobalModelVisitorCache();
                if(globalModelVisitorCache != null){
                    globalModelVisitorCache.filterAll(ret);
                }
            }
            return ret.toArray(new IToken[ret.size()]);
        }else{
            return completionsForModule;
        }
    }

    /**
     * This method should only be accessed from the public getCompletionsForModule (which caches the result).
     */
    private IToken[] internalGenerateGetCompletionsForModule(IModule module, ICompletionState state, 
            boolean searchSameLevelMods, boolean lookForArgumentCompletion) throws CompletionRecursionException{
        
        if(DebugSettings.DEBUG_CODE_COMPLETION){
            Log.toLogFile(this, "getCompletionsForModule");
        }
        ArrayList<IToken> importedModules = new ArrayList<IToken>();
        
        ILocalScope localScope = null;
        int line = state.getLine();
        int col = state.getCol();
        
        if(state.getLocalImportsGotten() == false){
            //in the first analyzed module, we have to get the local imports too. 
            state.setLocalImportsGotten (true);
            if(module != null && line >= 0){
                localScope = module.getLocalScope(line, col);
                if(localScope != null){
                    importedModules.addAll(localScope.getLocalImportedModules(line+1, col+1, module.getName()));
                }
            }
        }

        IToken[] builtinsCompletions = getBuiltinsCompletions(state);
        if(builtinsCompletions != null){
            return builtinsCompletions;
        }
        
        String act = state.getActivationToken();
        int parI = act.indexOf('(');
        if(parI != -1){
            act = act.substring(0, parI);
            state.setActivationToken(act);
            state.setLookingFor(ICompletionState.LOOKING_FOR_INSTANCED_VARIABLE);
        }
            
        if (module != null) {

            //get the tokens (global, imported and wild imported)
            IToken[] globalTokens = module.getGlobalTokens();
            
            List<IToken> tokenImportedModules = Arrays.asList(module.getTokenImportedModules());
            importedModules.addAll(tokenImportedModules);
            state.setTokenImportedModules(importedModules);
            IToken[] wildImportedModules = module.getWildImportedModules();
            
            
            //now, lets check if this is actually a module that is an __init__ (if so, we have to get all
            //the other .py files as modules that are in the same level as the __init__)
            Set<IToken> initial = new HashSet<IToken>();
            if(searchSameLevelMods){
                //now, we have to ask for the module if it's a 'package' (folders that have __init__.py for python
                //or only folders -- not classes -- in java).
                if(module.isPackage()){
                    HashSet<IToken> gotten = new HashSet<IToken>();
                    //the module also decides how to get its submodules
                    getAbsoluteImportTokens(module.getPackageFolderName(), gotten, IToken.TYPE_IMPORT, true);
                    for (IToken token : gotten) {
                        if(token.getRepresentation().equals("__init__") == false){
                            initial.add(token);
                        }
                    }
                }
            }

            if (state.getActivationToken().length() == 0) {

                List<IToken> completions = getGlobalCompletions(globalTokens, 
                        importedModules.toArray(EMPTY_ITOKEN_ARRAY), wildImportedModules, state, module);
                
                //now find the locals for the module
                if (line >= 0){
                    IToken[] localTokens = module.getLocalTokens(line, col, localScope);
                    for (int i = 0; i < localTokens.length; i++) {
                        completions.add(localTokens[i]); 
                    }
                }
                completions.addAll(initial); //just add all that are in the same level if it was an __init__

                return completions.toArray(EMPTY_ITOKEN_ARRAY);
                
            }else{ //ok, we have a token, find it and get its completions.
                
                //first check if the token is a module... if it is, get the completions for that module.
                IToken[] tokens = findTokensOnImportedMods(importedModules.toArray(EMPTY_ITOKEN_ARRAY), state, module);
                if(tokens != null && tokens.length > 0){
                    return tokens;
                }
                
                //if it is an __init__, modules on the same level are treated as local tokens
                if(searchSameLevelMods){
                    tokens = searchOnSameLevelMods(initial, state);
                    if(tokens != null && tokens.length > 0){
                        return tokens;
                    }
                }

                //for wild imports, we must get the global completions with __all__ filtered
                //wild imports: recursively go and get those completions and see if any matches it.
                for (int i = 0; i < wildImportedModules.length; i++) {

                    IToken name = wildImportedModules[i];
                    IModule mod = getModule(name.getAsRelativeImport(module.getName()), state.getNature(), false); //relative (for wild imports this is ok... only a module can be used in wild imports)
                    
                    if (mod == null) {
                        mod = getModule(name.getOriginalRep(), state.getNature(), false); //absolute
                    }
                    
                    
                    if (mod != null) {
                        state.checkFindModuleCompletionsMemory(mod, state.getActivationToken());
                        IToken[] completionsForModule = getCompletionsForModule(mod, state);
                        if(completionsForModule.length > 0)
                            return completionsForModule;
                    } else {
                        //"Module not found:" + name.getRepresentation()
                    }
                }

                //it was not a module (would have returned already), so, try to get the completions for a global token defined.
                tokens = module.getGlobalTokens(state, this);
                if (tokens.length > 0){
                    return tokens;
                }
                
                //If it was still not found, go to builtins.
                IModule builtinsMod = getBuiltinMod(state.getNature());
                if(builtinsMod != null && builtinsMod != module){
                    tokens = getCompletionsForModule( builtinsMod, state);
                    if (tokens.length > 0){
                        if (tokens[0].getRepresentation().equals("ERROR:") == false){
                            return tokens;
                        }
                    }
                }

                if(lookForArgumentCompletion && localScope != null){
                    
                    //now, if we have to look for arguments and search things in the local scope, let's also
                    //check for assert (isinstance...) in this scope with the given variable.
                    {
                        List<String> lookForClass = localScope.getPossibleClassesForActivationToken(state.getActivationToken());
                        if(lookForClass.size() > 0){
                            HashSet<IToken> hashSet = new HashSet<IToken>();
                            
                            getCompletionsForClassInLocalScope(module, state, searchSameLevelMods, 
                                    lookForArgumentCompletion, lookForClass, hashSet);
                            
                            if(hashSet.size() > 0){
                                return hashSet.toArray(EMPTY_ITOKEN_ARRAY);
                            }
                        }
                    }
                    
                    
                    //ok, didn't find in assert isinstance... keep going
                    //if there was no assert for the class, get from extensions / local scope interface
                    tokens = getArgsCompletion(state, localScope);
                    if(tokens != null && tokens.length > 0){
                        return tokens;
                    }
                }
                
                //nothing worked so far, so, let's look for an assignment...
                return getAssignAnalysis().getAssignCompletions(this, module, state).toArray(EMPTY_ITOKEN_ARRAY);
            }

            
        }else{
            System.err.println("Module passed in is null!!");
        }
        
        return EMPTY_ITOKEN_ARRAY;
    }

    /**
     * @see ICodeCompletionASTManager#getCompletionsForClassInLocalScope(IModule, ICompletionState, boolean, boolean, List, HashSet)
     */
    public void getCompletionsForClassInLocalScope(IModule module, ICompletionState state, boolean searchSameLevelMods,
            boolean lookForArgumentCompletion, List<String> lookForClass, HashSet<IToken> hashSet) throws CompletionRecursionException {
        IToken[] tokens;
        //if found here, it's an instanced variable (force it and restore if we didn't find it here...)
        ICompletionState stateCopy = state.getCopy();
        int prevLookingFor = stateCopy.getLookingFor();
        //force looking for instance
        stateCopy.setLookingFor(ICompletionState.LOOKING_FOR_INSTANCED_VARIABLE, true);
        
        for(String classFound: lookForClass){
            stateCopy.setLocalImportsGotten(false);
            stateCopy.setActivationToken(classFound);
            
            //same thing as the initial request, but with the class we could find...
            tokens = getCompletionsForModule(module, stateCopy, searchSameLevelMods, lookForArgumentCompletion);
            if(tokens != null){
                for(IToken tok:tokens){
                    hashSet.add(tok);
                }
            }
        }
        if(hashSet.size() == 0){
            //force looking for what was set before...
            stateCopy.setLookingFor(prevLookingFor, true);
        }
    }





    /**
     * Get the completions based on the arguments received
     * 
     * @param state this is the state used for the completion
     * @param localScope this is the scope we're currently on (may be null)
     */
    @SuppressWarnings("unchecked")
    private IToken[] getArgsCompletion(ICompletionState state, ILocalScope localScope) {
        IToken[] args = localScope.getLocalTokens(-1,-1,true); //only to get the args
        String activationToken = state.getActivationToken();
        String firstPart = FullRepIterable.getFirstPart(activationToken);
        for (IToken token : args) {
            if(token.getRepresentation().equals(firstPart)){
                Collection<IToken> interfaceForLocal = localScope.getInterfaceForLocal(state.getActivationToken());
                Collection argsCompletionFromParticipants = getArgsCompletionFromParticipants(state, localScope, interfaceForLocal);
                for (IToken t : interfaceForLocal) {
                    if(!t.getRepresentation().equals(state.getQualifier())){
                        argsCompletionFromParticipants.add(t);
                    }
                }
                return (IToken[]) argsCompletionFromParticipants.toArray(EMPTY_ITOKEN_ARRAY);
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private Collection getArgsCompletionFromParticipants(ICompletionState state, ILocalScope localScope, Collection<IToken> interfaceForLocal) {
        ArrayList ret = new ArrayList();
        
        List participants = ExtensionHelper.getParticipants(ExtensionHelper.PYDEV_COMPLETION);
        for (Iterator iter = participants.iterator(); iter.hasNext();) {
            IPyDevCompletionParticipant participant = (IPyDevCompletionParticipant) iter.next();
            ret.addAll(participant.getArgsCompletion(state, localScope, interfaceForLocal));
        }
        return ret;
    }


    /**
     * Attempt to search on modules on the same level as this one (this will only happen if we are in an __init__
     * module (otherwise, the initial set will be empty)
     * 
     * @param initial this is the set of tokens generated from modules in the same level
     * @param state the current state of the completion
     * 
     * @return a list of tokens found.
     * @throws CompletionRecursionException 
     */
    protected IToken[] searchOnSameLevelMods(Set<IToken> initial, ICompletionState state) throws CompletionRecursionException {
        IToken[] ret = null;
        Tuple<IModule, IModulesManager> modUsed = null;
        String actTokUsed = null;
        
        for (IToken token : initial) {
            //ok, maybe it was from the set that is in the same level as this one (this will only happen if we are on an __init__ module)
            String rep = token.getRepresentation();
            
            if(state.getActivationToken().startsWith(rep)){
                String absoluteImport = token.getAsAbsoluteImport();
                modUsed = modulesManager.getModuleAndRelatedModulesManager(absoluteImport, state.getNature(), true, false);
                
                IModule sameLevelMod = null; 
                if(modUsed != null){
                    sameLevelMod = modUsed.o1;
                }
                
                if(sameLevelMod == null){
                    return null;
                }
                
                String qualifier = state.getActivationToken().substring(rep.length());


                if(state.getActivationToken().equals(rep)){
                    actTokUsed = "";
                } else if(qualifier.startsWith(".")){
                    actTokUsed = qualifier.substring(1);
                }
                
                if(actTokUsed != null){
                    ICompletionState copy = state.getCopyWithActTok(actTokUsed);
                    copy.setBuiltinsGotten (true); //we don't want builtins... 
                    ret = getCompletionsForModule(sameLevelMod, copy);
                    break;
                }
            }
        }
        
        return ret;
    }

    /**
     * @see ICodeCompletionASTManager#getGlobalCompletions
     */
    public List<IToken> getGlobalCompletions(IToken[] globalTokens, IToken[] importedModules, IToken[] wildImportedModules, 
            ICompletionState state, IModule current) {
        if(DebugSettings.DEBUG_CODE_COMPLETION){
            Log.toLogFile(this, "getGlobalCompletions");
        }
        List<IToken> completions = new ArrayList<IToken>();

        //in completion with nothing, just go for what is imported and global tokens.
        for (int i = 0; i < globalTokens.length; i++) {
            completions.add(globalTokens[i]);
        }

        //now go for the token imports
        for (int i = 0; i < importedModules.length; i++) {
            completions.add(importedModules[i]);
        }

        if(!state.getBuiltinsGotten()){
            state.setBuiltinsGotten (true) ;
            if(DebugSettings.DEBUG_CODE_COMPLETION){
                Log.toLogFile(this, "getBuiltinCompletions");
            }
            //last thing: get completions from module __builtin__
            getBuiltinCompletions(state, completions);
            if(DebugSettings.DEBUG_CODE_COMPLETION){
                Log.toLogFile(this, "END getBuiltinCompletions");
            }
        }
        
        //wild imports: recursively go and get those completions. Must be done before getting the builtins, because 
        //when we do a wild import, we may get tokens that are filtered, and there's a chance that the builtins get 
        //filtered out if they are gotten from a wild import and not from the module itself.
        for (int i = 0; i < wildImportedModules.length; i++) {
        	
        	//for wild imports, we must get the global completions with __all__ filtered
        	IToken name = wildImportedModules[i];
        	getCompletionsForWildImport(state, current, completions, name);
        }
        return completions;
    }

    /**
     * @return the builtin completions
     */
    public List<IToken> getBuiltinCompletions(ICompletionState state, List<IToken> completions) {
        IPythonNature nature = state.getNature();
        IToken[] builtinCompletions = getBuiltinComps(nature);
        if(builtinCompletions != null){
            for (int i = 0; i < builtinCompletions.length; i++) {
                completions.add(builtinCompletions[i]);
            }
        }
        return completions;
        
    }


    /**
     * @return the tokens in the builtins
     */
    protected IToken[] getBuiltinComps(IPythonNature nature) {
        IToken[] builtinCompletions = nature.getBuiltinCompletions();
        
        if(builtinCompletions == null || builtinCompletions.length == 0){
            IModule builtMod = getBuiltinMod(nature);
            if(builtMod != null){
                builtinCompletions = builtMod.getGlobalTokens();
                nature.setBuiltinCompletions(builtinCompletions);
            }
        }
        return builtinCompletions;
    }

    /**
     * TODO: WHEN CLEARING CACHE, CLEAR THE BUILTIN REF TOO
     * @return the module that represents the builtins
     */
    protected IModule getBuiltinMod(IPythonNature nature) {
        IModule mod = nature.getBuiltinMod();
        if(mod == null){
            mod = getModule("__builtin__", nature, false);
            if(mod == null){
                //Python 3.0 has builtins and not __builtin__
                mod = getModule("builtins", nature, false);
            }
            nature.setBuiltinMod(mod);
        }
        return mod;
    }

    /**
     * Resolves a token defined with 'from module import something' statement 
     * to a proper type, as defined in module.  
     * @param imported the token to resolve.
     * @return the resolved token or the original token in case no additional information could be obtained.
     * @throws CompletionRecursionException 
     */
    public IToken resolveImport(ICompletionState state, IToken imported) throws CompletionRecursionException {
        String curModName = imported.getParentPackage();
        Tuple3<IModule, String, IToken> modTok = findOnImportedMods(new IToken[]{imported}, state.getCopyWithActTok(imported.getRepresentation()), curModName);
        if(modTok != null && modTok.o1 != null){

            if(modTok.o2.length() == 0){
                return imported; //it's a module actually, so, no problems...
                
            } else{
                try{
                    state.checkResolveImportMemory(modTok.o1, modTok.o2);
                }catch(CompletionRecursionException e){
                    return imported;
                }
                IToken repInModule = getRepInModule(modTok.o1, modTok.o2, state.getNature(), state);
                if(repInModule != null){
                    return repInModule;
                }
            }
        }
        return imported;
            
    }

    /**
     * This is the public interface
     * @throws CompletionRecursionException 
     * @see org.python.pydev.core.ICodeCompletionASTManager#getRepInModule(org.python.pydev.core.IModule, java.lang.String, org.python.pydev.core.IPythonNature)
     */
    public IToken getRepInModule(IModule module, String tokName, IPythonNature nature) throws CompletionRecursionException {
        return getRepInModule(module, tokName, nature, null);
    }
    
    /**
     * Get the actual token representing the tokName in the passed module  
     * @param module the module where we're looking
     * @param tokName the name of the token we're looking for
     * @param nature the nature we're looking for
     * @return the actual token in the module (or null if it was not possible to find it).
     * @throws CompletionRecursionException 
     */
    private IToken getRepInModule(IModule module, String tokName, IPythonNature nature, ICompletionState state) throws CompletionRecursionException {
        if(module != null){
            if(tokName.startsWith(".")){
                tokName = tokName.substring(1);
            }

            //ok, we are getting some token from the module... let's see if it is really available.
            String[] headAndTail = FullRepIterable.headAndTail(tokName);
            String actToken = headAndTail[0];  //tail (if os.path, it is os) 
            String hasToBeFound = headAndTail[1]; //head (it is path)
            
            //if it was os.path:
            //initial would be os.path
            //foundAs would be os
            //actToken would be path
            
            //now, what we will do is try to do a code completion in os and see if path is found
            if(state == null){
                state = CompletionStateFactory.getEmptyCompletionState(actToken, nature, new CompletionCache());
            }else{
                state = state.getCopy();
                state.setActivationToken(actToken);
            }
            IToken[] completionsForModule = getCompletionsForModule(module, state);
            for (IToken foundTok : completionsForModule) {
                if(foundTok.getRepresentation().equals(hasToBeFound)){
                    return foundTok;
                }
            }
        }
        return null;
    }


    /* (non-Javadoc)
     * @see ICodeCompletionASTManager#getCompletionsForWildImport(ICompletionState, IModule, List, IToken)
     */
    public boolean getCompletionsForWildImport(ICompletionState state, IModule current, List<IToken> completions, 
            IToken name) {
        try {
            //this one is an exception... even though we are getting the name as a relative import, we say it
            //is not because we want to get the module considering __init__
            IModule mod = null;
            
            if(current != null){
                //we cannot get the relative path if we don't have a current module
                mod = getModule(name.getAsRelativeImport(current.getName()), state.getNature(), false);
            }

            if (mod == null) {
                mod = getModule(name.getOriginalRep(), state.getNature(), false); //absolute import
            }

            if (mod != null) {
                state.checkWildImportInMemory(current, mod);
                IToken[] completionsForModule = getCompletionsForModule(mod, state, true, false, true);
                for (IToken token : completionsForModule) {
                    completions.add(token);
                }
                return true;
            } else {
                //"Module not found:" + name.getRepresentation()
            }
        } catch (CompletionRecursionException e) {
            //probably found a recursion... let's return the tokens we have so far
        }
        return false;
    }

    public IToken[] findTokensOnImportedMods( IToken[] importedModules, ICompletionState state, IModule current) throws CompletionRecursionException {
        Tuple3<IModule, String, IToken> o = findOnImportedMods(importedModules, state, current.getName());
        
        if(o == null)
            return null;
        
        IModule mod = o.o1;
        String tok = o.o2;
        
        if(tok.length() == 0){
            //the activation token corresponds to an imported module. We have to get its global tokens and return them.
            ICompletionState copy = state.getCopy();
            copy.setActivationToken("");
            copy.setBuiltinsGotten(true); //we don't want builtins... 
            return getCompletionsForModule(mod, copy);
        }else if (mod != null){
            ICompletionState copy = state.getCopy();
            copy.setActivationToken(tok);
            copy.setCol(-1);
            copy.setLine(-1);
            copy.raiseNFindTokensOnImportedModsCalled(mod, tok);
            
            String parentPackage = o.o3.getParentPackage();
            if(parentPackage.trim().length() > 0 && 
                parentPackage.equals(current.getName()) && 
                state.getActivationToken().equals(tok) && 
                !parentPackage.endsWith("__init__")){
                String name = mod.getName();
                if(name.endsWith(".__init__")){
                    name = name.substring(0, name.length()-9);
                }
                if(o.o3.getAsAbsoluteImport().startsWith(name)){
                    if(current.isInDirectGlobalTokens(tok, state)){
                        return null;
                    }
                }
            }

            return getCompletionsForModule(mod, copy);
        }
        return null;
    }

    /**
     * @param activationToken
     * @param importedModules
     * @param module
     * @return tuple with:
     * 0: mod
     * 1: tok
     * @throws CompletionRecursionException 
     */
    public Tuple3<IModule, String, IToken> findOnImportedMods( ICompletionState state, IModule current) throws CompletionRecursionException {
        IToken[] importedModules = current.getTokenImportedModules();
        return findOnImportedMods(importedModules, state, current.getName());
    }
    
    /**
     * This function tries to find some activation token defined in some imported module.  
     * @return tuple with: the module and the token that should be used from it.
     * 
     * @param this is the activation token we have. It may be a single token or some dotted name.
     * 
     * If it is a dotted name, such as testcase.TestCase, we need to match against some import
     * represented as testcase or testcase.TestCase.
     * 
     * If a testcase.TestCase matches against some import named testcase, the import is returned and
     * the TestCase is put as the module
     * 
     * 0: mod
     * 1: tok (string)
     * 2: actual tok
     * @throws CompletionRecursionException 
     */
    public Tuple3<IModule, String, IToken> findOnImportedMods( IToken[] importedModules, ICompletionState state, 
            String currentModuleName) throws CompletionRecursionException {
        
        FullRepIterable iterable = new FullRepIterable(state.getActivationToken(), true);
        for(String tok : iterable){
            for (IToken importedModule : importedModules) {
            
                final String modRep = importedModule.getRepresentation(); //this is its 'real' representation (alias) on the file (if it is from xxx import a as yyy, it is yyy)
                
                if(modRep.equals(tok)){
                    String act = state.getActivationToken();
                    Tuple<IModule, String> r = findOnImportedMods(importedModule, tok, state, act, currentModuleName);
                    if(r == null){
                        return null;
                    }
                    return new Tuple3<IModule, String, IToken>(r.o1, r.o2, importedModule);
                }
            }
        }   
        return null;
    }

    
    /**
     * Checks if some module can be resolved and returns the module it is resolved to (and to which token).
     * @throws CompletionRecursionException 
     * 
     */
    protected Tuple<IModule, String> findOnImportedMods(IToken importedModule, String tok, ICompletionState state, 
            String activationToken, String currentModuleName) throws CompletionRecursionException {
        
        
        Tuple<IModule, String> modTok = null;
        IModule mod = null;

        //ok, check if it is a token for the new import
        IPythonNature nature = state.getNature();
        if(importedModule instanceof SourceToken){
            SourceToken token = (SourceToken) importedModule;
            
            if(token.isImportFrom()){
                ImportFrom importFrom = (ImportFrom) token.getAst();
                int level = importFrom.level;
                if(level > 0){
                    //ok, it must be treated as a relative import
                    //ok, it is the import added on python 2.5 (from .. import xxx)
                    
                    String parentPackage = token.getParentPackage();
                    String[] moduleParts = StringUtils.dotSplit(parentPackage);
                    String relative = null;
                    if(moduleParts.length > level){
                        relative = FullRepIterable.joinParts(moduleParts, moduleParts.length-level);
                    }
                    
                    String modName = ((NameTok)importFrom.module).id;
                    if(modName.length() > 0){
                        //ok, we have to add the other part too, as we have more than the leading dots
                        //from ..bar import 
                        relative += "."+modName;
                    }
                    relative += "."+tok;
                    
                    modTok = findModuleFromPath(relative, nature, false, null);
                    mod = modTok.o1;
                    if(checkValidity(currentModuleName, mod)){
                        Tuple<IModule, String> ret = fixTok(modTok, tok, activationToken);
                        return ret;
                    }
                    //ok, it is 'forced' as relative import because it has a level, so, it MUST return here
                    return null;
                }
            }
        }
        
        
        
        //check as relative with complete rep
        String asRelativeImport = importedModule.getAsRelativeImport(currentModuleName);
        modTok = findModuleFromPath(asRelativeImport, nature, true, currentModuleName);
        mod = modTok.o1;
        if(checkValidity(currentModuleName, mod)){
            Tuple<IModule, String> ret = fixTok(modTok, tok, activationToken);
            return ret;
        }
        

        
        //check if the import actually represents some token in an __init__ file
        String originalWithoutRep = importedModule.getOriginalWithoutRep();
        if(originalWithoutRep.length() > 0){
            if(!originalWithoutRep.endsWith("__init__")){
                originalWithoutRep = originalWithoutRep + ".__init__";
            }
            modTok = findModuleFromPath(originalWithoutRep, nature, true, null);
            mod = modTok.o1;
            if(modTok.o2.endsWith("__init__") == false && checkValidity(currentModuleName, mod)){
                if(mod.isInGlobalTokens(importedModule.getRepresentation(), nature, false, state)){
                    //then this is the token we're looking for (otherwise, it might be a module).
                    Tuple<IModule, String> ret =  fixTok(modTok, tok, activationToken);
                    if(ret.o2.length() == 0){
                        ret.o2 = importedModule.getRepresentation();
                    }else{
                        ret.o2 = importedModule.getRepresentation()+"."+ret.o2;
                    }
                    return ret;
                }
            }
        }
        

        
        //the most 'simple' case: check as absolute with original rep
        modTok = findModuleFromPath(importedModule.getOriginalRep(), nature, false, null);
        mod = modTok.o1;
        if(checkValidity(currentModuleName, mod)){
            Tuple<IModule, String> ret =  fixTok(modTok, tok, activationToken);
            return ret;
        }
        
        
        
        

        
        //ok, one last shot, to see a relative looking in folders __init__
        modTok = findModuleFromPath(asRelativeImport, nature, false, null);
        mod = modTok.o1;
        if(checkValidity(currentModuleName, mod, true)){
            Tuple<IModule, String> ret = fixTok(modTok, tok, activationToken);
            //now let's see if what we did when we found it as a relative import is correct:
            
            //if we didn't find it in an __init__ module, all should be ok
            if(!mod.getName().endsWith("__init__")){
                return ret;
            }
            //otherwise, we have to be more cautious...
            //if the activation token is empty, then it is the module we were looking for
            //if it is not the initial token we were looking for, it is correct
            //if it is in the global tokens of the found module it is correct
            //if none of this situations was found, we probably just found the same token we had when we started (unless I'm mistaken...)
            else if(activationToken.length() == 0 || ret.o2.equals(activationToken) == false || 
                    mod.isInGlobalTokens(activationToken, nature, false, state)){
                return ret;
            }
        }
        
        return null;        
    }

    
    
    
    
    protected boolean checkValidity(String currentModuleName, IModule mod) {
    	return checkValidity(currentModuleName, mod, false);
    }
    
    
    /**
     * @param isRelative: On a relative import we have to check some more conditions...
     */
    protected boolean checkValidity(String currentModuleName, IModule mod, boolean isRelative) {
        if(mod == null){
            return false;
        }
        
        String modName = mod.getName();
        if(modName == null){
            return true;
        }
        
        //still in the same module
        if(modName.equals(currentModuleName)){
            return false;
        }
        
        if(isRelative && currentModuleName != null && modName.endsWith(".__init__")){
            //we have to check it without the __init__
            
            //what happens here is that considering the structure:
            //
            // xxx.__init__
            // xxx.mod1
            //
            // we cannot have tokens from the mod1 getting __init__
            
            String withoutLastPart = FullRepIterable.getWithoutLastPart(modName);
            String currentWithoutLastPart = FullRepIterable.getWithoutLastPart(currentModuleName);
            if(currentWithoutLastPart.equals(withoutLastPart)){
                return false;
            }
        }
        return true;
    }
            
            
    /**
     * Fixes the token if we found a module that was just a substring from the initial activation token.
     * 
     * This means that if we had testcase.TestCase and found it as TestCase, the token is added with TestCase
     */
    protected Tuple<IModule, String> fixTok(Tuple<IModule, String> modTok, String tok, String activationToken) {
        if(activationToken.length() > tok.length() && activationToken.startsWith(tok)){
            String toAdd = activationToken.substring(tok.length() + 1);
            if(modTok.o2.length() == 0){
                modTok.o2 = toAdd;
            }else{
                modTok.o2 += "."+toAdd;
            }
        }
        return modTok;
    }


    /**
     * This function receives a path (rep) and extracts a module from that path.
     * First it tries with the full path, and them removes a part of the final of
     * that path until it finds the module or the path is empty.
     * 
     * @param currentModuleName this is the module name (used to check validity for relative imports) -- not used if dontSearchInit is false
     * if this parameter is not null, it means we're looking for a relative import. When checking for relative imports, 
     * we should only check the modules that are directly under this project (so, we should not check the whole pythonpath for
     * it, just direct modules) 
     * 
     * @return tuple with found module and the String removed from the path in
     * order to find the module.
     */
    protected Tuple<IModule, String> findModuleFromPath(String rep, IPythonNature nature, boolean dontSearchInit, String currentModuleName){
        String tok = "";
        boolean lookingForRelative = currentModuleName != null;
        IModule mod = getModule(rep, nature, dontSearchInit, lookingForRelative);
        String mRep = rep;
        int index;
        while(mod == null && (index = mRep.lastIndexOf('.')) != -1){
            tok = mRep.substring(index+1) + "."+tok;
            mRep = mRep.substring(0,index);
            if(mRep.length() > 0){
                mod = getModule(mRep, nature, dontSearchInit, lookingForRelative);
            }
        }
        if (tok.endsWith(".")){
            tok = tok.substring(0, tok.length()-1); //remove last point if found.
        }
        
        if(dontSearchInit && currentModuleName != null && mod != null){
            String parentModule = FullRepIterable.getParentModule(currentModuleName);
            //if we are looking for some relative import token, it can only match if the name found is not less than the parent
            //of the current module because of the way in that relative imports are meant to be written.
            
            //if it equal, it should not match either, as it was found as the parent module... this can not happen because it must find
            //it with __init__ if it was the parent module
            if (mod.getName().length() <= parentModule.length()){
                return new Tuple<IModule, String>(null, null);
            }
        }
        return new Tuple<IModule, String>((AbstractModule)mod, tok);
    }
}
