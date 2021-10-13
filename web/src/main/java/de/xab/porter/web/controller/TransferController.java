package de.xab.porter.web.controller;

import de.xab.porter.api.task.Context;
import de.xab.porter.core.Session;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransferController {
    @PostMapping("transfer")
    public String transfer(@RequestBody Context context) {
        Session session = new Session();
        session.start(context);
        return "OK";
    }
}
