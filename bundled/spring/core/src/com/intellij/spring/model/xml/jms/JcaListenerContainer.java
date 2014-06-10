package com.intellij.spring.model.xml.jms;

import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface JcaListenerContainer extends SpringJmsElement {

  /**
   * Returns the value of the resource-adapter child.
   * <pre>
   * <h3>Attribute null:resource-adapter documentation</h3>
   * 	A reference to the JCA ResourceAdapter bean for the JMS provider.
   * 	Default is "resourceAdapter".
   * <p/>
   * </pre>
   *
   * @return the value of the resource-adapter child.
   */
  @NotNull
  GenericAttributeValue<String> getResourceAdapter();


  /**
   * Returns the value of the activation-spec-factory child.
   * <pre>
   * <h3>Attribute null:activation-spec-factory documentation</h3>
   * 	A reference to the JmsActivationSpecFactory.
   * 	Default is to autodetect the JMS provider and its ActivationSpec class
   * 	(see DefaultJmsActivationSpecFactory).
   * <p/>
   * </pre>
   *
   * @return the value of the activation-spec-factory child.
   */
  @NotNull
  GenericAttributeValue<String> getActivationSpecFactory();


  /**
   * Returns the value of the destination-resolver child.
   * <pre>
   * <h3>Attribute null:destination-resolver documentation</h3>
   *   A reference to the DestinationResolver strategy for resolving destination names.
   *   Default is to pass in the destination name Strings into the JCA ActivationSpec as-is.
   *   Alternatively, specify a reference to a JndiDestinationResolver (typically in a J2EE
   *   environment, in particular if the server insists on receiving Destination objects).
   * <p/>
   * </pre>
   *
   * @return the value of the destination-resolver child.
   */
  @NotNull
  GenericAttributeValue<String> getDestinationResolver();


  /**
   * Returns the value of the message-converter child.
   * <pre>
   * <h3>Attribute null:message-converter documentation</h3>
   * 	A reference to the MessageConverter strategy for converting JMS Messages to
   * 	listener method arguments. Default is a SimpleMessageConverter.
   * <p/>
   * </pre>
   *
   * @return the value of the message-converter child.
   */
  @NotNull
  GenericAttributeValue<String> getMessageConverter();


  /**
   * Returns the value of the destination-type child.
   * <pre>
   * <h3>Attribute null:destination-type documentation</h3>
   * 	The JMS destination type for this listener: "queue", "topic" or "durableTopic".
   * 	Default is "queue".
   * <p/>
   * </pre>
   *
   * @return the value of the destination-type child.
   */
  @NotNull
  GenericAttributeValue<DestinationType> getDestinationType();


  /**
   * Returns the value of the client-id child.
   * <pre>
   * <h3>Attribute null:client-id documentation</h3>
   * 	The JMS client id for this listener container.
   * <p/>
   * </pre>
   *
   * @return the value of the client-id child.
   */
  @NotNull
  GenericAttributeValue<String> getClientId();


  /**
   * Returns the value of the acknowledge child.
   * <pre>
   * <h3>Attribute null:acknowledge documentation</h3>
   * 	The native JMS acknowledge mode: "auto", "client", "dups-ok" or "transacted".
   * 	The latter effectively activates a locally transacted Session; as alternative,
   * 	specify an external "transaction-manager" via the corresponding attribute.
   * 	Default is "auto".
   * <p/>
   * </pre>
   *
   * @return the value of the acknowledge child.
   */
  @NotNull
  GenericAttributeValue<Acknowledge> getAcknowledge();


  /**
   * Returns the value of the transaction-manager child.
   * <pre>
   * <h3>Attribute null:transaction-manager documentation</h3>
   * 	A reference to the Spring JtaTransactionManager or [javax.transaction.TransactionManager],
   * 	for kicking off an XA transaction for each incoming message.
   * 	If not specified, native acknowledging will be used (see "acknowledge" attribute).
   * <p/>
   * </pre>
   *
   * @return the value of the transaction-manager child.
   */
  @NotNull
  GenericAttributeValue<String> getTransactionManager();


  /**
   * Returns the value of the concurrency child.
   * <pre>
   * <h3>Attribute null:concurrency documentation</h3>
   * 	The maximum number of concurrent sessions to activate for each listener.
   * 	Default is 1.
   * <p/>
   * </pre>
   *
   * @return the value of the concurrency child.
   */
  @NotNull
  GenericAttributeValue<Integer> getConcurrency();


  /**
   * Returns the value of the prefetch child.
   * <pre>
   * <h3>Attribute null:prefetch documentation</h3>
   * 	The maximum number of messages to load into a single session.
   * 	Note that raising this number might lead to starvation of concurrent consumers!
   * <p/>
   * </pre>
   *
   * @return the value of the prefetch child.
   */
  @NotNull
  GenericAttributeValue<Integer> getPrefetch();


  /**
   * Returns the list of listener children.
   *
   * @return the list of listener children.
   */
  @NotNull
  List<Listener> getListeners();

  /**
   * Adds new child to the list of listener children.
         *
         * @return created child
         */
	Listener addListener();


}
