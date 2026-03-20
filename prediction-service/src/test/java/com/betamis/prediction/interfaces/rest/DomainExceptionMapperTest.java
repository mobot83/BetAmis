package com.betamis.prediction.interfaces.rest;

import com.betamis.prediction.domain.exception.KickOffAlreadyPassedException;
import com.betamis.prediction.domain.exception.PredictionAlreadyClosedException;
import com.betamis.prediction.domain.exception.PredictionAlreadySubmittedException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DomainExceptionMapperTest {

    private final DomainExceptionMapper mapper = new DomainExceptionMapper();

    @Test
    void kickOffAlreadyPassed_returns422() {
        Response response = mapper.toResponse(new KickOffAlreadyPassedException("too late"));
        assertEquals(422, response.getStatus());
    }

    @Test
    void predictionAlreadyClosed_returns422() {
        Response response = mapper.toResponse(new PredictionAlreadyClosedException("already closed"));
        assertEquals(422, response.getStatus());
    }

    @Test
    void predictionAlreadySubmitted_returns409() {
        Response response = mapper.toResponse(new PredictionAlreadySubmittedException("duplicate"));
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    @Test
    void illegalArgument_returns400() {
        Response response = mapper.toResponse(new IllegalArgumentException("bad input"));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void unknownRuntimeException_returns500() {
        Response response = mapper.toResponse(new RuntimeException("unexpected"));
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    void errorResponseBody_containsMessage() {
        Response response = mapper.toResponse(new KickOffAlreadyPassedException("kick-off passed"));
        DomainExceptionMapper.ErrorResponse body = (DomainExceptionMapper.ErrorResponse) response.getEntity();
        assertEquals("kick-off passed", body.message());
    }
}
