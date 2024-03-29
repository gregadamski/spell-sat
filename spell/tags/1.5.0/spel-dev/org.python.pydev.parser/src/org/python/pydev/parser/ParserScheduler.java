/*
 * License: Common Public License v1.0
 * Created on Sep 14, 2005
 * 
 * @author Fabio Zadrozny
 */
package org.python.pydev.parser;

public class ParserScheduler {

    /**
     * used to do parsings in a thread - is null when not doing parsings
     */
    public volatile ParsingThread parsingThread; 
    
    /**
     * indicates that currently nothing is happening
     */
    public static final int STATE_WAITING = 0; 
    
    /**
     * indicates whether some parse later has been requested
     */
    public static final int STATE_PARSE_LATER = 1; 

    /**
     * indicates if a thread is currently waiting for an elapse cycle to end
     */
    public static final int STATE_WAITING_FOR_ELAPSE = 2;
    
    /**
     * indicates if a thread is currently doing a parse action
     */
    public static final int STATE_DOING_PARSE = 3;
    
    /**
     * initially we're waiting
     */
    int state = STATE_WAITING;
    
    /**
     * this is the exact time a parse later was requested
     */
    private long timeParseLaterRequested = 0;
    
    /**
     * this is the exact time the last parse was requested
     */
    private long timeLastParse = 0;

    private PyParser parser;

    public ParserScheduler(PyParser parser) {
        super();
        this.parser = parser;
    }


    public void parseNow() {
        parseNow(false);
    }
    
    /**
     * The arguments passed in argsToReparse will be passed to the reparseDocument, and then on to fireParserChanged / fireParserError
     */
    public void parseNow(boolean force, Object ... argsToReparse) {
        if(!force){
            if(state != STATE_WAITING_FOR_ELAPSE && state != STATE_DOING_PARSE){
                //waiting or parse later
                state = STATE_WAITING_FOR_ELAPSE; // the parser will reset it later
                timeLastParse = System.currentTimeMillis();
                checkCreateAndStartParsingThread();
            }else{
                //another request... we keep waiting until the user stops adding requests
                boolean created = checkCreateAndStartParsingThread();
                if(!created){
                    parsingThread.okToGo = false;
                }
            }
        }else{
            ParsingThread parserThreadLocal = parsingThread; //if it dies suddenly, we don't want to get it as null...
            if(state == ParserScheduler.STATE_DOING_PARSE){
                //a parse is already in action
            }else{
                if(parserThreadLocal == null){
                    parserThreadLocal = new ParsingThread(this, argsToReparse);
                    parsingThread = parserThreadLocal;
                    parserThreadLocal.force = true;
                    parserThreadLocal.setPriority(Thread.MIN_PRIORITY); //parsing is low priority
                    parserThreadLocal.start();
                }else{
                    //force it to run
                    parserThreadLocal.force = true;
                    parserThreadLocal.interrupt();
                }
            }
        }
    }


    /**
     * @return whether we really created the thread (returns false if the thread already exists)
     */
    private boolean checkCreateAndStartParsingThread() {
        ParsingThread p = parsingThread;
        if(p == null){
            p = new ParsingThread(this);
            p.setPriority(Thread.MIN_PRIORITY); //parsing is low priority
            p.start();
            parsingThread = p;
            return true;
        }
        return false;
    }
    
    public void parseLater() {
        if(state != STATE_WAITING_FOR_ELAPSE && state != STATE_PARSE_LATER){
            state = STATE_PARSE_LATER;
            //ok, the time for this request is:
            timeParseLaterRequested = System.currentTimeMillis();
            Thread thread = new Thread(){
                @Override
                public void run() {
                    try {
                        sleep(PyParserManager.getPyParserManager(null).getElapseMillisBeforeAnalysis());
                    } catch (Exception e) {
                        //that's ok
                    }
                    //ok, no parse happened while we were sleeping
                    if( state == STATE_PARSE_LATER && timeLastParse < timeParseLaterRequested){
                        parseNow();
                    }
                }
            };
            thread.setName("ParserScheduler");
            thread.start();
        }
        
    }


    /**
     * this should call back to the parser itself for doing a parse
     * 
     * The argsToReparse will be passed to the IParserObserver2
     */
    public void reparseDocument(Object ... argsToReparse) {
        PyParser p = parser;
        if(p != null){
            p.reparseDocument(argsToReparse);
        }
    }


    public void dispose() {
        ParsingThread p = this.parsingThread;
        if(p != null){
            p.dispose();
        }
        this.parser = null;
    }


}
