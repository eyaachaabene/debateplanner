package com.defense.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Centralized global error handler for the reactive gateway.
 *
 * <p>Returns structured JSON error responses for all unhandled exceptions,
 * including routing errors (404) and downstream connection failures (502/503).
 */
@Slf4j
@Configuration
public class GlobalErrorHandler {

    @Bean
    @Order(-1)
    public GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler(
            ErrorAttributes errorAttributes,
            WebProperties webProperties,
            ApplicationContext applicationContext) {
        return new GlobalErrorWebExceptionHandler(errorAttributes, webProperties, applicationContext);
    }

    public static class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

        public GlobalErrorWebExceptionHandler(
                ErrorAttributes errorAttributes,
                WebProperties webProperties,
                ApplicationContext applicationContext) {
            super(errorAttributes, webProperties.getResources(), applicationContext);
        }

        @Override
        protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
            return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
        }

        private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
            Throwable error = getError(request);
            log.error("Gateway error for {}: {}", request.uri(), error.getMessage(), error);

            HttpStatus status = determineStatus(error);
            String message = determineMessage(error, status);

            Map<String, Object> body = Map.of(
                    "status",  status.value(),
                    "error",   status.getReasonPhrase(),
                    "message", message,
                    "path",    request.uri().getPath()
            );

            return ServerResponse
                    .status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body);
        }

        private HttpStatus determineStatus(Throwable error) {
            if (error instanceof NotFoundException) return HttpStatus.NOT_FOUND;
            if (error instanceof org.springframework.web.server.ResponseStatusException rse) {
                return HttpStatus.valueOf(rse.getStatusCode().value());
            }
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        private String determineMessage(Throwable error, HttpStatus status) {
            return switch (status) {
                case NOT_FOUND             -> "The requested route was not found";
                case BAD_GATEWAY           -> "Downstream service is unavailable";
                case SERVICE_UNAVAILABLE   -> "Service temporarily unavailable";
                case GATEWAY_TIMEOUT       -> "Downstream service timed out";
                default                    -> "An unexpected error occurred";
            };
        }
    }
}
