// Generated on Tue Jun 17 15:40:19 MSD 2008
// DTD/Schema  :    http://www.springframework.org/schema/jms

package com.intellij.spring.model.xml.jms;

import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/jms:listener-containerElemType interface.
 */
public interface ListenerContainer extends SpringJmsElement {

  /**
   * Returns the value of the container-type child.
   * <pre>
   * <h3>Attribute null:container-type documentation</h3>
   * 	The type of this listener container: "default" or "simple", choosing
   * 	between DefaultMessageListenerContainer and SimpleMessageListenerContainer.
   * 	The "102" suffix adapts to a JMS provider that implements JMS 1.0.2 only.
   * <p/>
   * </pre>
   *
   * @return the value of the container-type child.
   */
  @NotNull
  GenericAttributeValue<ContainerType> getContainerType();


  /**
   * Returns the value of the connection-factory child.
   * <pre>
   * <h3>Attribute null:connection-factory documentation</h3>
   * 	A reference to the JMS ConnectionFactory bean.
   * 	Default is "connectionFactory".
   * <p/>
   * </pre>
   *
   * @return the value of the connection-factory child.
   */
  @NotNull
  GenericAttributeValue<String> getConnectionFactory();


  /**
   * Returns the value of the task-executor child.
   * <pre>
   * <h3>Attribute null:task-executor documentation</h3>
   * 	A reference to the Spring TaskExecutor for the JMS listener invokers.
   * 	Default is a SimpleAsyncTaskExecutor, using internally managed threads.
   * <p/>
   * </pre>
   *
   * @return the value of the task-executor child.
   */
  @NotNull
  GenericAttributeValue<String> getTaskExecutor();


  /**
   * Returns the value of the destination-resolver child.
   * <pre>
   * <h3>Attribute null:destination-resolver documentation</h3>
   * 	A reference to the DestinationResolver strategy for resolving destination names.
   * 	Default is a DynamicDestinationResolver, using the JMS provider's queue/topic
   * 	name resolution. Alternatively, specify a reference to a JndiDestinationResolver
   * 	(typically in a J2EE environment).
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
   * 	A reference to an external PlatformTransactionManager (typically an
   * 	XA-based transaction coordinator, e.g. Spring's JtaTransactionManager).
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
   * 	The maximum number of concurrent sessions/consumers to start for each listener.
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
