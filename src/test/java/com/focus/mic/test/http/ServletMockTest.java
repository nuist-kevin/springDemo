package com.focus.mic.test.http;

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Created by caiwen on 2017/7/16.
 */
public class ServletMockTest {

    @Test
    public void test() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(HttpMethod.GET.name(), "http://localhost:8080/sendRequest");

    }
}
