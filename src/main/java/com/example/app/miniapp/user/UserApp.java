package com.example.app.miniapp.user;

import com.example.app.miniapp.MiniAppCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component("userApp")
public class UserApp {

    private final ApplicationContext applicationContext;

    @Autowired
    public UserApp(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public MiniAppCommand getCommand(String commandName) {
        return (MiniAppCommand) applicationContext.getBean(commandName);
    }
}
