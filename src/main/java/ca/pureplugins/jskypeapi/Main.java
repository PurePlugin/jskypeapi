package ca.pureplugins.jskypeapi;

import ca.pureplugins.jskypeapi.controller.LoginController;

public class Main {
    public static void main(String[] args) throws Exception {
        LoginController loginController = new LoginController(args[0], args[1]);
        System.out.println(loginController.getToken());
    }
}
