package com.advancedtools.webservices.rest;

import org.jetbrains.annotations.NonNls;

/**
 * @by Konstantin Bulenkov
 */
public interface RestAnnotations {
  @NonNls String PATH = "javax.ws.rs.Path";
  @NonNls String PATH_SHORT = "Path";

  @NonNls String PRODUCE_MIME = "javax.ws.rs.ProduceMime";
  @NonNls String PRODUCE_MIME_SHORT = "ProduceMime";
  @NonNls String PRODUCES = "javax.ws.rs.Produces";
  @NonNls String PRODUCES_SHORT = "Produces";

  @NonNls String CONSUME_MIME = "javax.ws.rs.ConsumeMime";
  @NonNls String CONSUME_MIME_SHORT = "ConsumeMime";
  @NonNls String CONSUMES = "javax.ws.rs.Consumes";
  @NonNls String CONSUMES_SHORT = "Consumes";

  @NonNls String PATH_PARAM = "javax.ws.rs.PathParam";
  @NonNls String PATH_PARAM_SHORT = "PathParam";

  @NonNls String GET = "javax.ws.rs.GET";
  @NonNls String PUT = "javax.ws.rs.PUT";
  @NonNls String DELETE = "javax.ws.rs.DELETE";
  @NonNls String POST = "javax.ws.rs.POST";
  @NonNls String HEAD = "javax.ws.rs.HEAD";

  @NonNls String GET_SHORT = "GET";
  @NonNls String PUT_SHORT = "PUT";
  @NonNls String DELETE_SHORT = "DELETE";
  @NonNls String POST_SHORT = "POST";
  @NonNls String HEAD_SHORT = "HEAD";
}
