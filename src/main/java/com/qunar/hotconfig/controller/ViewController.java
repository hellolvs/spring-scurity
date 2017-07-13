package com.qunar.hotconfig.controller;

import com.qunar.flight.qmonitor.QMonitor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by kun.ji on 2017/4/12.
 */
@Controller
public class ViewController {

    @RequestMapping("/loginView")
    public String loginView() {
        QMonitor.recordOne("Login_Request");
        return "qsso";
    }

    @RequestMapping("/configView")
    public String configView() {
        return "config";
    }

    @RequestMapping("/deniedView")
    public String deniedView() {
        return "denied";
    }

    @RequestMapping("/permissionView")
    public String permissionView() {
        return "permission";
    }
}
