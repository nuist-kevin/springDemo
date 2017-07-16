package com.focus.mic.test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class SendRequestController {

    @RequestMapping(path = "sendRequest")
    public String view() {
        return "sendRequest";
    }

    @RequestMapping(path = "/CBEC/{business}.do", method = RequestMethod.POST, produces = "application/text")
    @ResponseBody
    public String sendSignedReq(@PathVariable(name = "business") String business,  String url, String request) {
        System.out.println(business);
        System.out.println(request);
        return business + ": " + url + "-" + request;
    }
}
