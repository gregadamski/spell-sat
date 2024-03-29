package org.python.pydev.plugin.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

interface IPythonNatureStore {

    /**
     * Sets the project to be used.
     * 
     * @param project the project related to this store.
     * 
     * Note: the side-effect of this method is that the contents of the xml file are either created (if the file still does not exist) or just loaded.
     */
    public abstract void setProject(IProject project);

    /**
     * Retrieve a path property as a combined string with | character as path separator
     * 
     * @param key
     * @return the combined path string or null if the property is not set.
     * @throws CoreException
     */
    public abstract String getPathProperty(QualifiedName key) throws CoreException;

    /**
     * Set a path property. If the value is null the property is removed.
     * 
     * @param key the name of the property
     * @param value the combined string of paths with | character as separator
     * @throws CoreException
     */
    public abstract void setPathProperty(QualifiedName key, String value) throws CoreException;

    /**
     * Retrieve a string property with the specified key from the Xml representation. If the key is not in the Xml representation, the eclipse persistent property of the same key is read and migrated
     * into the Xml representation.
     * 
     * @param key
     * @return The value of the property or null if the property is not set.
     * @throws CoreException
     */
    public abstract String getPropertyFromXml(QualifiedName key);

    /**
     * Store a string property in the Xml representation and request the storage of the changes. If the value is is null, the property is removed.
     * 
     * @param key
     * @param value
     * @throws CoreException
     */
    public abstract void setPropertyToXml(QualifiedName key, String value, boolean store) throws CoreException;

    /**
     * This method should be called when the nature is being created (and paths are still being set)
     */
    public abstract void startInit();

    /**
     * This method should be called when the nature finished its initialization -- and paths are already set
     */
    public abstract void endInit();

}