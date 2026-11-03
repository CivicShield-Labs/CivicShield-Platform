package com.civicshield.platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class RequestBodyLimitFilterTest {

    @Test
    void rejectsOversizedRequestWithUnknownContentLength() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setContent(("x".repeat(70_000)).getBytes());
        HttpServletRequest unknownLengthRequest = new HttpServletRequestWrapper(request) {
            @Override
            public long getContentLengthLong() {
                return -1;
            }
        };

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        new RequestBodyLimitFilter().doFilter(unknownLengthRequest, response, filterChain);

        assertEquals(413, response.getStatus());
        verifyNoInteractions(filterChain);
    }
}
