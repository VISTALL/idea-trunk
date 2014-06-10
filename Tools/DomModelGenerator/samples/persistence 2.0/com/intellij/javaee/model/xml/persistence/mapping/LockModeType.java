// Generated on Tue Apr 28 15:52:23 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/persistence/orm

package com.intellij.javaee.model.xml.persistence.mapping;

/**
 * http://java.sun.com/xml/ns/persistence/orm:lock-mode-type enumeration.
 * <pre>
 * <h3>Enumeration http://java.sun.com/xml/ns/persistence/orm:lock-mode-type documentation</h3>
 * public enum LockModeType { READ, WRITE, OPTIMISTIC, 
 * OPTIMISTIC_FORCE_INCREMENT, PESSIMISTIC_READ, PESSIMISTIC_WRITE, 
 * PESSIMISTIC_FORCE_INCREMENT, NONE};
 * </pre>
 */
public enum LockModeType {
	NONE,
	OPTIMISTIC,
	OPTIMISTIC_FORCE_INCREMENT,
	PESSIMISTIC_FORCE_INCREMENT,
	PESSIMISTIC_READ,
	PESSIMISTIC_WRITE,
	READ,
	WRITE
}
