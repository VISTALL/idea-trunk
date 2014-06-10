package com.intellij.spring.model.xml.jms;

import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

public interface Listener extends SpringJmsElement {

  /**
   * Returns the value of the destination child.
   * <pre>
   * <h3>Attribute null:destination documentation</h3>
   * 	The destination name for this listener, resolved through the
   * 	container-wide DestinationResolver strategy (if any). Required.
   * <p/>
   * </pre>
   *
   * @return the value of the destination child.
   */
  @NotNull
  @Required
  GenericAttributeValue<String> getDestination();

  /**
   * Returns the value of the subscription child.
   * <pre>
   * <h3>Attribute null:subscription documentation</h3>
   * 	The name for the durable subscription, if any.
   * <p/>
   * </pre>
   *
   * @return the value of the subscription child.
   */
  @NotNull
  GenericAttributeValue<String> getSubscription();


  /**
   * Returns the value of the selector child.
   * <pre>
   * <h3>Attribute null:selector documentation</h3>
   * 	The JMS message selector for this listener.
   * <p/>
   * </pre>
   *
   * @return the value of the selector child.
   */
  @NotNull
  GenericAttributeValue<String> getSelector();


  /**
   * Returns the value of the ref child.
   * <pre>
   * <h3>Attribute null:ref documentation</h3>
   * 	The bean name of the listener object, implementing
   * 	the MessageListener/SessionAwareMessageListener interface
   * 	or defining the specified listener method. Required.
   * <p/>
   * </pre>
   *
   * @return the value of the ref child.
   */
  @NotNull
  @Required
  GenericAttributeValue<String> getRef();

  /**
   * Returns the value of the method child.
   * <pre>
   * <h3>Attribute null:method documentation</h3>
   * 	The name of the listener method to invoke. If not specified,
   * 	the target bean is supposed to implement the MessageListener
   * 	or SessionAwareMessageListener interface.
   * <p/>
   * </pre>
   *
   * @return the value of the method child.
   */
  @NotNull
  GenericAttributeValue<String> getMethod();


  /**
   * Returns the value of the response-destination child.
   * <pre>
   * <h3>Attribute null:response-destination documentation</h3>
   * 	The name of the default response destination to send response messages to.
   * 	This will be applied in case of a request message that does not carry
   * 	a "JMSReplyTo" field. The type of this destination will be determined
   * 	by the listener-container's "destination-type" attribute.
   * 	Note: This only applies to a listener method with a return value,
   * 	for which each result object will be converted into a response message.
   * <p/>
   * </pre>
   *
   * @return the value of the response-destination child.
   */
  @NotNull
  GenericAttributeValue<String> getResponseDestination();
}
