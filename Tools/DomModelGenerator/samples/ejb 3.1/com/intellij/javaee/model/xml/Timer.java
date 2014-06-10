// Generated on Wed Apr 29 15:54:26 MSD 2009
// DTD/Schema  :    http://java.sun.com/xml/ns/javaee

package com.intellij.javaee.model.xml;

import com.intellij.javaee.model.xml.ejb.NamedMethod;
import com.intellij.javaee.model.xml.ejb.TimerSchedule;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://java.sun.com/xml/ns/javaee:timerType interface.
 * <pre>
 * <h3>Type http://java.sun.com/xml/ns/javaee:timerType documentation</h3>
 * The timerType specifies an enterprise bean timer.  Each
 * 	timer is automatically created by the container upon
 * 	deployment.  Timer callbacks occur based on the
 * 	schedule attributes.  All callbacks are made to the
 * 	timeout-method associated with the timer.
 * 	A timer can have an optional start and/or end date. If
 * 	a start date is specified, it takes precedence over the
 * 	associated timer schedule such that any matching
 * 	expirations prior to the start time will not occur.
 * 	Likewise, no matching expirations will occur after any
 * 	end date.   Start/End dates are specified using the
 * 	XML Schema dateTime type, which follows the ISO-8601
 * 	standard for date(and optional time-within-the-day)
 * 	representation.
 * 	An optional flag can be used to control whether
 * 	this timer has persistent(true) delivery semantics or
 * 	non-persistent(false) delivery semantics.  If not specified,
 *         the value defaults to persistent(true).
 * 	A time zone can optionally be associated with a timer.
 * 	If specified, the timer's schedule is evaluated in the context
 *         of that time zone, regardless of the default time zone in which
 * 	the container is executing.   Time zones are specified as an
 * 	ID string.  The set of required time zone IDs is defined by
 * 	the Zone Name(TZ) column of the public domain zoneinfo database.
 * 	An optional info string can be assigned to the timer and
 * 	retrieved at runtime through the Timer.getInfo() method.
 * 	The timerType can only be specified on stateless session
 * 	beans, singleton session beans, and message-driven beans.
 * </pre>
 */
public interface Timer extends CommonDomModelElement {

	/**
	 * Returns the list of description children.
	 * @return the list of description children.
	 */
	@NotNull
	List<Description> getDescriptions();
	/**
	 * Adds new child to the list of description children.
	 * @return created child
	 */
	Description addDescription();


	/**
	 * Returns the value of the schedule child.
	 * @return the value of the schedule child.
	 */
	@NotNull
	@Required
	TimerSchedule getSchedule();


	/**
	 * Returns the value of the start child.
	 * @return the value of the start child.
	 */
	@NotNull
	GenericDomValue<String> getStart();


	/**
	 * Returns the value of the end child.
	 * @return the value of the end child.
	 */
	@NotNull
	GenericDomValue<String> getEnd();


	/**
	 * Returns the value of the timeout-method child.
	 * @return the value of the timeout-method child.
	 */
	@NotNull
	@Required
	NamedMethod getTimeoutMethod();


	/**
	 * Returns the value of the persistent child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:true-falseType documentation</h3>
	 * This simple type designates a boolean with only two
	 * 	permissible values
	 * 	- true
	 * 	- false
	 * </pre>
	 * @return the value of the persistent child.
	 */
	@NotNull
	GenericDomValue<Boolean> getPersistent();


	/**
	 * Returns the value of the timezone child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:string documentation</h3>
	 * This is a special string datatype that is defined by Java EE as
	 * 	a base type for defining collapsed strings. When schemas
	 * 	require trailing/leading space elimination as well as
	 * 	collapsing the existing whitespace, this base type may be
	 * 	used.
	 * </pre>
	 * @return the value of the timezone child.
	 */
	@NotNull
	GenericDomValue<String> getTimezone();


	/**
	 * Returns the value of the info child.
	 * <pre>
	 * <h3>Type http://java.sun.com/xml/ns/javaee:string documentation</h3>
	 * This is a special string datatype that is defined by Java EE as
	 * 	a base type for defining collapsed strings. When schemas
	 * 	require trailing/leading space elimination as well as
	 * 	collapsing the existing whitespace, this base type may be
	 * 	used.
	 * </pre>
	 * @return the value of the info child.
	 */
	@NotNull
	GenericDomValue<String> getInfo();


}
